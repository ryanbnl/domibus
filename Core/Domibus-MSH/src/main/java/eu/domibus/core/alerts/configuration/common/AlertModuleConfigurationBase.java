package eu.domibus.core.alerts.configuration.common;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class AlertModuleConfigurationBase implements AlertModuleConfiguration {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertModuleConfigurationBase.class);

    AlertType alertType;

    private boolean active;

    private AlertLevel alertLevel;

    private String mailSubject;

    public AlertModuleConfigurationBase(AlertType alertType) {
        this.alertType = alertType;
    }

    @Override
    public AlertType getAlertType() {
        return alertType;
    }

    @Override
    public String getMailSubject() {
        return mailSubject;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public AlertLevel getAlertLevel(Event event) {
        AlertType alertTypeFromEvent =  AlertType.getByEventType(event.getType());
        if (this.alertType != alertTypeFromEvent) {
            LOG.error("Invalid alert type: [{}] for this strategy, expected: [{}]", alertTypeFromEvent, this.alertType);
            throw new IllegalArgumentException("Invalid alert type of the strategy.");
        }
        return alertLevel;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }
}
