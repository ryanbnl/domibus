package eu.domibus.core.plugin.notification;

import eu.domibus.common.MessageSendSuccessEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.core.metrics.Counter;
import eu.domibus.core.metrics.Timer;
import eu.domibus.core.plugin.delegate.BackendConnectorDelegate;
import eu.domibus.plugin.BackendConnector;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class PluginMessageSendSuccessNotifier implements PluginEventNotifier<MessageSendSuccessEvent> {

    protected BackendConnectorDelegate backendConnectorDelegate;

    public PluginMessageSendSuccessNotifier(BackendConnectorDelegate backendConnectorDelegate) {
        this.backendConnectorDelegate = backendConnectorDelegate;
    }

    @Override
    public boolean canHandle(NotificationType notificationType) {
        return NotificationType.MESSAGE_SEND_SUCCESS == notificationType;
    }

    @Timer(clazz = PluginMessageSendSuccessNotifier.class, value = "notifyPluginSendSuccess")
    @Counter(clazz = PluginMessageSendSuccessNotifier.class, value = "notifyPluginSendSuccess")
    @Override
    public void notifyPlugin(MessageSendSuccessEvent messageEvent, BackendConnector<?, ?> backendConnector) {
        backendConnectorDelegate.messageSendSuccess(backendConnector, messageEvent);
    }
}
