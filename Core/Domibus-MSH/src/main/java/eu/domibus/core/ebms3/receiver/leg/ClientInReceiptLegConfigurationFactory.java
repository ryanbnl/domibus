package eu.domibus.core.ebms3.receiver.leg;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("clientInReceiptLegConfigurationFactory")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ClientInReceiptLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Ebms3Messaging messaging) {
        ReceiptLegConfigurationExtractor receiptLegConfigurationExtractor = null;
        if (messaging.getSignalMessage().getReceipt() != null) {
            receiptLegConfigurationExtractor = new ReceiptLegConfigurationExtractor(soapMessage, messaging);
        }
        return receiptLegConfigurationExtractor;
    }
}
