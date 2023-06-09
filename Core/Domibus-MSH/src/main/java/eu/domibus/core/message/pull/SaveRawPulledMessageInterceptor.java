package eu.domibus.core.message.pull;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.model.MessageType;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.MessageExchangeService;
import eu.domibus.core.util.SoapUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.SoapOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import javax.xml.ws.WebServiceException;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Interceptor to save the raw xml in case of a pull message.
 * The non repudiation mechanism needs the raw message at the end of the interceptor queue, as it needs the security interceptors added
 * informations in order to do his job.
 * It is only saving userMessage found in the ServerOutInterceptor=&gt; PullMessage.
 */
@Service
public class SaveRawPulledMessageInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRawPulledMessageInterceptor.class);

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    protected SoapUtil soapUtil;

    public SaveRawPulledMessageInterceptor() {
        super(Phase.WRITE_ENDING);
        addAfter(SoapOutInterceptor.SoapOutEndingInterceptor.class.getName());
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        MessageType messageType = (MessageType) message.getExchange().get(MSHDispatcher.MESSAGE_TYPE_OUT);
        String messageId = (String) message.getExchange().get(DispatchClientDefaultProvider.MESSAGE_ID);
        String messageRole = (String) message.getExchange().get(DispatchClientDefaultProvider.MESSAGE_ROLE);
        if (!MessageType.USER_MESSAGE.equals(messageType) || messageId == null) {
            LOG.trace("No handling is performed: message type is [{}]", messageType);
            return;
        }
        try {
            SOAPMessage soapContent = message.getContent(SOAPMessage.class);
            String rawXMLMessage = soapUtil.getRawXMLMessage(soapContent);
            messageExchangeService.saveRawXml(rawXMLMessage, messageId.toString(), MSHRole.valueOf(messageRole.toString()));
        } catch (TransformerException e) {
            throw new WebServiceException(new IllegalArgumentException(e));
        }

    }
}
