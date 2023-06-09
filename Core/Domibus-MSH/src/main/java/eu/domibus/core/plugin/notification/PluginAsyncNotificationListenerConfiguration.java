package eu.domibus.core.plugin.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.notification.AsyncNotificationConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.config.JmsListenerContainerFactory;

import static eu.domibus.api.property.DomibusGeneralConstants.JSON_MAPPER_BEAN;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class PluginAsyncNotificationListenerConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginAsyncNotificationListenerConfiguration.class);

    protected JmsListenerContainerFactory jmsListenerContainerFactory;
    protected AuthUtils authUtils;
    protected DomainContextProvider domainContextProvider;
    protected PluginEventNotifierProvider pluginEventNotifierProvider;
    protected ObjectMapper objectMapper;

    public PluginAsyncNotificationListenerConfiguration(@Qualifier("internalJmsListenerContainerFactory") JmsListenerContainerFactory jmsListenerContainerFactory,
                                                        AuthUtils authUtils,
                                                        DomainContextProvider domainContextProvider,
                                                        PluginEventNotifierProvider pluginEventNotifierProvider,
                                                        @Qualifier(JSON_MAPPER_BEAN) ObjectMapper objectMapper) {
        this.jmsListenerContainerFactory = jmsListenerContainerFactory;
        this.authUtils = authUtils;
        this.domainContextProvider = domainContextProvider;
        this.pluginEventNotifierProvider = pluginEventNotifierProvider;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public PluginAsyncNotificationListener createAsyncNotificationListener(AsyncNotificationConfiguration asyncNotificationConfiguration) {
        PluginAsyncNotificationListener notificationListenerServiceImpl = new PluginAsyncNotificationListener(domainContextProvider,
                asyncNotificationConfiguration, pluginEventNotifierProvider, authUtils, objectMapper);
        return notificationListenerServiceImpl;
    }
}
