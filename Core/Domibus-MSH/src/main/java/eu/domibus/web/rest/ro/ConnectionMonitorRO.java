package eu.domibus.web.rest.ro;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
public class ConnectionMonitorRO {

    boolean testable;

    boolean monitored;

    boolean alertable;

    boolean deleteHistory;

    public enum ConnectionStatus {
        UNKNOWN,
        OK,
        BROKEN,
        PENDING,
    }

    ConnectionStatus status = ConnectionStatus.UNKNOWN;

    TestServiceMessageInfoRO lastSent;

    TestServiceMessageInfoRO lastReceived;

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

    public boolean isAlertable() {
        return alertable;
    }

    public void setAlertable(boolean alertable) {
        this.alertable = alertable;
    }

    public boolean isTestable() {
        return testable;
    }

    public void setTestable(boolean testable) {
        this.testable = testable;
    }

    public ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public TestServiceMessageInfoRO getLastSent() {
        return lastSent;
    }

    public void setLastSent(TestServiceMessageInfoRO lastSent) {
        this.lastSent = lastSent;
    }

    public TestServiceMessageInfoRO getLastReceived() {
        return lastReceived;
    }

    public void setLastReceived(TestServiceMessageInfoRO lastReceived) {
        this.lastReceived = lastReceived;
    }

    public boolean isDeleteHistory() {
        return deleteHistory;
    }

    public void setDeleteHistory(boolean deleteHistory) {
        this.deleteHistory = deleteHistory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("testable", testable)
                .append("monitored", monitored)
                .append("status", status)
                .toString();
    }

}
