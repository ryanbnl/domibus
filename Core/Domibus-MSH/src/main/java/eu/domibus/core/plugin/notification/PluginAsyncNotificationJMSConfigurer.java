package eu.domibus.core.plugin.notification;

import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginAsyncNotificationJMSConfigurer implements JmsListenerConfigurer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginAsyncNotificationJMSConfigurer.class);

    protected List<AsyncNotificationConfiguration> asyncNotificationConfigurations;
    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected ObjectProvider<PluginAsyncNotificationListener> asyncNotificationListenerProvider;


    public PluginAsyncNotificationJMSConfigurer(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                ObjectProvider<PluginAsyncNotificationListener> asyncNotificationListenerProvider,
                                                @Autowired(required = false) List<AsyncNotificationConfiguration> asyncNotificationConfigurations) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.asyncNotificationConfigurations = asyncNotificationConfigurations;
        this.asyncNotificationListenerProvider = asyncNotificationListenerProvider;
    }

    @Override
    public void configureJmsListeners(final JmsListenerEndpointRegistrar registrar) {
        LOG.info("Initializing services of type AsyncNotificationListenerService");

        if (CollectionUtils.isEmpty(asyncNotificationConfigurations)) {
            LOG.info("No service of type AsyncNotificationListenerService detected");
            return;
        }

        for (AsyncNotificationConfiguration asyncNotificationConfiguration : asyncNotificationConfigurations) {
            initializeAsyncNotificationLister(registrar, asyncNotificationConfiguration);
        }
    }

    protected void initializeAsyncNotificationLister(JmsListenerEndpointRegistrar registrar,
                                                     AsyncNotificationConfiguration asyncNotificationConfiguration) {
        BackendConnector backendConnector = asyncNotificationConfiguration.getBackendConnector();
        if (backendConnector == null) {
            LOG.error("No connector configured for async notification listener");
            return;
        }

        if (asyncNotificationConfiguration.getBackendNotificationQueue() == null) {
            LOG.info("No notification queue configured for plugin [{}]. No async notification listener is created", backendConnector.getName());
            return;
        }

        SimpleJmsListenerEndpoint endpoint = createJMSListener(asyncNotificationConfiguration);
        if (endpoint != null) {
            registrar.registerEndpoint(endpoint, jmsListenerContainerFactory);
            LOG.info("Instantiated AsyncNotificationListenerService for backend [{}]", backendConnector.getName());
        }
    }

    protected SimpleJmsListenerEndpoint createJMSListener(AsyncNotificationConfiguration asyncNotificationListener) {
        BackendConnector backendConnector = asyncNotificationListener.getBackendConnector();
        LOG.debug("Configuring JmsListener for plugin [{}]", backendConnector.getName());

        final SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId(backendConnector.getName());
        final Queue pushQueue = asyncNotificationListener.getBackendNotificationQueue();
        if (pushQueue == null) {
            throw new ConfigurationException("No notification queue found for " + backendConnector.getName());
        }
        try {
            endpoint.setDestination(asyncNotificationListener.getQueueName());
        } catch (final JMSException e) {
            LOG.error("Problem with predefined queue.", e);
            return null;
        }
        PluginAsyncNotificationListener pluginAsyncNotificationListener = asyncNotificationListenerProvider.getObject(asyncNotificationListener);
        endpoint.setMessageListener(pluginAsyncNotificationListener);

        return endpoint;
    }
}
