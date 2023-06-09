package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.ebms3.model.Ebms3Error;
import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.message.splitandjoin.SplitAndJoinService;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * Handles the incoming AS4 Signal containing an error
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingSignalErrorHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingSignalErrorHandler.class);

    @Autowired
    protected UserMessageDao userMessageDao;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected SoapUtil soapUtil;

    @Override
    public SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging messaging) {
        final Ebms3SignalMessage signalMessage = messaging.getSignalMessage();
        if (CollectionUtils.isEmpty(signalMessage.getError())) {
            LOG.warn("Could not process the Signal: no errors found");
            return null;
        }

        if (signalMessage.getError().size() > 1) {
            LOG.warn("More than one error received in the SignalMessage, only the first one will be processed");
        }

        final Ebms3Error error = signalMessage.getError().iterator().next();
        LOG.debug("Processing Signal with error [{}]", error);

        final String refToMessageId = signalMessage.getMessageInfo().getRefToMessageId();
        final UserMessage userMessage = userMessageDao.findByMessageId(refToMessageId, MSHRole.SENDING);
        if (userMessage == null) {
            LOG.warn("Could not process the Signal: no message [{}] found", refToMessageId);
            return null;
        }

        if (userMessage.isSourceMessage()) {
            processSourceMessageSignalError(userMessage);
        } else {
            LOG.warn("Could not process the Signal for message [{}]: not yet supported", refToMessageId);
        }

        LOG.debug("Finished processing Signal error");
        return null;
    }

    protected void processSourceMessageSignalError(final UserMessage sourceMessage) {
        final String messageId = sourceMessage.getMessageId();
        LOG.info("Processing Signal error for SourceMessage [{}]", messageId);

        splitAndJoinService.handleSourceMessageSignalError(messageId);

        LOG.debug("Finished processing Signal error for SourceMessage [{}]", messageId);
    }


}
