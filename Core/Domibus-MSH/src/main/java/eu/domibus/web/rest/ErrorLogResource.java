package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Ints;
import eu.domibus.api.model.MSHRole;
import eu.domibus.api.util.DateUtil;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.converter.AuditLogCoreMapper;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.error.ErrorLogService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.ErrorLogFilterRequestRO;
import eu.domibus.web.rest.ro.ErrorLogRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/errorlogs")
@Validated
public class ErrorLogResource extends BaseResource {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ErrorLogResource.class);

    @Autowired
    private ErrorLogService errorLogService;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    private AuditLogCoreMapper auditLogCoreMapper;

    @GetMapping
    public ErrorLogResultRO getErrorLog(@Valid ErrorLogFilterRequestRO request) {
        LOGGER.debug("Getting error log");
        HashMap<String, Object> filters = createFilterMap(request);
        ErrorLogResultRO result = new ErrorLogResultRO();
        result.setFilter(filters);
        LOGGER.debug("using filters [{}]", filters);

        long entries = errorLogService.countEntries(filters);
        LOGGER.debug("count [{}]", entries);
        result.setCount(Ints.checkedCast(entries));

        final List<ErrorLogEntry> errorLogEntries = errorLogService.findPaged(request.getPageSize() * request.getPage(),
                request.getPageSize(), request.getOrderBy(), request.getAsc(), filters);
        result.setErrorLogEntries(convert(errorLogEntries));

        result.setErrorCodes(ErrorCode.values());
        result.setMshRoles(MSHRole.values());
        result.setPage(request.getPage());
        result.setPageSize(request.getPageSize());

        return result;
    }

    /**
     * This method returns a CSV file with the contents of Error Log table
     *
     * @return CSV file with the contents of Error Log table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid ErrorLogFilterRequestRO request) {
        HashMap<String, Object> filters = createFilterMap(request);
        final List<ErrorLogEntry> entries = errorLogService.findPaged(0, getCsvService().getPageSizeForExport(),
                request.getOrderBy(), request.getAsc(), filters);
        getCsvService().validateMaxRows(entries.size(), () -> errorLogService.countEntries(filters));

        final List<ErrorLogRO> errorLogROList = auditLogCoreMapper.errorLogEntryListToErrorLogROList(entries);

        return exportToCSV(errorLogROList,
                ErrorLogRO.class,
                ImmutableMap.of(
                        "ErrorSignalMessageId".toUpperCase(), "Signal Message Id",
                        "MshRole".toUpperCase(), "AP Role",
                        "MessageInErrorId".toUpperCase(), "Message Id"),
                "errorlog");
    }

    private HashMap<String, Object> createFilterMap(ErrorLogFilterRequestRO request) {
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("errorSignalMessageId", request.getErrorSignalMessageId());
        filters.put("mshRole", request.getMshRole());
        filters.put("messageInErrorId", request.getMessageInErrorId());
        filters.put("errorCode", request.getErrorCode());
        filters.put("errorDetail", request.getErrorDetail());

        filters.put("timestampFrom", dateUtil.fromString(request.getTimestampFrom()));
        filters.put("timestampTo", dateUtil.fromString(request.getTimestampTo()));
        filters.put("notifiedFrom", dateUtil.fromString(request.getNotifiedFrom()));
        filters.put("notifiedTo", dateUtil.fromString(request.getNotifiedTo()));

        return filters;
    }

    protected List<ErrorLogRO> convert(List<ErrorLogEntry> errorLogEntries) {
        List<ErrorLogRO> result = new ArrayList<>();
        for (ErrorLogEntry errorLogEntry : errorLogEntries) {
            final ErrorLogRO errorLogRO = convert(errorLogEntry);
            if (errorLogRO != null) {
                result.add(errorLogRO);
            }
        }
        return result;
    }

    protected ErrorLogRO convert(ErrorLogEntry errorLogEntry) {
        if (errorLogEntry == null) {
            return null;
        }
        ErrorLogRO result = new ErrorLogRO();
        result.setTimestamp(errorLogEntry.getTimestamp());
        result.setNotified(errorLogEntry.getNotified());
        result.setErrorCode(errorLogEntry.getErrorCode());
        result.setMshRole(errorLogEntry.getMshRole());
        result.setErrorDetail(errorLogEntry.getErrorDetail());
        result.setErrorSignalMessageId(errorLogEntry.getErrorSignalMessageId());
        result.setMessageInErrorId(errorLogEntry.getMessageInErrorId());
        return result;
    }

}
