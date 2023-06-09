package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.MessageSender;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.message.PartInfoDao;
import eu.domibus.core.message.reliability.ReliabilityChecker;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.lang3.Validate;
import org.apache.cxf.interceptor.Fault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Class responsible for sending SourceMessages on the local endpoint(SplitAndJoin)
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SourceMessageSender implements MessageSender {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SourceMessageSender.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private MessageAttemptService messageAttemptService;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Override
    public void sendMessage(final UserMessage userMessage, final UserMessageLog userMessageLog) {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        domainTaskExecutor.submitLongRunningTask(() -> doSendMessage(userMessage), currentDomain);
    }

    protected void doSendMessage(final UserMessage userMessage) {
        String messageId = userMessage.getMessageId();
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);

        LOG.debug("Sending SourceMessage");

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;

        ReliabilityChecker.CheckResult reliabilityCheck = ReliabilityChecker.CheckResult.SEND_FAIL;
        try {
            final String pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : [{}]", pModeKey);
            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            Party sendingParty = pModeProvider.getSenderParty(pModeKey);
            Validate.notNull(sendingParty, "Initiator party was not found");
            Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            Validate.notNull(receiverParty, "Responder party was not found");

            try {
                messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
                messageExchangeService.verifySenderCertificate(legConfiguration, sendingParty.getName());
            } catch (ChainCertificateInvalidException cciEx) {
                LOG.securityError(DomibusMessageCode.SEC_INVALID_X509CERTIFICATE, cciEx);
                attemptError = cciEx.getMessage();
                attemptStatus = MessageAttemptStatus.ABORT;
                LOG.error("Cannot handle request for message:[{}], Certificate is not valid or it has been revoked ", messageId, cciEx);
                LOG.info("Skipped checking the reliability for message [{}]: message sending has been aborted", messageId);
                return;
            }

            final List<PartInfo> partInfoList = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
            final SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, partInfoList, legConfiguration);
            mshDispatcher.dispatchLocal(userMessage, soapMessage, legConfiguration);
            reliabilityCheck = ReliabilityChecker.CheckResult.OK;
        } catch (final SOAPFaultException soapFEx) {
            LOG.error("A SOAP fault occurred when sending source message with ID [{}]", messageId, soapFEx);
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), userMessage);
            }
            attemptError = soapFEx.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (final EbMS3Exception e) {
            LOG.error("EbMS3 exception occurred when sending source message with ID [{}]", messageId, e);
            reliabilityChecker.handleEbms3Exception(e, userMessage);
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            LOG.error("Error occurred when sending source message with ID [{}]", messageId, t);
            attemptError = t.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw t;
        } finally {
            try {
                if (ReliabilityChecker.CheckResult.SEND_FAIL == reliabilityCheck) {
                    splitAndJoinService.setSourceMessageAsFailed(userMessage);
                }

                attempt.setError(attemptError);
                attempt.setStatus(attemptStatus);
                attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
                attempt.setUserMessageEntityId(userMessage.getEntityId());
                messageAttemptService.create(attempt);

                LOG.debug("Finished sending SourceMessage");
            } catch (Exception ex) {
                LOG.error("Finally exception when marking message as failed", ex);
            }
        }
    }
}
