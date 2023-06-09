package eu.domibus.core.alerts.model.web;

import eu.domibus.api.validators.SkipWhiteListed;

import java.util.Date;
import java.util.List;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class AlertRo {

    private String entityId;

    private boolean processed;

    private boolean deleted;

    private String alertType;

    private String alertLevel;

    private String alertStatus;

    private String alertDescription;

    private Date creationTime;

    private Date reportingTime;

    private Integer attempts;

    private Integer maxAttempts;

    private Date nextAttempt;

    private String nextAttemptTimezoneId;

    private int nextAttemptOffsetSeconds;

    private Date reportingTimeFailure;

    @SkipWhiteListed
    private List<String> parameters;

    private boolean superAdmin;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String alertId) {
        this.entityId = alertId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertDescription() {
        return alertDescription;
    }

    public void setAlertDescription(String alertDescription) {
        this.alertDescription = alertDescription;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setNextAttemptTimezoneId(String nextAttemptTimezoneId) {
        this.nextAttemptTimezoneId = nextAttemptTimezoneId;
    }

    public void setNextAttemptOffsetSeconds(int nextAttemptOffsetSeconds) {
        this.nextAttemptOffsetSeconds = nextAttemptOffsetSeconds;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(String alertStatus) {
        this.alertStatus = alertStatus;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Date getReportingTimeFailure() {
        return reportingTimeFailure;
    }

    public void setReportingTimeFailure(Date reportingTimeFailure) {
        this.reportingTimeFailure = reportingTimeFailure;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public String getNextAttemptTimezoneId() {
        return nextAttemptTimezoneId;
    }

    public int getNextAttemptOffsetSeconds() {
        return nextAttemptOffsetSeconds;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }
}
