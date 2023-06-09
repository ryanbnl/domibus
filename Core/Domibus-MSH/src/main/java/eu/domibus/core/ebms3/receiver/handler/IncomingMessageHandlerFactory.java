package eu.domibus.core.ebms3.receiver.handler;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.Messaging;

import javax.xml.soap.SOAPMessage;

/**
 * Responsible for choosing an incoming message handler depending on the message type
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public interface IncomingMessageHandlerFactory {

    IncomingMessageHandler getMessageHandler(final SOAPMessage request, final Ebms3Messaging ebms3Messaging);
}
