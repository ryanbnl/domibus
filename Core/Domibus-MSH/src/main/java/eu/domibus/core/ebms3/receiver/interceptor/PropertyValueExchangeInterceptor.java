
package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeConstants;
import eu.domibus.core.certificate.CertificateExchangeType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * This interceptor is responsible for the exchange of parameters from a org.apache.cxf.binding.soap.SoapMessage to a javax.xml.soap.SOAPException
 *
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class PropertyValueExchangeInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyValueExchangeInterceptor.class);

    public PropertyValueExchangeInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        final SOAPMessage jaxwsMessage = message.getContent(javax.xml.soap.SOAPMessage.class);
        try {
            jaxwsMessage.setProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY, message.getContextualProperty(PModeConstants.PMODE_KEY_CONTEXT_PROPERTY));
            jaxwsMessage.setProperty(DomainContextProvider.HEADER_DOMIBUS_DOMAIN, message.getContextualProperty(DomainContextProvider.HEADER_DOMIBUS_DOMAIN));
            jaxwsMessage.setProperty(CertificateExchangeType.getKey(), message.getContextualProperty(CertificateExchangeType.getKey()));
            jaxwsMessage.setProperty(CertificateExchangeType.getValue(), message.getContextualProperty(CertificateExchangeType.getValue()));
        } catch (final SOAPException e) {
            PropertyValueExchangeInterceptor.LOG.error("", e);
        }
    }
}


