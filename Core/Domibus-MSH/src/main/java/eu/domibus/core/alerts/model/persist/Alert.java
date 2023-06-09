package eu.domibus.core.alerts.model.persist;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.model.AbstractBaseEntity;
import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.api.scheduler.Reprogrammable;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.common.AlertType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_ALERT")
@NamedQueries({
        @NamedQuery(name = "Alert.findRetry", query = "SELECT a FROM Alert a where a.alertStatus='RETRY' and a.nextAttempt < :CURRENT_TIMESTAMP"),
        @NamedQuery(name = "Alert.updateProcess", query = "UPDATE Alert a set a.processed=:PROCESSED where a.entityId=:ALERT_ID"),
        @NamedQuery(name = "Alert.deleteAlerts", query = "DELETE FROM Alert a where  a.creationTime<:ALERT_LIMIT_DATE")
})
public class Alert extends AbstractBaseEntity implements Reprogrammable {

    @Column(name = "PROCESSED")
    private boolean processed;

    @Column(name = "PROCESSED_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processedTime;

    @Column(name = "ALERT_TYPE")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertType alertType;

    @Column(name = "REPORTING_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportingTime;

    @Column(name = "NEXT_ATTEMPT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date nextAttempt;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_TIMEZONE_OFFSET")
    private TimezoneOffset timezoneOffset;

    @Column(name = "ATTEMPTS_NUMBER")
    private Integer attempts;

    @Column(name = "MAX_ATTEMPTS_NUMBER")
    @NotNull
    private Integer maxAttempts;

    @Column(name = "REPORTING_TIME_FAILURE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date reportingTimeFailure;

    @Size(min = 1)
    @ManyToMany(mappedBy = "alerts", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Set<Event> events = new HashSet<>();

    @Column(name = "ALERT_STATUS")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertStatus alertStatus;

    @Column(name = "ALERT_LEVEL")
    @Enumerated(EnumType.STRING)
    @NotNull
    private AlertLevel alertLevel;

    public void addEvent(Event event) {
        events.add(event);
        event.addAlert(this);
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

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public Date getReportingTime() {
        return reportingTime;
    }

    public void setReportingTime(Date reportingTime) {
        this.reportingTime = reportingTime;
    }

    @Override
    public Date getNextAttempt() {
        return nextAttempt;
    }

    @Override
    public void setNextAttempt(Date nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    @Override
    public TimezoneOffset getTimezoneOffset() {
        return timezoneOffset;
    }

    @Override
    public void setTimezoneOffset(TimezoneOffset nextAttemptTimezoneOffset) {
        this.timezoneOffset = nextAttemptTimezoneOffset;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("entityId", getEntityId())
                .append("processed", processed)
                .append("processedTime", processedTime)
                .append("alertType", alertType)
                .append("reportingTime", reportingTime)
                .append("attempts", attempts)
                .append("maxAttempts", maxAttempts)
                .append("reportingTimeFailure", reportingTimeFailure)
                .append("alertStatus", alertStatus)
                .append("alertLevel", alertLevel)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Alert)) return false;

        Alert alert = (Alert) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(processed, alert.processed)
                .append(processedTime, alert.processedTime)
                .append(alertType, alert.alertType)
                .append(reportingTime, alert.reportingTime)
                .append(nextAttempt, alert.nextAttempt)
                .append(attempts, alert.attempts)
                .append(maxAttempts, alert.maxAttempts)
                .append(reportingTimeFailure, alert.reportingTimeFailure)
                .append(alertStatus, alert.alertStatus)
                .append(alertLevel, alert.alertLevel)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(processed)
                .append(processedTime)
                .append(alertType)
                .append(reportingTime)
                .append(nextAttempt)
                .append(attempts)
                .append(maxAttempts)
                .append(reportingTimeFailure)
                .append(alertStatus)
                .append(alertLevel)
                .toHashCode();
    }
}
