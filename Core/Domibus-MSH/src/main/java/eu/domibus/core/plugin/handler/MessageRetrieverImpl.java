package eu.domibus.core.plugin.handler;

import eu.domibus.api.messaging.DuplicateMessageFoundException;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.model.UserMessageLog;
import eu.domibus.api.usermessage.UserMessageDownloadEvent;
import eu.domibus.common.ErrorResult;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.core.message.MessagingService;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.message.UserMessageLogDefaultService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service used for retrieving messages (split from DatabaseMessageHandler)
 *
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class MessageRetrieverImpl implements MessageRetriever {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageRetrieverImpl.class);

    protected final UserMessageDefaultService userMessageService;

    private final MessagingService messagingService;

    private final UserMessageLogDefaultService userMessageLogService;

    private final ErrorLogService errorLogService;

    protected final ApplicationEventPublisher applicationEventPublisher;

    public MessageRetrieverImpl(UserMessageDefaultService userMessageService, MessagingService messagingService, UserMessageLogDefaultService userMessageLogService,
                                ErrorLogService errorLogService, ApplicationEventPublisher applicationEventPublisher) {
        this.userMessageService = userMessageService;
        this.messagingService = messagingService;
        this.userMessageLogService = userMessageLogService;
        this.errorLogService = errorLogService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public Submission downloadMessage(String messageId) throws MessageNotFoundException {
        return downloadMessage(messageId, true);
    }

    @Override
    @Transactional
    public Submission downloadMessage(final String messageId, boolean markAsDownloaded) throws MessageNotFoundException {
        LOG.info("Downloading message with id [{}]", messageId);
        final UserMessage userMessage = userMessageService.getByMessageId(messageId, MSHRole.RECEIVING);

        if (markAsDownloaded) {
            markMessageAsDownloaded(userMessage.getMessageId());
        }
        return messagingService.getSubmission(userMessage);
    }

    @Override
    @Transactional
    public Submission downloadMessage(final Long messageEntityId, boolean markAsDownloaded) throws MessageNotFoundException {
        return downloadMessage(messageEntityId);
    }

    @Override
    @Transactional
    public Submission downloadMessage(final Long messageEntityId) throws MessageNotFoundException {
        LOG.info("Downloading message with entity id [{}]", messageEntityId);
        final UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

        markMessageAsDownloaded(userMessage.getMessageId());
        return messagingService.getSubmission(userMessage);
    }

    @Override
    public Submission browseMessage(String messageId) {
        try {
            return browseMessage(messageId, eu.domibus.common.MSHRole.RECEIVING);
        } catch (eu.domibus.api.messaging.MessageNotFoundException ex) {
            LOG.info("Could not find message with id [{}] and RECEIVING role; trying the SENDING role.", messageId);
            return browseMessage(messageId, eu.domibus.common.MSHRole.SENDING);
        }
    }

    @Override
    public Submission browseMessage(String messageId, eu.domibus.common.MSHRole mshRole) throws eu.domibus.api.messaging.MessageNotFoundException {
        LOG.info("Browsing message with id [{}] and role [{}]", messageId, mshRole);

        MSHRole role = MSHRole.valueOf(mshRole.name());
        UserMessage userMessage = userMessageService.getByMessageId(messageId, role);

        return messagingService.getSubmission(userMessage);
    }

    @Override
    public Submission browseMessage(final Long messageEntityId) {
        LOG.info("Browsing message with entity id [{}]", messageEntityId);

        UserMessage userMessage = userMessageService.getByMessageEntityId(messageEntityId);

        return messagingService.getSubmission(userMessage);
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final String messageId) {
        final MessageStatus messageStatus = userMessageLogService.getMessageStatusById(messageId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageId, role);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public eu.domibus.common.MessageStatus getStatus(final Long messageEntityId) {
        final MessageStatus messageStatus = userMessageLogService.getMessageStatus(messageEntityId);
        return eu.domibus.common.MessageStatus.valueOf(messageStatus.name());
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(final String messageId) {
        try {
            userMessageLogService.findByMessageId(messageId);
        } catch (eu.domibus.api.messaging.MessageNotFoundException exception) {
            throw new eu.domibus.api.messaging.MessageNotFoundException(messageId);
        } catch (DuplicateMessageFoundException exception) {
            throw new DuplicateMessageFoundException("Duplicate message found with same message Id. For self sending please call the method with access point role to get the errors of the message." + "[" + messageId + "] ", exception);
        }
        List<ErrorLogEntry> errorsForMessage = errorLogService.getErrorsForMessage(messageId);

        return errorsForMessage.stream().map(errorLogEntry -> errorLogService.convert(errorLogEntry)).collect(Collectors.toList());
    }

    @Override
    public List<? extends ErrorResult> getErrorsForMessage(String messageId, eu.domibus.common.MSHRole mshRole) {
        MSHRole role = MSHRole.valueOf(mshRole.name());
        return errorLogService.getErrors(messageId, role);
    }

    @Override
    public void markMessageAsDownloaded(String messageId) {
        LOG.info("Setting the status of the message with id [{}] to downloaded", messageId);
        final UserMessage userMessage = userMessageService.getByMessageId(messageId, MSHRole.RECEIVING);
        final UserMessageLog messageLog = userMessageLogService.findById(userMessage.getEntityId());
        if (MessageStatus.DOWNLOADED == messageLog.getMessageStatus()) {
            LOG.debug("Message [{}] is already downloaded", userMessage.getMessageId());
        } else {
            MSHRole mshRole = userMessage.getMshRole().getRole();
            publishDownloadEvent(userMessage.getMessageId(), mshRole);
            userMessageLogService.setMessageAsDownloaded(userMessage, messageLog);
        }
    }

    /**
     * Publishes a download event to be caught in case of transaction rollback
     *
     * @param messageId message id of the message that is being downloaded
     * @param role
     */
    protected void publishDownloadEvent(String messageId, MSHRole role) {
        UserMessageDownloadEvent downloadEvent = new UserMessageDownloadEvent();
        downloadEvent.setMessageId(messageId);
        String roleName = role.name();
        downloadEvent.setMshRole(roleName);
        LOG.debug("Publishing [{}] for message [{}] and role [{}]", downloadEvent.getClass().getName(), messageId, roleName);
        applicationEventPublisher.publishEvent(downloadEvent);
    }
}
