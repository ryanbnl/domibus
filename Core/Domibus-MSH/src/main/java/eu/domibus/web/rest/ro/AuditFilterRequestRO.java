package eu.domibus.web.rest.ro;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Criteria class to filter the audit logs.
 */

public class AuditFilterRequestRO {

    /**
     * Type of audit.
     */
    private Set<String> auditTargetName;

    /**
     * Type of action linked with audit.
     */
    private Set<String> action;

    /**
     * the user that performed the audited action.
     */
    private Set<String> user;

    /**
     * Date from which we want to retrieve audit logs.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date from;

    /**
     * Date to which we want to retrieve audit logs.
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date to;

    /**
     * The index we want to start from in the list (Page number in pagination system)
     */
    private int start;

    /**
     * The number of reccords we want to retrieve.
     */
    private int max;

    private boolean domain = true;

    public Set<String> getAuditTargetName() {
        return auditTargetName;
    }

    public void setAuditTargetName(Set<String> auditTargetName) {
        this.auditTargetName = auditTargetName;
    }

    public Set<String> getAction() {
        return action;
    }

    public void setAction(Set<String> action) {
        this.action = action;
    }

    public Set<String> getUser() {
        return user;
    }

    public void setUser(Set<String> user) {
        this.user = user;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isDomain() {
        return domain;
    }

    public void setDomain(boolean domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "AuditFilterRequestRO{" +
                "auditTargetName=" + auditTargetName +
                ", action=" + action +
                ", user=" + user +
                ", from=" + from +
                ", to=" + to +
                ", start=" + start +
                ", max=" + max +
                ", domain=" + domain +
                '}';
    }
}
