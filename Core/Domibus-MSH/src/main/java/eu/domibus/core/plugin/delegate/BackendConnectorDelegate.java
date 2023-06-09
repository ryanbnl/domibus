package eu.domibus.core.plugin.delegate;

import eu.domibus.common.*;
import eu.domibus.plugin.BackendConnector;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public interface BackendConnectorDelegate {

    void messageReceiveFailed(BackendConnector backendConnector, MessageReceiveFailureEvent event);

    void messageResponseSent(BackendConnector backendConnector, MessageResponseSentEvent event);

    void messageStatusChanged(BackendConnector backendConnector, MessageStatusChangeEvent event);

    void deliverMessage(BackendConnector backendConnector, DeliverMessageEvent event);

    void messageSendFailed(BackendConnector backendConnector, MessageSendFailedEvent event);

    void messageSendSuccess(BackendConnector backendConnector, MessageSendSuccessEvent event);

    void messageDeletedEvent(String backendName, MessageDeletedEvent event);

    void messageDeletedBatchEvent(String backendName, MessageDeletedBatchEvent event);

    void payloadSubmitted(BackendConnector backendConnector, PayloadSubmittedEvent event);

    void payloadProcessed(BackendConnector backendConnector, PayloadProcessedEvent event);

}
