package eu.domibus.plugin.jms;

import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.MapMessage;

/**
 * @author Cosmin Baciu
 */
@Service
public class JMSPluginReceivingListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSPluginReceivingListener.class);

    @Autowired
    protected JMSPluginImpl backendJMS;

    @Autowired
    protected AuthenticationExtService authenticationExtService;


    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @JmsListener(destination = "${jmsplugin.queue.in}", containerFactory = "backendJmsListenerContainerFactory")
    public void receiveMessage(final MapMessage map) {
        if (!authenticationExtService.isUnsecureLoginAllowed()) {
            LOG.debug("Performing authentication");
            LOG.clearCustomKeys();
            authenticate(map);
        }
        backendJMS.receiveMessage(map);
    }

    protected void authenticate(final MapMessage map) {
        String username;
        String password;
        try {
            username = map.getStringProperty(JMSMessageConstants.USERNAME);
            password = map.getStringProperty(JMSMessageConstants.PASSWORD);
        } catch (Exception e) {
            LOG.error("Exception occurred while retrieving the username or password", e);
            throw new DefaultJmsPluginException("Exception occurred while retrieving the username or password", e);
        }
        if (StringUtils.isBlank(username)) {
            LOG.error("Username is empty");
            throw new DefaultJmsPluginException("Username is empty");
        }
        if (StringUtils.isBlank(password)) {
            LOG.error("Password is empty");
            throw new DefaultJmsPluginException("Password is empty");
        }

        authenticationExtService.basicAuthenticate(username, password);
    }

}
