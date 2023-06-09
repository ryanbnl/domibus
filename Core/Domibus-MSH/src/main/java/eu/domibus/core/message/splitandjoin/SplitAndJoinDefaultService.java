package eu.domibus.core.message.splitandjoin;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageFragmentType;
import eu.domibus.api.ebms3.model.mf.Ebms3MessageHeaderType;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.messaging.MessageNotFoundException;
import eu.domibus.api.model.*;
import eu.domibus.api.model.splitandjoin.MessageFragmentEntity;
import eu.domibus.api.model.splitandjoin.MessageGroupEntity;
import eu.domibus.api.model.splitandjoin.MessageHeaderEntity;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.core.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.ebms3.sender.retry.UpdateRetryLoggingService;
import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupService;
import eu.domibus.core.ebms3.ws.policy.PolicyService;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.*;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.message.receipt.AS4ReceiptService;
import eu.domibus.core.message.retention.MessageRetentionDefaultService;
import eu.domibus.core.payload.persistence.PayloadPersistence;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorage;
import eu.domibus.core.payload.persistence.filesystem.PayloadFileStorageProvider;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.core.util.MessageUtil;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.http.entity.ContentType;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SplitAndJoinDefaultService implements SplitAndJoinService {

    private static final Long MB_IN_BYTES = 1048576L;
    public static final String BOUNDARY = "boundary";
    public static final String START = "start";
    public static final String FRAGMENT_FILENAME_SEPARATOR = "_";

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinDefaultService.class);
    public static final String ERROR_MESSAGE_GROUP_HAS_EXPIRED = "Group has expired";
    public static final String ERROR_GENERATING_THE_SIGNAL_SOAPMESSAGE_FOR_SOURCE_MESSAGE = "Error generating the Signal SOAPMessage for SourceMessage [";

    @Autowired
    protected MessageFragmentDao messageFragmentDao;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected PayloadFileStorageProvider storageProvider;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected UserMessageDefaultService userMessageDefaultService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected AttachmentCleanupService attachmentCleanupService;

    @Autowired
    protected UserMessagePayloadService userMessagePayloadService;

    @Autowired
    protected MessagingService messagingService;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected IncomingSourceMessageHandler incomingSourceMessageHandler;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Autowired
    protected AS4ReceiptService as4ReceiptService;

    @Autowired
    protected EbMS3MessageBuilder messageBuilder;

    @Autowired
    protected MessageRetentionDefaultService messageRetentionService;

    @Autowired
    protected MessageGroupService messageGroupService;

    @Autowired
    protected ErrorLogService errorLogService;

    @Autowired
    protected Ebms3Converter ebms3Converter;

    @Autowired
    protected PartInfoDao partInfoDao;

    @Autowired
    protected MshRoleDao mshRoleDao;

    @Autowired
    protected UserMessageDao userMessageDao;

    @Autowired
    SplitAndJoinHelper splitAndJoinHelper;

    @Override
    public void createUserFragmentsFromSourceFile(String sourceMessageFileName, SOAPMessage sourceMessageRequest, UserMessage userMessage, String contentTypeString, boolean compression) {
        MessageGroupEntity messageGroupEntity = new MessageGroupEntity();

        MSHRoleEntity mshRoleEntity = mshRoleDao.findOrCreate(MSHRole.SENDING);
        messageGroupEntity.setMshRole(mshRoleEntity);
        messageGroupEntity.setGroupId(userMessage.getMessageId());
        File sourceMessageFile = new File(sourceMessageFileName);
        messageGroupEntity.setMessageSize(BigInteger.valueOf(sourceMessageFile.length()));
        if (compression) {
            final File compressSourceMessage = compressSourceMessage(sourceMessageFileName);
            LOG.debug("Deleting file [{}]", sourceMessageFile);
            final boolean sourceDeleteSuccessful = sourceMessageFile.delete();
            if (!sourceDeleteSuccessful) {
                LOG.warn("Could not delete uncompressed source file [{}]", sourceMessageFile);
            }

            LOG.debug("Using [{}] as source message file ", compressSourceMessage);
            sourceMessageFile = compressSourceMessage;
            messageGroupEntity.setCompressedMessageSize(BigInteger.valueOf(compressSourceMessage.length()));
            messageGroupEntity.setCompressionAlgorithm(MessageConstants.COMPRESSION_PROPERTY_VALUE);
        }

        messageGroupEntity.setSoapAction(StringUtils.EMPTY);
        final UserMessage dbUserMessage = userMessageDao.findByMessageId(userMessage.getMessageId());
        messageGroupEntity.setSourceMessage(dbUserMessage);

        MessageExchangeConfiguration userMessageExchangeConfiguration;
        LegConfiguration legConfiguration;
        try {
            userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Could not get LegConfiguration", e);
        }

        List<String> fragmentFiles;
        try {
            fragmentFiles = splitSourceMessage(sourceMessageFile, legConfiguration.getSplitting().getFragmentSize());
        } catch (IOException e) {
            throw new SplitAndJoinException("Could not split SourceMessage " + sourceMessageFileName, e);
        }
        messageGroupEntity.setFragmentCount(Long.valueOf(fragmentFiles.size()));
        LOG.debug("Deleting source file [{}]", sourceMessageFile);
        final boolean deleteSuccessful = sourceMessageFile.delete();
        if (!deleteSuccessful) {
            LOG.warn("Could not delete source file [{}]", sourceMessageFile);
        }
        LOG.debug("Finished deleting source file [{}]", sourceMessageFile);

        final ContentType contentType = ContentType.parse(contentTypeString);
        MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
        messageHeaderEntity.setBoundary(contentType.getParameter(BOUNDARY));
        final String start = contentType.getParameter(START);
        messageHeaderEntity.setStart(StringUtils.replaceEach(start, new String[]{"<", ">"}, new String[]{"", ""}));
        messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);

        splitAndJoinHelper.createMessageFragments(dbUserMessage, messageGroupEntity, fragmentFiles);

        attachmentCleanupService.cleanAttachments(sourceMessageRequest);

        LOG.debug("Finished processing source message file");
    }

    @Override
    public void rejoinSourceMessage(String groupId, String sourceMessageFile, String backendName) {
        LOG.debug("Rejoining SourceMessage for group [{}]", groupId);

        final SOAPMessage sourceRequest = rejoinSourceMessage(groupId, new File(sourceMessageFile));
        Ebms3Messaging ebms3Messaging = messageUtil.getMessage(sourceRequest);
        UserMessage userMessage = ebms3Converter.convertFromEbms3(ebms3Messaging.getUserMessage());
        userMessage.setSourceMessage(true);

        MessageExchangeConfiguration userMessageExchangeContext;
        LegConfiguration legConfiguration;
        try {
            userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.RECEIVING);
            String sourcePmodeKey = userMessageExchangeContext.getPmodeKey();
            legConfiguration = pModeProvider.getLegConfiguration(sourcePmodeKey);
            sourceRequest.setProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, sourcePmodeKey);
        } catch (EbMS3Exception | SOAPException e) {
            throw new SplitAndJoinException("Error getting the pmodeKey", e);
        }

        incomingSourceMessageHandler.processMessage(sourceRequest, ebms3Messaging);

        final String sourceMessageId = userMessage.getMessageId();
        messageGroupService.setSourceMessageId(sourceMessageId, groupId);

        userMessageService.scheduleSourceMessageReceipt(sourceMessageId, userMessageExchangeContext.getReversePmodeKey());

        LOG.debug("Finished rejoining SourceMessage for group [{}]", groupId);
    }

    @Override
    public void sendSourceMessageReceipt(String sourceMessageId, String pModeKey) {
        SOAPMessage receiptMessage;
        try {
            receiptMessage = as4ReceiptService.generateReceipt(sourceMessageId, MSHRole.RECEIVING, false);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error generating the source message receipt", e);
        }
        sendSignalMessage(receiptMessage, pModeKey);

    }

    @Override
    public void sendSignalError(String messageId, String ebMS3ErrorCode, String errorDetail, String pmodeKey) {
        final ErrorCode.EbMS3ErrorCode errorCode = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(ebMS3ErrorCode);

        EbMS3Exception ebMS3Exception = EbMS3ExceptionBuilder.getInstance()
                .ebMS3ErrorCode(errorCode)
                .message(errorDetail)
                .refToMessageId(messageId)
                .mshRole(MSHRole.RECEIVING)
                .build();

        SOAPMessage soapMessage;
        try {
            soapMessage = messageBuilder.buildSOAPFaultMessage(ebMS3Exception.getFaultInfoError());
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException(ERROR_GENERATING_THE_SIGNAL_SOAPMESSAGE_FOR_SOURCE_MESSAGE + messageId + "]", e);
        }
        sendSignalMessage(soapMessage, pmodeKey);
    }

    protected void sendSignalMessage(SOAPMessage soapMessage, String pModeKey) {
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
        final Policy policy = policyService.getPolicy(legConfiguration);

        try {
            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error dispatching SourceMessage receipt", e);
        }
    }

    @Override
    public String generateSourceFileName(String temporaryDirectoryLocation) {
        final String uuid = UUID.randomUUID().toString();
        return temporaryDirectoryLocation + "/" + uuid;
    }

    @Override
    public File rejoinMessageFragments(String groupId) {
        LOG.debug("Rejoining the SourceMessage for group [{}]", groupId);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            throw new SplitAndJoinException("Could not rejoin fragments: could not find group [" + groupId + "]");
        }


        final List<UserMessage> userMessageFragments = userMessageDao.findUserMessageByGroupId(groupId);

        if (messageGroupEntity.getFragmentCount() != userMessageFragments.size()) {
            throw new SplitAndJoinException("Could not rejoin fragments: number of fragments found [" + userMessageFragments.size() + "] do not correspond with the total fragment count [" + messageGroupEntity.getFragmentCount() + "]");
        }


        List<File> fragmentFilesInOrder = new ArrayList<>();
        for (UserMessage userMessage : userMessageFragments) {
            List<PartInfo> partInfos = partInfoDao.findPartInfoByUserMessageEntityId(userMessage.getEntityId());
            final PartInfo partInfo = partInfos.iterator().next();
            final String fileName = partInfo.getFileName();
            if (StringUtils.isBlank(fileName)) {
                throw new SplitAndJoinException("Could not rejoin fragments: filename is null for part [" + partInfo.getHref() + "]");
            }
            fragmentFilesInOrder.add(new File(fileName));
        }

        final File sourceMessageFile = mergeSourceFile(fragmentFilesInOrder, messageGroupEntity);
        LOG.debug("Rejoined the SourceMessage for group [{}] into file [{}] of length [{}]", groupId, sourceMessageFile, sourceMessageFile.length());

        return sourceMessageFile;
    }

    protected SOAPMessage rejoinSourceMessage(String groupId, File sourceMessageFile) {
        LOG.debug("Creating the SOAPMessage for group [{}] from file [{}] ", groupId, sourceMessageFile);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupIdWithMessageHeader(groupId);
        final String contentType = createContentType(messageGroupEntity.getMessageHeaderEntity().getBoundary(), messageGroupEntity.getMessageHeaderEntity().getStart());

        return getUserMessage(sourceMessageFile, contentType);
    }

    public SOAPMessage getUserMessage(File sourceMessageFileName, String contentTypeString) {
        LOG.debug("Parsing the SOAPMessage from file [{}]", sourceMessageFileName);

        try (InputStream rawInputStream = new FileInputStream(sourceMessageFileName)) {
            MessageImpl messageImpl = new MessageImpl();
            final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(PayloadFileStorage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
            LOG.debug("Using temporaryDirectoryLocation for attachments [{}]", temporaryDirectoryLocation);
            messageImpl.put(AttachmentDeserializer.ATTACHMENT_DIRECTORY, temporaryDirectoryLocation);
            messageImpl.setContent(InputStream.class, rawInputStream);
            messageImpl.put(Message.CONTENT_TYPE, contentTypeString);

            LOG.debug("Start initializeAttachments");
            new AttachmentDeserializer(messageImpl).initializeAttachments();
            LOG.debug("End initializeAttachments");

            LOG.debug("Start createUserMessage");
            final SOAPMessage soapMessage = soapUtil.createUserMessage(messageImpl);
            LOG.debug("End createUserMessage");

            return soapMessage;
        } catch (Exception e) {
            throw new SplitAndJoinException(e);
        }
    }

    @Transactional
    @Override
    public void setSourceMessageAsFailed(UserMessage userMessage) {
        updateRetryLoggingService.setSourceMessageAsFailed(userMessage);
    }

    @Override
    public void setUserMessageFragmentAsFailed(String messageId) {
        LOG.debug("Setting the UserMessage fragment [{}] as failed", messageId);

        final UserMessage userMessage = userMessageDao.findByMessageId(messageId, MSHRole.RECEIVING);
        if (userMessage == null) {
            LOG.error("UserMessage not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }

        final UserMessageLog messageLog = userMessageLogDao.findByMessageIdSafely(messageId, MSHRole.RECEIVING);
        if (messageLog == null) {
            LOG.error("UserMessageLogEntity not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }
        final MessageStatus messageStatus = messageLog.getMessageStatus();
        if (MessageStatus.ACKNOWLEDGED == messageStatus || MessageStatus.SEND_FAILURE == messageStatus) {
            LOG.debug("UserMessage fragment [{}] was not scheduled to be marked as failed: status is [{}]", messageId, messageStatus);
            return;
        }

        updateRetryLoggingService.messageFailed(userMessage, messageLog);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void handleSourceMessageSignalError(String messageId) {
        LOG.debug("SplitAndJoin handleSourceMessageSignalError for message [{}]", messageId);

        sendSplitAndJoinFailed(messageId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void handleExpiredGroups() {
        handleExpiredReceivedGroups();
        handleExpiredSendGroups();
    }

    protected void handleExpiredSendGroups() {
        LOG.trace("Handling expired send groups");

        final List<MessageGroupEntity> sendNonExpiredOrRejected = messageGroupDao.findOngoingSendNonExpiredOrRejected();
        final List<MessageGroupEntity> sendExpiredGroups = getSendExpiredGroups(sendNonExpiredOrRejected);

        if (CollectionUtils.isEmpty(sendExpiredGroups)) {
            LOG.trace("No send expired groups found");
            return;
        }
        LOG.debug("Found send expired groups [{}]", sendExpiredGroups);
        sendExpiredGroups.forEach(this::setSendGroupAsExpired);

        LOG.trace("Finished handling expired send groups");
    }

    protected List<MessageGroupEntity> getSendExpiredGroups(final List<MessageGroupEntity> sendNonExpiredOrRejected) {
        return sendNonExpiredOrRejected.stream().filter(this::isSendGroupExpired).collect(Collectors.toList());
    }

    protected void setSendGroupAsExpired(MessageGroupEntity messageGroupEntity) {
        LOG.debug("Setting the group [{}] as expired", messageGroupEntity.getGroupId());
        messageGroupEntity.setExpired(true);
        messageGroupDao.update(messageGroupEntity);
        userMessageService.scheduleSplitAndJoinSendFailed(messageGroupEntity.getGroupId(), String.format("Message group [%s] has expired", messageGroupEntity.getGroupId()));
    }


    protected void handleExpiredReceivedGroups() {
        LOG.trace("Handling expired received groups");

        final List<MessageGroupEntity> receivedNonExpiredOrRejected = messageGroupDao.findOngoingReceivedNonExpiredOrRejected();
        final List<MessageGroupEntity> receivedExpiredGroups = getReceivedExpiredGroups(receivedNonExpiredOrRejected);

        if (CollectionUtils.isEmpty(receivedExpiredGroups)) {
            LOG.trace("No received expired groups found");
            return;
        }
        LOG.debug("Found received expired groups [{}]", receivedExpiredGroups);
        receivedExpiredGroups.forEach(this::setReceivedGroupAsExpired);

        LOG.trace("Finished handling expired received groups");
    }


    protected void setReceivedGroupAsExpired(MessageGroupEntity messageGroupEntity) {
        messageGroupEntity.setExpired(true);
        messageGroupDao.update(messageGroupEntity);
        UserMessage sourceMessage = userMessageDao.findByGroupEntityId(messageGroupEntity.getEntityId());
        userMessageService.scheduleSplitAndJoinReceiveFailed(messageGroupEntity.getGroupId(), sourceMessage.getMessageId(), ErrorCode.EbMS3ErrorCode.EBMS_0051.getCode().getErrorCode().getErrorCodeName(), ERROR_MESSAGE_GROUP_HAS_EXPIRED);
    }

    protected List<MessageGroupEntity> getReceivedExpiredGroups(final List<MessageGroupEntity> receivedNonExpiredOrRejected) {
        return receivedNonExpiredOrRejected.stream().filter(this::isReceivedGroupExpired).collect(Collectors.toList());
    }

    protected boolean isSendGroupExpired(MessageGroupEntity messageGroupEntity) {
        final UserMessage sourceUserMessage = userMessageDao.findByGroupEntityId(messageGroupEntity.getEntityId());
        return isGroupExpired(sourceUserMessage, messageGroupEntity.getGroupId());
    }

    protected boolean isGroupExpired(final UserMessage userMessage, String groupId) {
        LegConfiguration legConfiguration;
        try {
            MessageExchangeConfiguration userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String sourcePmodeKey = userMessageExchangeContext.getPmodeKey();
            legConfiguration = pModeProvider.getLegConfiguration(sourcePmodeKey);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error getting the pmodeKey", e);
        }
        if (legConfiguration.getSplitting() == null) {
            LOG.debug("Could no find Splitting configuration");
            return false;
        }

        final long userMessageEntityId = userMessage.getEntityId();
        //in minutes
        final int joinInterval = legConfiguration.getSplitting().getJoinInterval();
        final UserMessageLog userMessageLog = userMessageLogDao.findByEntityId(userMessageEntityId);
        if (userMessageLog == null) {
            throw new MessageNotFoundException(userMessageEntityId);
        }
        final boolean messageExpired = isMessageExpired(userMessage.getMessageId(), userMessageLog.getReceived(), joinInterval);
        if (messageExpired) {
            LOG.debug("Message group [{}] is expired", groupId);
            return true;
        }
        return false;
    }

    protected boolean isMessageExpired(final String messageId, final Date messageCreationDate, final int joinInterval) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime messageCreationTime = new Timestamp(messageCreationDate.getTime()).toLocalDateTime();

        LOG.debug("Checking if the (current time [{}] - message [{}] creationTime  time [{}]) > join interval [{}]", now, messageId, messageCreationTime, joinInterval);
        if (Duration.between(messageCreationTime, now).toMinutes() > joinInterval) {
            LOG.debug("Message [{}] creationTime [{}] is > join interval [{}]", messageId, messageCreationTime, joinInterval);
            return true;
        }
        return false;
    }

    protected boolean isReceivedGroupExpired(MessageGroupEntity messageGroupEntity) {
        final String groupId = messageGroupEntity.getGroupId();
        final List<UserMessage> fragments = userMessageDao.findUserMessageByGroupId(groupId);
        if (CollectionUtils.isEmpty(fragments)) {
            LOG.debug("No fragments found for group [{}]", groupId);
            return false;
        }

        fragments.sort(Comparator.comparing(UserMessage::getTimestamp));
        final UserMessage firstFragment = fragments.get(0);
        return isGroupExpired(firstFragment, groupId);
    }

    @Transactional
    @Override
    public void splitAndJoinSendFailed(final String groupId, final String errorDetail) {
        LOG.debug("SplitAndJoin send failed for group [{}]", groupId);

        sendSplitAndJoinFailed(groupId);

        final List<UserMessage> groupUserMessages = userMessageDao.findUserMessageByGroupId(groupId);
        groupUserMessages.forEach(userMessage ->
                userMessageService.scheduleSetUserMessageFragmentAsFailed(userMessage.getMessageId(), userMessage.getMshRole().getRole()));

        LOG.debug("Creating error entry for message [{}]", groupId);
        errorLogService.createErrorLog(groupId, ErrorCode.EBMS_0004, "[SPLIT] " + errorDetail, MSHRole.SENDING, groupUserMessages.stream().findAny().orElse(null));
    }


    protected void sendSplitAndJoinFailed(final String groupId) {
        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            LOG.warn("Group not found [{}]: could't clear SplitAndJoin messages for group", groupId);
            return;
        }

        if (BooleanUtils.isTrue(messageGroupEntity.getRejected())) {
            LOG.debug("The group [{}] is already marked as rejected", groupId);
            return;
        }

        LOG.debug("Marking the group [{}] as rejected", groupId);
        messageGroupEntity.setRejected(true);
        messageGroupDao.update(messageGroupEntity);

        final UserMessage sourceUserMessage = userMessageDao.findByGroupEntityId(messageGroupEntity.getEntityId());
        setSourceMessageAsFailed(sourceUserMessage);
    }

    @Transactional
    @Override
    public void splitAndJoinReceiveFailed(final String groupId, final String sourceMessageId, final String ebMS3ErrorCode, final String errorDetail) {
        LOG.debug("SplitAndJoin receiving failed for group [{}]", groupId);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            LOG.warn("Group not found [{}]: could not handle splitAndJoinReceiveFailed", groupId);
            return;
        }

        LOG.debug("Marking the group [{}] as rejected", groupId);
        messageGroupEntity.setRejected(true);
        messageGroupDao.update(messageGroupEntity);

        final List<UserMessage> userMessageFragments = userMessageDao.findUserMessageByGroupId(groupId);
        if (userMessageFragments == null || userMessageFragments.isEmpty()) {
            throw new SplitAndJoinException(ERROR_GENERATING_THE_SIGNAL_SOAPMESSAGE_FOR_SOURCE_MESSAGE + sourceMessageId + "]: no message fragments found");
        }

        messageRetentionService.scheduleDeleteMessages(userMessageFragments);

        if (StringUtils.isNotEmpty(sourceMessageId)) {
            LOG.debug("Scheduling sending the Signal error for SourceMessage [{}]", sourceMessageId);

            final UserMessage messageFragment = userMessageFragments.iterator().next();
            MessageExchangeConfiguration userMessageExchangeContext;
            try {
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(messageFragment, MSHRole.RECEIVING);
            } catch (EbMS3Exception e) {
                throw new SplitAndJoinException(ERROR_GENERATING_THE_SIGNAL_SOAPMESSAGE_FOR_SOURCE_MESSAGE + sourceMessageId + "]: could not get the MessageExchangeConfiguration", e);
            }
            userMessageDefaultService.scheduleSendingSignalError(sourceMessageId, ebMS3ErrorCode, errorDetail, userMessageExchangeContext.getReversePmodeKey());
        }
    }

    @Override
    public synchronized void incrementSentFragments(String groupId) {
        LOG.debug("Incrementing the sentFragments count for group [{}]", groupId);

        final MessageGroupEntity groupEntity = messageGroupDao.findByGroupId(groupId);
        groupEntity.incrementSentFragments();
        messageGroupDao.update(groupEntity);
        LOG.debug("Sent fragments [{}] out of [{}] for group [{}]", groupEntity.getSentFragments(), groupEntity.getFragmentCount(), groupId);
    }

    @Override
    public synchronized void incrementReceivedFragments(String groupId, String backendName) {
        LOG.debug("Incrementing receivedFragments count for group [{}]", groupId);

        final MessageGroupEntity groupEntity = messageGroupDao.findByGroupId(groupId);
        groupEntity.incrementReceivedFragments();
        messageGroupDao.update(groupEntity);

        LOG.debug("Received fragments [{}] out of expected [{}] for group [{}]", groupEntity.getReceivedFragments(), groupEntity.getFragmentCount(), groupEntity.getGroupId());

        if (groupEntity.getReceivedFragments().equals(groupEntity.getFragmentCount())) {
            LOG.info("All fragment files received for group [{}], scheduling the source message rejoin", groupEntity.getGroupId());

            userMessageService.scheduleSourceMessageRejoinFile(groupEntity.getGroupId(), backendName);
        }
    }

    protected File compressSourceMessage(String fileName) {
        String compressedFileName = fileName + ".zip";
        LOG.debug("Compressing the source message file [{}] to [{}]", fileName, compressedFileName);
        try (GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(compressedFileName)));
             FileInputStream sourceMessageInputStream = new FileInputStream(fileName)) {
            byte[] buffer = new byte[PayloadPersistence.DEFAULT_BUFFER_SIZE];
            int len;
            while ((len = sourceMessageInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            LOG.error("Could not compress the message content to file " + fileName);
            throw new Fault(e);
        }
        LOG.debug("Finished compressing the source message file [{}] to [{}]", fileName, compressedFileName);
        return new File(compressedFileName);
    }


    protected List<String> splitSourceMessage(File sourceMessageFile, int fragmentSizeInMB) throws IOException {
        LOG.debug("Source file [{}] will be split into fragments", sourceMessageFile);

        final long sourceSize = sourceMessageFile.length();
        long fragmentSizeInBytes = fragmentSizeInMB * MB_IN_BYTES;

        long bytesPerSplit;
        long fragmentCount = 1;
        long remainingBytes = 0;
        if (sourceSize > fragmentSizeInBytes) {
            fragmentCount = sourceSize / fragmentSizeInBytes;
            bytesPerSplit = fragmentSizeInBytes;

            if (fragmentCount > 0) {
                remainingBytes = sourceSize % (fragmentCount * fragmentSizeInBytes);
            }
        } else {
            bytesPerSplit = sourceSize;
        }
        final File storageDirectory = getFragmentStorageDirectory();
        return splitSourceFileIntoFragments(sourceMessageFile, storageDirectory, fragmentCount, bytesPerSplit, remainingBytes);
    }

    protected File getFragmentStorageDirectory() {
        final PayloadFileStorage currentStorage = storageProvider.getCurrentStorage();
        if (currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not store fragment payload. Please configure " + PayloadFileStorage.ATTACHMENT_STORAGE_LOCATION + " when using SplitAndJoin");
        }
        return currentStorage.getStorageDirectory();
    }

    protected List<String> splitSourceFileIntoFragments(File sourceMessageFile, File storageDirectory, long fragmentCount, long bytesPerSplit, long remainingBytes) throws IOException {
        List<String> result = new ArrayList<>();

        LOG.debug("Splitting SourceMessage [{}] into [{}] fragments, bytesPerSplit [{}], remainingBytes [{}]", sourceMessageFile, fragmentCount, bytesPerSplit, remainingBytes);

        int maxReadBufferSize = PayloadPersistence.DEFAULT_BUFFER_SIZE;
        try (RandomAccessFile raf = new RandomAccessFile(sourceMessageFile, "r")) {
            for (int index = 1; index <= fragmentCount; index++) {
                final String fragmentFileName = getFragmentFileName(storageDirectory, sourceMessageFile.getName(), index);
                result.add(fragmentFileName);
                saveFragmentPayload(bytesPerSplit, maxReadBufferSize, raf, fragmentFileName);
            }
            if (remainingBytes > 0) {
                final String remainingFragmentFileName = getFragmentFileName(storageDirectory, sourceMessageFile.getName(), (fragmentCount + 1));
                result.add(remainingFragmentFileName);

                try (final FileOutputStream outputStream = new FileOutputStream(remainingFragmentFileName);
                     final BufferedOutputStream bw = new BufferedOutputStream(outputStream)) {
                    copyToOutputStream(raf, bw, remainingBytes);
                }
            }
        }
        return result;
    }

    protected void saveFragmentPayload(long bytesPerSplit, int maxReadBufferSize, RandomAccessFile raf, final String fragmentFileName) throws IOException {
        LOG.debug("Saving fragment file [{}]", fragmentFileName);

        try (final FileOutputStream fileOutputStream = new FileOutputStream(fragmentFileName);
             BufferedOutputStream bw = new BufferedOutputStream(fileOutputStream)) {
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int index = 0; index < numReads; index++) {
                    copyToOutputStream(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    copyToOutputStream(raf, bw, numRemainingRead);
                }
            } else {
                copyToOutputStream(raf, bw, bytesPerSplit);
            }
        }
    }

    protected void copyToOutputStream(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }

    protected String getFragmentFileName(File outputDirectory, String sourceFileName, long fragmentNumber) {
        return outputDirectory.getAbsolutePath() + File.separator + sourceFileName + FRAGMENT_FILENAME_SEPARATOR + fragmentNumber;
    }

    protected String createContentType(String boundary, String start) {
        final String contentType = "multipart/related; type=\"application/soap+xml\"; boundary=" + boundary + "; start=" + start + "; start-info=\"application/soap+xml\"";
        LOG.debug("Created contentType [{}]", contentType);
        return contentType;
    }

    protected File mergeSourceFile(List<File> fragmentFilesInOrder, MessageGroupEntity messageGroupEntity) {
        final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(PayloadFileStorage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isEmpty(temporaryDirectoryLocation)) {
            throw new SplitAndJoinException("Could not rejoin fragments: the property [" + PayloadFileStorage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION + "] is not defined");
        }
        String sourceFileName = generateSourceFileName(temporaryDirectoryLocation);
        String outputFileName = sourceFileName;
        final boolean sourceMessageCompressed = isSourceMessageCompressed(messageGroupEntity);
        if (sourceMessageCompressed) {
            outputFileName = sourceFileName + "_compressed";
        }

        final File outputFile = new File(outputFileName);

        LOG.debug("Merging files [{}] for group [{}] into file [{}]", fragmentFilesInOrder, messageGroupEntity.getGroupId(), outputFile);

        try (OutputStream mergingStream = new FileOutputStream(outputFile)) {
            mergeFiles(fragmentFilesInOrder, mergingStream);
        } catch (IOException exp) {
            throw new SplitAndJoinException("Could not rejoin fragments", exp);
        }
        if (!sourceMessageCompressed) {
            return outputFile;
        }

        final File decompressedSourceFile = new File(sourceFileName);
        try {
            LOG.debug("Decompressing SourceMessage file [{}] into file [{}]", outputFile, decompressedSourceFile);
            decompressGzip(outputFile, decompressedSourceFile);
            LOG.debug("Deleting file [{}]", outputFile);
            final boolean delete = outputFile.delete();
            if (!delete) {
                LOG.warn("Could not delete file [{}]", outputFile);
            }
        } catch (IOException exp) {
            throw new SplitAndJoinException("Could not rejoin fragments", exp);
        }
        return decompressedSourceFile;
    }

    protected boolean isSourceMessageCompressed(MessageGroupEntity messageGroupEntity) {
        return StringUtils.isNotBlank(messageGroupEntity.getCompressionAlgorithm());
    }

    protected void mergeFiles(List<File> files, OutputStream mergingStream) throws IOException {
        for (File f : files) {
            Files.copy(f.toPath(), mergingStream);
            mergingStream.flush();
        }
    }

    protected void decompressGzip(File input, File output) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(input))) {
            try (FileOutputStream out = new FileOutputStream(output)) {
                IOUtils.copy(in, out, PayloadPersistence.DEFAULT_BUFFER_SIZE);
            }
        }
    }


    @Transactional
    @Override
    public void persistReceivedUserFragment(UserMessage userMessage, Ebms3MessageFragmentType ebms3MessageFragmentType, LegConfiguration legConfiguration) throws EbMS3Exception {
        MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(ebms3MessageFragmentType.getGroupId());

        if (messageGroupEntity == null) {
            LOG.debug("Creating messageGroupEntity");

            messageGroupEntity = new MessageGroupEntity();
            MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
            final Ebms3MessageHeaderType messageHeader = ebms3MessageFragmentType.getMessageHeader();
            messageHeaderEntity.setStart(messageHeader.getStart());
            messageHeaderEntity.setBoundary(messageHeader.getBoundary());

            MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.RECEIVING);
            messageGroupEntity.setMshRole(role);
            messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);
            messageGroupEntity.setSoapAction(ebms3MessageFragmentType.getAction());
            messageGroupEntity.setCompressionAlgorithm(ebms3MessageFragmentType.getCompressionAlgorithm());
            messageGroupEntity.setMessageSize(ebms3MessageFragmentType.getMessageSize());
            messageGroupEntity.setCompressedMessageSize(ebms3MessageFragmentType.getCompressedMessageSize());
            messageGroupEntity.setGroupId(ebms3MessageFragmentType.getGroupId());
            messageGroupEntity.setFragmentCount(ebms3MessageFragmentType.getFragmentCount());
            messageGroupDao.create(messageGroupEntity);
        }

        validateUserMessageFragment(userMessage, messageGroupEntity, ebms3MessageFragmentType, legConfiguration);

        MessageFragmentEntity messageFragmentEntity = new MessageFragmentEntity();
        messageFragmentEntity.setUserMessage(userMessageDao.findByReference(userMessage.getEntityId()));
        messageFragmentEntity.setGroup(messageGroupEntity);
        messageFragmentEntity.setFragmentNumber(ebms3MessageFragmentType.getFragmentNum());
        messageFragmentDao.create(messageFragmentEntity);
    }

    protected void validateUserMessageFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, Ebms3MessageFragmentType ebms3MessageFragmentType, final LegConfiguration legConfiguration) throws EbMS3Exception {
        if (legConfiguration.getSplitting() == null) {
            LOG.error("No splitting configuration found on leg [{}]", legConfiguration.getName());
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                    .message("No splitting configuration found")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        if (storageProvider.isPayloadsPersistenceInDatabaseConfigured()) {
            LOG.error("SplitAndJoin feature works only with payload storage configured on the file system");
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0002)
                    .message("SplitAndJoin feature needs payload storage on the file system")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }

        final String groupId = ebms3MessageFragmentType.getGroupId();
        if (messageGroupEntity == null) {
            LOG.warn("Could not validate UserMessage fragment [[{}] for group [{}]: messageGroupEntity is null", userMessage.getMessageId(), groupId);
            return;
        }
        if (isTrue(messageGroupEntity.getExpired())) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0051)
                    .message("More time than Pmode[].Splitting.JoinInterval has passed since the first fragment was received but not all other fragments are received")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
        if (isTrue(messageGroupEntity.getRejected())) {
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0040)
                    .message("A fragment is received that relates to a group that was previously rejected")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
        final Long fragmentCount = messageGroupEntity.getFragmentCount();
        if (fragmentCount != null && ebms3MessageFragmentType.getFragmentCount() != null && ebms3MessageFragmentType.getFragmentCount() > fragmentCount) {
            LOG.error("An incoming message fragment has a value greater than the known FragmentCount for group [{}]", groupId);
            throw EbMS3ExceptionBuilder.getInstance()
                    .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0048)
                    .message("An incoming message fragment has a a value greater than the known FragmentCount")
                    .refToMessageId(userMessage.getMessageId())
                    .mshRole(MSHRole.RECEIVING)
                    .build();
        }
    }
}
