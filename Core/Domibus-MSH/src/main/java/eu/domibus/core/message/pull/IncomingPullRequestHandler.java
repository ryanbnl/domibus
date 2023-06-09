package eu.domibus.core.message.pull;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.security.AuthorizationServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;

/**
 * Handles the incoming AS4 pull request
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class IncomingPullRequestHandler implements IncomingMessageHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IncomingPullRequestHandler.class);

    @Autowired
    private PullRequestHandler pullRequestHandler;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    private AuthorizationServiceImpl authorizationService;

    @Override
    @Timer(clazz = IncomingPullRequestHandler.class, value = "incoming_pull_request")
    @Counter(clazz = IncomingPullRequestHandler.class, value = "incoming_pull_request")
    public SOAPMessage processMessage(SOAPMessage request, Ebms3Messaging messaging) throws EbMS3Exception {
        authorizationService.authorizePullRequest(request, messaging.getSignalMessage().getPullRequest().getMpc());
        LOG.trace("before pull request.");

        String refToMessageId = (messaging.getSignalMessage().getMessageInfo() == null) ? null :
                messaging.getSignalMessage().getMessageInfo().getMessageId();
        final SOAPMessage soapMessage = handlePullRequest(messaging.getSignalMessage().getPullRequest().getMpc(), refToMessageId);
        LOG.trace("returning pull request message.");
        return soapMessage;
    }

    protected SOAPMessage handlePullRequest(String mpc, String refToMessageId) {
        PullContext pullContext;
        try {
            LOG.debug("Extract process on MPC [{}]", mpc);
            pullContext = messageExchangeService.extractProcessOnMpc(mpc);
        } catch (PModeException pme) {
            LOG.debug("No process for mpc [{}]", mpc);
            if (messageExchangeService.forcePullOnMpc(mpc)) {
                LOG.debug("Extract base mpc");
                mpc = messageExchangeService.extractBaseMpc(mpc);
                LOG.debug("Trying base mpc [{}]", mpc);
                pullContext = messageExchangeService.extractProcessOnMpc(mpc);
            } else {
                throw pme;
            }
        }
        LOG.debug("Retrieve ready to pull User message for mpc: [{}]", mpc);
        String messageId = messageExchangeService.retrieveReadyToPullUserMessageId(mpc, pullContext.getInitiator());


        return pullRequestHandler.handlePullRequest(messageId, pullContext, refToMessageId);
    }
}
