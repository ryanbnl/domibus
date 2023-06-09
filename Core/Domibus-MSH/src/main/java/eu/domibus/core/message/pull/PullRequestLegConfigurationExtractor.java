package eu.domibus.core.message.pull;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.Ebms3PullRequest;
import eu.domibus.api.model.MessageType;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.receiver.leg.AbstractSignalLegConfigurationExtractor;
import eu.domibus.core.ebms3.receiver.leg.MessageLegConfigurationVisitor;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class PullRequestLegConfigurationExtractor extends AbstractSignalLegConfigurationExtractor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PullRequestLegConfigurationExtractor.class);

    private MessageExchangeService messageExchangeService;

    public PullRequestLegConfigurationExtractor(SoapMessage message, Ebms3Messaging messaging) {
        super(message, messaging);
    }

    @Override
    protected String getMessageId() {
        return ebms3Messaging.getSignalMessage().getMessageInfo().getMessageId();
    }

    @Override
    public LegConfiguration process() throws EbMS3Exception {
        message.put(MSHDispatcher.MESSAGE_TYPE_IN, MessageType.SIGNAL_MESSAGE);
        Ebms3PullRequest pullRequest = ebms3Messaging.getSignalMessage().getPullRequest();
        try {
            String mpc = pullRequest.getMpc();
            PullContext pullContext = messageExchangeService.extractProcessOnMpc(mpc);
            LegConfiguration legConfiguration = pullContext.getProcess().getLegs().iterator().next();
            String initiatorPartyName = null;
            if (pullContext.getInitiator() != null) {
                LOG.debug("Get initiator from pull context");
                initiatorPartyName = pullContext.getInitiator().getName();
            } else if (messageExchangeService.forcePullOnMpc(mpc)) {
                LOG.debug("Extract initiator from mpc");
                initiatorPartyName = messageExchangeService.extractInitiator(mpc);
            }
            LOG.debug("Initiator is [{}]", initiatorPartyName);

            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(pullContext.getAgreement(),
                    pullContext.getResponder().getName(),
                    initiatorPartyName,
                    legConfiguration.getService().getName(),
                    legConfiguration.getAction().getName(),
                    legConfiguration.getName());
            LOG.debug("Extracted the exchange configuration, pModeKey is [{}]", messageExchangeConfiguration.getPmodeKey());
            setUpMessage(messageExchangeConfiguration.getPmodeKey());
            return legConfiguration;
        } catch (PModeException p) {
            EbMS3Exception ebMS3Exception =  EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                    .message( "Error for pullrequest with mpc:" + pullRequest.getMpc() + " " + p.getMessage())
                    .cause(p)
                    .build();
            LOG.warn("Could not extract pull request leg configuration from pMode", ebMS3Exception);
            throw ebMS3Exception;
        }
    }

    @Override
    public void accept(MessageLegConfigurationVisitor visitor) {
        visitor.visit(this);
    }

    public void setMessageExchangeService(MessageExchangeService messageExchangeService) {
        this.messageExchangeService = messageExchangeService;
    }
}
