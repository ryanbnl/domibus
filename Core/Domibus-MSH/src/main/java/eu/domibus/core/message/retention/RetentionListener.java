package eu.domibus.core.message.retention;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.multitenancy.DomibusDomainException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Listeners that deletes messages by their identifiers.
 *
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class RetentionListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RetentionListener.class);

    @Autowired
    private UserMessageDefaultService userMessageDefaultService;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Timer(clazz = RetentionListener.class,value = "onMessage.deleteMessages")
    @Counter(clazz = RetentionListener.class,value = "onMessage.deleteMessages")
    public void onMessage(final Message message) {
        authUtils.runWithSecurityContext(() -> onMessagePrivate(message), "retention", "retention", AuthRole.ROLE_ADMIN);
    }

    protected void onMessagePrivate(final Message message) {
        try {
            final String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            LOG.debug("Processing JMS message for domain [{}]", domainCode);
            try {
                domainContextProvider.setCurrentDomainWithValidation(domainCode);
            } catch (DomibusDomainException ex) {
                LOG.error("Invalid domain: [{}]", domainCode, ex);
                return;
            }

            MessageDeleteType deleteType = MessageDeleteType.valueOf(message.getStringProperty(MessageRetentionDefaultService.DELETE_TYPE));
            if (MessageDeleteType.SINGLE == deleteType) {
                String messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
                String roleName = message.getStringProperty(MessageConstants.MSH_ROLE);
                MSHRole mshRole = roleName!=null && !roleName.equals("null") ? MSHRole.valueOf(roleName) : null;
                LOG.debug("Delete one message [{}] [{}]", messageId, mshRole);
                userMessageDefaultService.deleteMessage(messageId, mshRole);
                return;
            }

            LOG.warn("Unknown message type [{}], JMS message will be ignored.", deleteType);
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }
    }
}
