package eu.domibus.core.plugin.notification;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class PluginAsyncNotificationJMSConfigurerTest {

    @Tested
    PluginAsyncNotificationJMSConfigurer pluginAsyncNotificationJMSConfigurer;

    @Injectable
    ObjectProvider<PluginAsyncNotificationListener> asyncNotificationListenerProvider;

    @Injectable
    protected JmsListenerContainerFactory internalJmsListenerContainerFactory;

    @Injectable
    protected AuthUtils authUtils;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PluginEventNotifierProvider pluginEventNotifierProvider;

    @Injectable
    List<AsyncNotificationConfiguration> notificationListenerServices;

    @Test
    public void configureJmsListenersWithNoAsyncPlugins(@Injectable JmsListenerEndpointRegistrar registrar) throws JMSException {
        pluginAsyncNotificationJMSConfigurer.asyncNotificationConfigurations = null;

        new Expectations(pluginAsyncNotificationJMSConfigurer) {{
            pluginAsyncNotificationJMSConfigurer.initializeAsyncNotificationLister(registrar, (AsyncNotificationConfiguration) any);
            times = 0;
        }};

        pluginAsyncNotificationJMSConfigurer.configureJmsListeners(registrar);

        new FullVerifications() {{
        }};
    }
    
    @Test
    public void initializeAsyncNotificationLister(@Injectable JmsListenerEndpointRegistrar registrar,
                                                  @Injectable AsyncNotificationConfiguration asyncNotificationConfiguration,
                                                  @Injectable BackendConnector backendConnector,
                                                  @Injectable Queue queue,
                                                  @Injectable SimpleJmsListenerEndpoint endpoint) {
        new Expectations(pluginAsyncNotificationJMSConfigurer) {{
            asyncNotificationConfiguration.getBackendConnector();
            result = backendConnector;

            asyncNotificationConfiguration.getBackendNotificationQueue();
            result = queue;

            pluginAsyncNotificationJMSConfigurer.createJMSListener(asyncNotificationConfiguration);
            result = endpoint;
        }};

        pluginAsyncNotificationJMSConfigurer.initializeAsyncNotificationLister(registrar, asyncNotificationConfiguration);

        new Verifications() {{
            registrar.registerEndpoint(endpoint, internalJmsListenerContainerFactory);
        }};
    }

    @Test
    public void createJMSListener(@Injectable AsyncNotificationConfiguration asyncNotificationListener,
                                  @Injectable BackendConnector backendConnector,
                                  @Injectable PluginAsyncNotificationListener pluginAsyncNotificationListener) throws JMSException {
        String queueName = "myqueue";

        new Expectations() {{
            asyncNotificationListener.getBackendConnector();
            result = backendConnector;

            asyncNotificationListenerProvider.getObject(asyncNotificationListener);
            result = pluginAsyncNotificationListener;

            asyncNotificationListener.getQueueName();
            result = queueName;
        }};

        SimpleJmsListenerEndpoint jmsListener = pluginAsyncNotificationJMSConfigurer.createJMSListener(asyncNotificationListener);
        Assert.assertEquals(jmsListener.getMessageListener(), pluginAsyncNotificationListener);
        Assert.assertEquals(jmsListener.getDestination(), queueName);
    }
}