package eu.domibus.core.ebms3.sender;

import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.UserMessageServiceHelper;
import eu.domibus.core.message.nonrepudiation.NonRepudiationService;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.message.reliability.ReliabilityService;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.interceptor.Fault;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;

/**
 * Common logic for sending AS4 messages to C3
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class AbstractUserMessageSender implements MessageSender {

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Autowired
    protected EbMS3MessageBuilder messageBuilder;

    @Autowired
    protected ReliabilityChecker reliabilityChecker;

    @Autowired
    protected ResponseHandler responseHandler;

    @Autowired
    protected NonRepudiationService nonRepudiationService;

    @Autowired
    protected MessageExchangeService messageExchangeService;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected ReliabilityService reliabilityService;

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Autowired
    protected UserMessageServiceHelper userMessageServiceHelper;

    @Override
    @Timer(clazz = AbstractUserMessageSender.class, value = "outgoing_user_message")
    @Counter(clazz = AbstractUserMessageSender.class, value = "outgoing_user_message")
    public void sendMessage(final UserMessage userMessage, final UserMessageLog userMessageLog) {
        String messageId = userMessage.getMessageId();

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        attempt.setStatus(MessageAttemptStatus.SUCCESS);

        ReliabilityChecker.CheckResult reliabilityCheckResult = ReliabilityChecker.CheckResult.SEND_FAIL;
        ResponseResult responseResult = null;
        SOAPMessage responseSoapMessage = null;
        String requestRawXMLMessage = null;

        LegConfiguration legConfiguration = null;
        final String pModeKey;

        try {
            try {
                validateBeforeSending(userMessage);
            } catch (DomibusCoreException e) {
                getLog().error("Validation exception: message [{}] will not be send", messageId, e);
                attempt.setError(e.getMessage());
                attempt.setStatus(MessageAttemptStatus.ABORT);
                // this flag is used in the final clause
                reliabilityCheckResult = ReliabilityChecker.CheckResult.ABORT;
                return;
            }

            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            getLog().debug("PMode found [{}]", pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            getLog().info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            Policy policy;
            try {
                policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            } catch (final ConfigurationException e) {
                throw EbMS3ExceptionBuilder.getInstance()
                        .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0010)
                        .message("Policy configuration invalid")
                        .mshRole(MSHRole.SENDING)
                        .cause(e)
                        .build();
            }

            getLog().debug("pModeKey is [{}]", pModeKey);
            Party sendingParty = pModeProvider.getSenderParty(pModeKey);
            Validate.notNull(sendingParty, "Initiator party was not found");
            Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            Validate.notNull(receiverParty, "Responder party was not found");

            try {
                messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
                messageExchangeService.verifySenderCertificate(legConfiguration, sendingParty.getName());
            } catch (ChainCertificateInvalidException cciEx) {
                getLog().securityError(DomibusMessageCode.SEC_INVALID_X509CERTIFICATE, cciEx);
                attempt.setError(cciEx.getMessage());
                attempt.setStatus(MessageAttemptStatus.ERROR);
                // this flag is used in the finally clause
                reliabilityCheckResult = ReliabilityChecker.CheckResult.SEND_FAIL;
                getLog().error("Cannot handle request for message:[{}], Certificate is not valid or it has been revoked ", messageId, cciEx);
                errorLogService.createErrorLog(messageId, ErrorCode.EBMS_0004, cciEx.getMessage(), MSHRole.SENDING, userMessage);
                return;
            }

            getLog().debug("PMode found : [{}]", pModeKey);
            final SOAPMessage requestSoapMessage = createSOAPMessage(userMessage, legConfiguration);

            String receiverUrl = pModeProvider.getReceiverPartyEndpoint(receiverParty, userMessageServiceHelper.getFinalRecipient(userMessage));
            responseSoapMessage = mshDispatcher.dispatch(requestSoapMessage, receiverUrl, policy, legConfiguration, pModeKey);

            requestRawXMLMessage = soapUtil.getRawXMLMessage(requestSoapMessage);
            responseResult = responseHandler.verifyResponse(responseSoapMessage, messageId);

            reliabilityCheckResult = reliabilityChecker.check(requestSoapMessage, responseSoapMessage, responseResult, legConfiguration);
        } catch (final SOAPFaultException soapFEx) {
            getLog().error("A SOAP fault occurred when sending message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), userMessage);
            }
            attempt.setError(soapFEx.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
        } catch (final EbMS3Exception e) {
            getLog().error("EbMS3 exception occurred when sending message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, userMessage);
            attempt.setError(e.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            getLog().error("Error occurred when sending message with ID [{}]", messageId, t);
            attempt.setError(t.getMessage());
            attempt.setStatus(MessageAttemptStatus.ERROR);
        } finally {
            getLog().debug("Finally handle reliability");
            reliabilityService.handleReliability(userMessage, userMessageLog, reliabilityCheckResult, requestRawXMLMessage, responseSoapMessage, responseResult, legConfiguration, attempt);
        }
    }

    protected void validateBeforeSending(final UserMessage userMessage) {
        //can be overridden by child implementations
    }

    protected abstract SOAPMessage createSOAPMessage(final UserMessage userMessage, LegConfiguration legConfiguration) throws EbMS3Exception;

    protected abstract DomibusLogger getLog();
}
