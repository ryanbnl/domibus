package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageEvent;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.BackendConnector;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageStatusChangeNotifier implements PluginEventNotifier<MessageStatusChangeEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageStatusChangeNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_STATUS_CHANGE == notificationType;
    }

    @Override
    public void notifyPlugin(MessageStatusChangeEvent messageEvent, BackendConnector<?, ?> backendConnector, Long messageEntityId, String messageId, Map<String, String> properties) {
        backendConnectorDelegate.messageStatusChanged(backendConnector, messageEvent);
    }
}
