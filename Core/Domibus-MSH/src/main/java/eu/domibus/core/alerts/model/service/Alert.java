package eu.domibus.core.alerts.model.service;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id", scope = Alert.class)
public class Alert {

    private long entityId;

    private boolean processed;

    private Date processedTime;

    private AlertType alertType;

    private Date creationTime;

    private Date reportingTime;

    private Integer attempts;

    private Integer maxAttempts;

    private Date nextAttempt;

    private String nextAttemptTimezoneId;

    private int nextAttemptOffsetSeconds;

    private Date reportingTimeFailure;

    private AlertStatus alertStatus;

    private AlertLevel alertLevel;

    private Set<Event> events = new HashSet<>();

    private Map<String, String> properties = new HashMap<>();

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Date getProcessedTime() {
        return processedTime;
    }

    public void setProcessedTime(Date processedTime) {
        this.processedTime = processedTime;
    }

    public AlertType getAlertType() { return alertType; }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
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

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public AlertStatus getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(AlertStatus alertStatus) {
        this.alertStatus = alertStatus;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public Date getNextAttempt() {
        return nextAttempt;
    }

    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getNextAttemptTimezoneId() {
        return nextAttemptTimezoneId;
    }

    public void setNextAttemptTimezoneId(String nextAttemptTimezoneId) {
        this.nextAttemptTimezoneId = nextAttemptTimezoneId;
    }

    public int getNextAttemptOffsetSeconds() {
        return nextAttemptOffsetSeconds;
    }

    public void setNextAttemptOffsetSeconds(int nextAttemptOffsetSeconds) {
        this.nextAttemptOffsetSeconds = nextAttemptOffsetSeconds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entityId", entityId)
                .append("processed", processed)
                .append("processedTime", processedTime)
                .append("alertType", alertType)
                .append("reportingTime", reportingTime)
                .append("attempts", attempts)
                .append("maxAttempts", maxAttempts)
                .append("nextAttempt", nextAttempt)
                .append("nextAttemptTimezoneId", nextAttemptTimezoneId)
                .append("nextAttemptOffsetSeconds", nextAttemptOffsetSeconds)
                .append("reportingTimeFailure", reportingTimeFailure)
                .append("events", events)
                .toString();
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }
}
