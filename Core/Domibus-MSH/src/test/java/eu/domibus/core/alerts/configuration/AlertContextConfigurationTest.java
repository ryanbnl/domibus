package eu.domibus.core.alerts.configuration;

import eu.domibus.api.property.DomibusPropertyChangeNotifier;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.alerts.configuration.common.AlertConfigurationService;
import eu.domibus.core.alerts.configuration.common.AlertContextConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.destination.JndiDestinationResolver;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import java.util.Optional;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class AlertContextConfigurationTest {


    @Tested
    AlertContextConfiguration alertContextConfiguration;

    @Mocked
    DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory;

    @Injectable
    DomibusPropertyChangeNotifier domibusPropertyChangeNotifier;

    @Injectable
    AlertConfigurationService alertConfigurationService;

    @Test
    public void alertJmsListenerContainerFactory(@Injectable ConnectionFactory connectionFactory,
                                                 @Injectable DomibusPropertyProvider domibusPropertyProvider,
                                                 @Injectable MappingJackson2MessageConverter jackson2MessageConverter,
                                                 @Injectable Optional<JndiDestinationResolver> internalDestinationResolver
                                                 ) {
        String concurrency = "2-3";

        new Expectations() {{
            domibusPropertyProvider.getProperty(DomibusPropertyMetadataManagerSPI.DOMIBUS_ALERT_QUEUE_CONCURRENCY);
            this.result = concurrency;
        }};


        alertContextConfiguration.alertJmsListenerContainerFactory(connectionFactory, domibusPropertyProvider, jackson2MessageConverter, internalDestinationResolver,null);

        new Verifications() {{
            MessageConverter messageConverter = null;
            defaultJmsListenerContainerFactory.setMessageConverter(messageConverter = withCapture());
            Assert.assertEquals(messageConverter, jackson2MessageConverter);

            ConnectionFactory cf = null;
            defaultJmsListenerContainerFactory.setConnectionFactory(cf = withCapture());
            Assert.assertEquals(connectionFactory, cf);

            String factoryConcurrency = null;
            defaultJmsListenerContainerFactory.setConcurrency(factoryConcurrency = withCapture());
            Assert.assertEquals(factoryConcurrency, concurrency);

            defaultJmsListenerContainerFactory.setSessionTransacted(true);

            Integer sessionAckMode = null;
            defaultJmsListenerContainerFactory.setSessionAcknowledgeMode(sessionAckMode = withCapture());
            Assert.assertEquals(Session.SESSION_TRANSACTED, sessionAckMode.intValue());
        }};
    }
}
