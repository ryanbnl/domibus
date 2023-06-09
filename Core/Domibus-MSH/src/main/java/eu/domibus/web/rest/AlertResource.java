package eu.domibus.web.rest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.util.DateUtil;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.web.AlertRo;
import eu.domibus.core.alerts.service.AlertService;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.AlertFilterRequestRO;
import eu.domibus.web.rest.ro.AlertResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping(value = "/rest/alerts")
@Validated
public class AlertResource extends BaseResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertResource.class);


    private AlertService alertService;

    private DateUtil dateUtil;

    private AuthUtils authUtils;

    private DomainTaskExecutor domainTaskExecutor;

    private DomibusConfigurationService domibusConfigurationService;

    static List<AlertType> forbiddenAlertTypesExtAuthProvider = Lists.newArrayList(AlertType.PASSWORD_EXPIRED, AlertType.PASSWORD_IMMINENT_EXPIRATION,
            AlertType.USER_ACCOUNT_DISABLED, AlertType.USER_ACCOUNT_ENABLED, AlertType.USER_LOGIN_FAILURE);

    public AlertResource(AlertService alertService, DateUtil dateUtil, AuthUtils authUtils, DomainTaskExecutor domainTaskExecutor, DomibusConfigurationService domibusConfigurationService) {
        this.alertService = alertService;
        this.dateUtil = dateUtil;
        this.authUtils = authUtils;
        this.domainTaskExecutor = domainTaskExecutor;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @GetMapping
    public AlertResult findAlerts(@Valid AlertFilterRequestRO request) {
        AlertCriteria alertCriteria = getAlertCriteria(request);

        if (!authUtils.isSuperAdmin() || request.getDomainAlerts()) {
            return retrieveAlerts(alertCriteria, false);
        }

        return domainTaskExecutor.submit(() -> retrieveAlerts(alertCriteria, true));
    }

    @GetMapping(path = "/types")
    public List<String> getAlertTypes() {
        return getAlertTypesAsStrings();
    }

    @NotNull
    protected List<String> getAlertTypesAsStrings() {
        final List<AlertType> alertTypes = Lists.newArrayList(AlertType.values());
        if (domibusConfigurationService.isExtAuthProviderEnabled()) {
            return alertTypes.stream()
                    .filter(alertType -> forbiddenAlertTypesExtAuthProvider.stream()
                            .noneMatch(forbiddenAlertType -> forbiddenAlertType.equals(alertType)))
                    .map(Enum::name)
                    .collect(Collectors.toList());
        }
        return alertTypes.stream().map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/levels")
    public List<String> getAlertLevels() {
        final List<AlertLevel> alertLevels = Lists.newArrayList(AlertLevel.values());
        return alertLevels.stream().map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/status")
    public List<String> getAlertStatus() {
        final List<AlertStatus> alertLevels = Lists.newArrayList(AlertStatus.values());
        return alertLevels.stream().map(Enum::name).collect(Collectors.toList());
    }

    @GetMapping(path = "/params")
    public List<String> getAlertParameters(@RequestParam(value = "alertType") String aType) {
        AlertType alertType;
        try {
            alertType = AlertType.valueOf(aType);
        } catch (IllegalArgumentException e) {
            LOG.trace("Invalid or empty alert type:[{}] sent from the gui ", aType, e);
            return Lists.newArrayList();
        }
        List<EventType> sourceEvents = alertType.getSourceEvents();
        if (!sourceEvents.isEmpty()) {
            return sourceEvents.get(0).getProperties();
        } else {
            LOG.trace("Invalid alert type:[{}]: it has no source events.", aType);
            return Lists.newArrayList();
        }
    }

    @PutMapping
    public void processAlerts(@RequestBody List<AlertRo> alertRos) {
        final List<Alert> domainAlerts = filterDomainAlerts(alertRos);
        final List<Alert> superAlerts = filterSuperAlerts(alertRos);
        final List<Alert> deletedDomainAlerts = filterDeletedDomainAlerts(alertRos);
        final List<Alert> deletedSuperAlerts = filterDeletedSuperAlerts(alertRos);

        alertService.updateAlertProcessed(domainAlerts);
        domainTaskExecutor.submit(() -> alertService.updateAlertProcessed(superAlerts));
        alertService.deleteAlerts(deletedDomainAlerts);
        domainTaskExecutor.submit(() -> alertService.deleteAlerts(deletedSuperAlerts));
    }

    protected List<Alert> filterDomainAlerts(@RequestBody List<AlertRo> alertRos) {
        return alertRos.stream()
                .filter(Objects::nonNull)
                .filter(alertRo -> !alertRo.isSuperAdmin())
                .filter(alertRo -> !alertRo.isDeleted())
                .map(this::toAlert)
                .collect(Collectors.toList());
    }

    protected List<Alert> filterSuperAlerts(@RequestBody List<AlertRo> alertRos) {
        return alertRos.stream()
                .filter(Objects::nonNull)
                .filter(AlertRo::isSuperAdmin)
                .filter(alertRo -> !alertRo.isDeleted())
                .map(this::toAlert)
                .collect(Collectors.toList());
    }

    protected List<Alert> filterDeletedDomainAlerts(@RequestBody List<AlertRo> alertRos) {
        return alertRos.stream()
                .filter(Objects::nonNull)
                .filter(alertRo -> alertRo.isDeleted())
                .filter(alertRo -> !alertRo.isSuperAdmin())
                .map(this::toAlert)
                .collect(Collectors.toList());
    }

    protected List<Alert> filterDeletedSuperAlerts(@RequestBody List<AlertRo> alertRos) {
        return alertRos.stream()
                .filter(Objects::nonNull)
                .filter(alertRo -> alertRo.isDeleted())
                .filter(AlertRo::isSuperAdmin)
                .map(this::toAlert)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@Valid AlertFilterRequestRO request) {
        request.setPage(0);
        request.setPageSize(getCsvService().getPageSizeForExport());
        AlertCriteria alertCriteria = getAlertCriteria(request);
        List<AlertRo> alertRoList;
        if (!authUtils.isSuperAdmin() || request.getDomainAlerts()) {
            alertRoList = fetchAndTransformAlerts(alertCriteria, false);
        } else {
            alertRoList = domainTaskExecutor.submit(() -> fetchAndTransformAlerts(alertCriteria, true));
        }

        return exportToCSV(alertRoList, AlertRo.class,
                ImmutableMap.of("entityId".toUpperCase(), "Alert Id",
                        "attempts".toUpperCase(), "Sent Attempts"),
                getExcludedColumns(domibusConfigurationService.isSingleTenantAware()),
                "alerts");
    }

    protected List<String> getExcludedColumns(boolean singleTenancy) {
        List<String> excludedColumns = new ArrayList<>();
        excludedColumns.addAll(Arrays.asList("alertDescription", "deleted", "nextAttemptTimezoneId", "nextAttemptOffsetSeconds"));
        if (singleTenancy) {
            excludedColumns.add("superAdmin");
        }
        return excludedColumns;
    }

    protected AlertResult retrieveAlerts(AlertCriteria alertCriteria, boolean isSuperAdmin) {
        final Long alertCount = alertService.countAlerts(alertCriteria);
        final List<Alert> alerts = alertService.findAlerts(alertCriteria);
        final List<AlertRo> alertRoList = alerts.stream().map(this::transform).collect(Collectors.toList());
        alertRoList.forEach(alert -> alert.setSuperAdmin(isSuperAdmin));
        final AlertResult alertResult = new AlertResult();
        alertResult.setAlertsEntries(alertRoList);
        alertResult.setCount(alertCount.intValue());
        return alertResult;
    }

    protected Alert toAlert(AlertRo alertRo) {
        final long entityId = Long.parseLong(alertRo.getEntityId());
        final boolean processed = alertRo.isProcessed();
        Alert alert = new Alert();
        alert.setEntityId(entityId);
        alert.setProcessed(processed);
        return alert;
    }

    protected List<AlertRo> fetchAndTransformAlerts(AlertCriteria alertCriteria, boolean isSuperAdmin) {
        final List<Alert> alerts = alertService.findAlerts(alertCriteria);
        getCsvService().validateMaxRows(alerts.size(), () -> alertService.countAlerts(alertCriteria));

        final List<AlertRo> alertRoList = alerts.stream().map(this::transform).collect(Collectors.toList());
        alertRoList.forEach(alert -> alert.setSuperAdmin(isSuperAdmin));
        return alertRoList;
    }

    private AlertCriteria getAlertCriteria(AlertFilterRequestRO request) {
        AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.setPage(request.getPage());
        alertCriteria.setPageSize(request.getPageSize());
        alertCriteria.setAsc(request.getAsc());
        alertCriteria.setOrderBy(request.getOrderBy());
        alertCriteria.setProcessed(request.getProcessed());
        alertCriteria.setAlertType(request.getAlertType());
        alertCriteria.setAlertID(request.getAlertId());
        alertCriteria.setAlertLevel(request.getAlertLevel());
        alertCriteria.setAlertStatus(request.getAlertStatus());

        if (StringUtils.isNotEmpty(request.getCreationFrom())) {
            alertCriteria.setCreationFrom(dateUtil.fromString(request.getCreationFrom()));
        }
        if (StringUtils.isNotEmpty(request.getCreationTo())) {
            alertCriteria.setCreationTo(dateUtil.fromString(request.getCreationTo()));
        }
        if (StringUtils.isNotEmpty(request.getReportingFrom())) {
            alertCriteria.setReportingFrom(dateUtil.fromString(request.getReportingFrom()));
        }

        if (StringUtils.isNotEmpty(request.getReportingTo())) {
            alertCriteria.setReportingTo(dateUtil.fromString(request.getReportingTo()));
        }

        if (StringUtils.isEmpty(request.getAlertType())) {
            request.setAlertType(AlertType.MSG_STATUS_CHANGED.name());
        }
        if (request.getParameters() != null) {
            final List<String> nonDateParameters = getNonDateParameters(request.getAlertType());
            final Map<String, String> parametersMap = IntStream.
                    range(0, request.getParameters().length).
                    mapToObj(i -> new SimpleImmutableEntry<>(nonDateParameters.get(i), request.getParameters()[i])).
                    filter(keyValuePair -> !keyValuePair.getValue().isEmpty()).
                    collect(Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue)); //NOSONAR
            alertCriteria.setParameters(parametersMap);
        }
        final String uniqueDynamicDateParameter = getUniqueDynamicDateParameter(request.getAlertType());
        alertCriteria.setUniqueDynamicDateParameter(uniqueDynamicDateParameter);
        if (StringUtils.isNotEmpty(request.getDynamicFrom())) {
            alertCriteria.setDynamicaPropertyFrom(dateUtil.fromString(request.getDynamicFrom()));
        }

        if (StringUtils.isNotEmpty(request.getDynamicTo())) {
            alertCriteria.setDynamicaPropertyTo(dateUtil.fromString(request.getDynamicTo()));
        }

        return alertCriteria;
    }

    private List<String> getNonDateParameters(String alertType) {
        return getAlertParameters(alertType).stream().filter(s -> !(s.endsWith("_TIME") || s.endsWith("_DATE"))).collect(Collectors.toList());
    }

    private String getUniqueDynamicDateParameter(String alertType) {
        final List<String> collect = getAlertParameters(alertType).stream().filter(s -> (s.endsWith("_TIME") || s.endsWith("_DATE"))).collect(Collectors.toList());
        if (collect.size() > 1) {
            throw new IllegalStateException("Only one dynamic date per alert type is supported right now.");
        }
        if (collect.isEmpty()) {
            return null;
        }
        return collect.get(0);

    }

    private AlertRo transform(Alert alert) {
        AlertRo alertRo = new AlertRo();
        alertRo.setProcessed(alert.isProcessed());
        alertRo.setEntityId(Long.toString(alert.getEntityId()));
        alertRo.setAlertType(alert.getAlertType().name());
        alertRo.setAlertLevel(alert.getAlertLevel().name());
        alertRo.setCreationTime(alert.getCreationTime());
        alertRo.setReportingTime(alert.getReportingTime());
        alertRo.setAlertStatus(alert.getAlertStatus().name());
        alertRo.setAttempts(alert.getAttempts());
        alertRo.setMaxAttempts(alert.getMaxAttempts());
        alertRo.setReportingTimeFailure(alert.getReportingTimeFailure());
        alertRo.setNextAttempt(alert.getNextAttempt());
        alertRo.setNextAttemptTimezoneId(alert.getNextAttemptTimezoneId());
        alertRo.setNextAttemptOffsetSeconds(alert.getNextAttemptOffsetSeconds());

        final List<String> alertParameterNames = getAlertParameters(alert.getAlertType().name());
        final List<String> alertParameterValues = alertParameterNames.
                stream().
                map(paramName -> alert.getEvents().iterator().next().findOptionalProperty(paramName)).
                filter(Optional::isPresent).
                map(Optional::get).
                map(this::manageMaxLength).
                collect(Collectors.toList());
        alertRo.setParameters(alertParameterValues);
        return alertRo;
    }

    private String manageMaxLength(String param) {
        return StringUtils.abbreviate(param, 254);
    }

}
