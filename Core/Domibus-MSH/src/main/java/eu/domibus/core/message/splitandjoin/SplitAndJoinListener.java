package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.multitenancy.DomibusDomainException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.io.File;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SplitAndJoinListener implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinListener.class);

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;


    @MDCKey(value = {DomibusLogger.MDC_MESSAGE_ID, DomibusLogger.MDC_MESSAGE_ROLE, DomibusLogger.MDC_MESSAGE_ENTITY_ID}, cleanOnStart = true)
    public void onMessage(final Message message) {
        try {
            LOG.clearCustomKeys();

            String domainCode = null;
            try {
                domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            } catch (final JMSException e) {
                LOG.error("Error processing JMS message", e);
            }
            if (StringUtils.isBlank(domainCode)) {
                LOG.error("Domain is empty: could not send message");
                return;
            }

            try {
                domainContextProvider.setCurrentDomainWithValidation(domainCode);
            } catch (DomibusDomainException ex) {
                LOG.error("Invalid domain: [{}]", domainCode, ex);
                return;
            }

            String messageType = message.getStringProperty(UserMessageService.MSG_TYPE);
            LOG.debug("Processing splitAndJoin message [{}]", messageType);

            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ROLE, MSHRole.RECEIVING.name());
            if (StringUtils.equals(messageType, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN_FILE)) {
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                //for SplitAndJoin the groupId is identical with the SourceMessage id
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, groupId);

                final String backendName = message.getStringProperty(UserMessageService.MSG_BACKEND_NAME);
                final Domain currentDomain = domainContextProvider.getCurrentDomain();
                domainTaskExecutor.submitLongRunningTask(
                        () -> {
                            final File sourceMessageFile = splitAndJoinService.rejoinMessageFragments(groupId);
                            userMessageService.scheduleSourceMessageRejoin(groupId, sourceMessageFile.getAbsolutePath(), backendName);
                        },
                        () -> splitAndJoinService.splitAndJoinReceiveFailed(groupId, groupId, ErrorCode.EbMS3ErrorCode.EBMS_0004.getCode().getErrorCode().getErrorCodeName(), "Error while rejoining the message fragments for group [" + groupId + "]"),
                        currentDomain);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SOURCE_MESSAGE_REJOIN)) {
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                //for SplitAndJoin the groupId is identical with the SourceMessage id
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, groupId);

                final String sourceMessageFile = message.getStringProperty(UserMessageService.MSG_SOURCE_MESSAGE_FILE);
                final String backendName = message.getStringProperty(UserMessageService.MSG_BACKEND_NAME);
                final Domain currentDomain = domainContextProvider.getCurrentDomain();
                domainTaskExecutor.submitLongRunningTask(
                        () -> splitAndJoinService.rejoinSourceMessage(groupId, sourceMessageFile, backendName),
                        () -> splitAndJoinService.splitAndJoinReceiveFailed(groupId, groupId, ErrorCode.EbMS3ErrorCode.EBMS_0004.getCode().getErrorCode().getErrorCodeName(), "Error while rejoining the SourceMessage for group [" + groupId + "]"),
                        currentDomain);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SOURCE_MESSAGE_RECEIPT)) {
                final String sourceMessageId = message.getStringProperty(UserMessageService.MSG_SOURCE_MESSAGE_ID);
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, sourceMessageId);

                final String pModeKey = message.getStringProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);

                splitAndJoinService.sendSourceMessageReceipt(sourceMessageId, pModeKey);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SPLIT_AND_JOIN_SEND_FAILED)) {
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                //for SplitAndJoin the groupId is identical with the SourceMessage id
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, groupId);

                final String errorDetail = message.getStringProperty(UserMessageService.MSG_EBMS3_ERROR_DETAIL);
                splitAndJoinService.splitAndJoinSendFailed(groupId, errorDetail);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SET_MESSAGE_FRAGMENT_AS_FAILED)) {
                final String messageId = message.getStringProperty(UserMessageService.MSG_USER_MESSAGE_ID);
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
                splitAndJoinService.setUserMessageFragmentAsFailed(messageId);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SEND_SIGNAL_ERROR)) {
                final String messageId = message.getStringProperty(UserMessageService.MSG_USER_MESSAGE_ID);
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
                final String ebms3ErrorCode = message.getStringProperty(UserMessageService.MSG_EBMS3_ERROR_CODE);
                final String ebms3ErrorDetail = message.getStringProperty(UserMessageService.MSG_EBMS3_ERROR_DETAIL);
                final String pModeKey = message.getStringProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY);

                splitAndJoinService.sendSignalError(messageId, ebms3ErrorCode, ebms3ErrorDetail, pModeKey);
            } else if (StringUtils.equals(messageType, UserMessageService.COMMAND_SPLIT_AND_JOIN_RECEIVE_FAILED)) {
                final String groupId = message.getStringProperty(UserMessageService.MSG_GROUP_ID);
                final String messageId = message.getStringProperty(UserMessageService.MSG_SOURCE_MESSAGE_ID);
                LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
                final String ebms3ErrorCode = message.getStringProperty(UserMessageService.MSG_EBMS3_ERROR_CODE);
                final String ebms3ErrorDetail = message.getStringProperty(UserMessageService.MSG_EBMS3_ERROR_DETAIL);

                splitAndJoinService.splitAndJoinReceiveFailed(groupId, messageId, ebms3ErrorCode, ebms3ErrorDetail);
            } else {
                LOG.error("Unrecognized message type [{}]", messageType);
            }
        } catch (final JMSException e) {
            LOG.error("Error processing JMS message", e);
        }

    }
}
