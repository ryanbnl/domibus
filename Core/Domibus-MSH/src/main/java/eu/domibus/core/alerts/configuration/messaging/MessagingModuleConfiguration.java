package eu.domibus.core.alerts.configuration.messaging;

import eu.domibus.api.alerts.AlertLevel;
import eu.domibus.api.model.MessageStatus;
import eu.domibus.core.alerts.configuration.common.AlertModuleConfigurationBase;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.MessageEvent;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MessagingModuleConfiguration extends AlertModuleConfigurationBase {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(MessagingModuleConfiguration.class);

    protected Map<MessageStatus, AlertLevel> messageStatusLevels = new EnumMap<>(MessageStatus.class);

    public MessagingModuleConfiguration(final String mailSubject) {
        this();
        this.setMailSubject(mailSubject);
    }

    public MessagingModuleConfiguration() {
        super(AlertType.MSG_STATUS_CHANGED);
    }

    public void addStatusLevelAssociation(MessageStatus messageStatus, AlertLevel alertLevel) {
        messageStatusLevels.put(messageStatus, alertLevel);
    }

    public boolean shouldMonitorMessageStatus(MessageStatus messageStatus) {
        return isActive() && messageStatusLevels.get(messageStatus) != null;
    }

    public AlertLevel getAlertLevel(MessageStatus messageStatus) {
        return messageStatusLevels.get(messageStatus);
    }

    @Override
    public String toString() {
        return "MessagingConfiguration{" +
                "messageCommunicationActive=" + isActive() +
                ", messageStatusLevels=" + messageStatusLevels +
                '}';
    }

    @Override
    public AlertLevel getAlertLevel(final Event event) {
        super.getAlertLevel(event);

        final Optional<String> stringPropertyValue = event.findStringProperty(MessageEvent.NEW_STATUS.name());
        final MessageStatus newStatus = MessageStatus.valueOf(stringPropertyValue.
                orElseThrow(() -> new IllegalArgumentException("New status property should always be present")));
        final AlertLevel alertLevel = getAlertLevel(newStatus);
        LOG.debug("Alert level for message change to status[{}] is [{}]", newStatus, alertLevel);
        return alertLevel;
    }

}
