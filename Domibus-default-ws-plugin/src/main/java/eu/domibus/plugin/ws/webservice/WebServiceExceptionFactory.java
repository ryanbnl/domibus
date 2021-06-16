package eu.domibus.plugin.ws.webservice;

import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.ws.generated.body.FaultDetail;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.1.4
 */
@Service
public class WebServiceExceptionFactory {

    public FaultDetail createDownloadMessageFault(Exception ex) {
        FaultDetail detail = WebServiceImpl.WEBSERVICE_OF.createFaultDetail();
        detail.setCode(eu.domibus.common.ErrorCode.EBMS_0004.getErrorCodeName());
        if (ex instanceof MessagingProcessingException) {
            MessagingProcessingException mpEx = (MessagingProcessingException) ex;
            detail.setCode(mpEx.getEbms3ErrorCode().getErrorCodeName());
            detail.setMessage(mpEx.getMessage());
        } else {
            detail.setMessage(ex.getMessage());
        }
        return detail;
    }

    public FaultDetail createFault(String message) {
        FaultDetail detail = WebServiceImpl.WEBSERVICE_OF.createFaultDetail();
        detail.setCode(eu.domibus.common.ErrorCode.EBMS_0004.getErrorCodeName());
        detail.setMessage(message);
        return detail;
    }
}
