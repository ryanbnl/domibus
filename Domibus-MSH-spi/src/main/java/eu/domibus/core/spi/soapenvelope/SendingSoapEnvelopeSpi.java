package eu.domibus.core.spi.soapenvelope;

import javax.xml.soap.SOAPMessage;

/**
 * @author Cosmin Baciu
 * @since 5.0.2
 *
 * SPI interface gives the possibility to validate or modify the SoapEnvelope before being sent to C3. At this stage the signing and encryption are not yet applied in the SoapEnvelope.
 */
public interface SendingSoapEnvelopeSpi {

    /**
     * Hook point that can be used to validate or modify the SoapEnvelope before it is being sent to C3
     *
     * @param soapMessage that will be sent to C3 before signing/encryption
     * @return The modified SoapEnvelope or the same SoapEnvelope in case it has not been modified
     */
    SOAPMessage beforeSending(SOAPMessage soapMessage);
}
