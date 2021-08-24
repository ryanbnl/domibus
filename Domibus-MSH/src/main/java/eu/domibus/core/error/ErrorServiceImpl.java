package eu.domibus.core.error;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MSHRoleEntity;
import eu.domibus.api.model.UserMessage;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.message.dictionary.MshRoleDao;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS;

/**
 * @author Thomas Dussart
 * @since 3.3
 * <p>
 * Service in charge or persisting errors.
 */
@Service
public class ErrorServiceImpl implements ErrorService {

    protected ErrorLogDao errorLogDao;
    protected DomibusPropertyProvider domibusPropertyProvider;
    protected MshRoleDao mshRoleDao;
    private ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil;

    public ErrorServiceImpl(ErrorLogDao errorLogDao,
                            DomibusPropertyProvider domibusPropertyProvider,
                            MshRoleDao mshRoleDao,
                            ErrorLogEntryTruncateUtil errorLogEntryTruncateUtil) {
        this.errorLogDao = errorLogDao;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.mshRoleDao = mshRoleDao;
        this.errorLogEntryTruncateUtil = errorLogEntryTruncateUtil;
    }
    
    public void create(ErrorLogEntry errorLogEntry) {
        errorLogEntryTruncateUtil.truncate(errorLogEntry);
        if(errorLogEntry.getUserMessage() == null) {
            UserMessage um = new UserMessage();
            um.setEntityId(UserMessage.DEFAULT_USER_MESSAGE_ID_PK);
            errorLogEntry.setUserMessage(um);
        }
        errorLogDao.create(errorLogEntry);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLogSending(EbMS3Exception exception, UserMessage userMessage) {
        ErrorLogEntry errorLogEntry = new ErrorLogEntry(exception);
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);
        errorLogEntry.setMshRole(role);
        errorLogEntry.setUserMessage(userMessage);
        create(errorLogEntry);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLog(Ebms3Messaging ebms3Messaging, MSHRole mshRole) {
        ErrorLogEntry errorLogEntry = ErrorLogEntry.parse(ebms3Messaging);
        MSHRoleEntity role = mshRoleDao.findOrCreate(mshRole);
        errorLogEntry.setMshRole(role);
        create(errorLogEntry);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLogSending(String messageInErrorId, ErrorCode errorCode, String errorDetail, UserMessage userMessage) {
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);
        final ErrorLogEntry errorLogEntry = new ErrorLogEntry(role, messageInErrorId, errorCode, errorDetail);
        errorLogEntry.setUserMessage(userMessage);
        create(errorLogEntry);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createErrorLog(EbMS3Exception exception, MSHRole mshRole) {
        ErrorLogEntry errorLogEntry = new ErrorLogEntry(exception);
        MSHRoleEntity role = mshRoleDao.findOrCreate(MSHRole.SENDING);
        errorLogEntry.setMshRole(role);
        create(errorLogEntry);
    }

    @Override
    public void deleteErrorLogWithoutMessageIds() {
        int days = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_OLDER_DAYS);
        int batchSize = domibusPropertyProvider.getIntegerProperty(DOMIBUS_ERRORLOG_CLEANER_BATCH_SIZE);

        errorLogDao.deleteErrorLogsWithoutMessageIdOlderThan(days, batchSize);
    }

    @Timer(clazz = ErrorServiceImpl.class, value = "deleteMessages.deleteErrorLogsByMessageIdInError")
    @Counter(clazz = ErrorServiceImpl.class, value = "deleteMessages.deleteErrorLogsByMessageIdInError")
    @Override
    public int deleteErrorLogsByMessageIdInError(List<String> messageIds) {
        return errorLogDao.deleteErrorLogsByMessageIdInError(messageIds);
    }

    @Override
    public List<ErrorLogEntry> getErrorsForMessage(final String messageId) {
        List<ErrorLogEntry> errorsForMessage = errorLogDao.getErrorsForMessage(messageId);
        initializeChildren(errorsForMessage);
        return errorsForMessage;
    }

    @Override
    public List<ErrorLogEntry> findPaged(final int from, final int max, final String sortColumn, final boolean asc, final Map<String, Object> filters) {
        List<ErrorLogEntry> list = errorLogDao.findPaged(from, max, sortColumn, asc, filters);
        initializeChildren(list);
        return list;
    }

    private void initializeChildren(List<ErrorLogEntry> errorLogEntries) {
        for (ErrorLogEntry errorLogEntry : errorLogEntries) {
            initializeChildren(errorLogEntry);
        }
    }

    private void initializeChildren(ErrorLogEntry errorLogEntry) {
        //initialize values from the second level cache
        errorLogEntry.getMshRole();
    }

    @Override
    public List<ErrorResult> getErrors(String messageId) {
        List<ErrorLogEntry> errorsForMessage = errorLogDao.getErrorsForMessage(messageId);
        return errorsForMessage.stream().map(this::convert).collect(Collectors.toList());
    }

    protected ErrorResultImpl convert(ErrorLogEntry errorLogEntry) {
        ErrorResultImpl result = new ErrorResultImpl();
        result.setErrorCode(errorLogEntry.getErrorCode());
        result.setErrorDetail(errorLogEntry.getErrorDetail());
        result.setMessageInErrorId(errorLogEntry.getMessageInErrorId());
        result.setMshRole(eu.domibus.common.MSHRole.valueOf(errorLogEntry.getMshRole().name()));
        result.setNotified(errorLogEntry.getNotified());
        result.setTimestamp(errorLogEntry.getTimestamp());

        return result;
    }

    @Override
    public long countEntries(Map<String, Object> filters) {
        return errorLogDao.countEntries(filters);
    }
}