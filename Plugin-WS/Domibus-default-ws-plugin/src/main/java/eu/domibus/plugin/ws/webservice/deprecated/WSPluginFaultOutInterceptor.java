package eu.domibus.plugin.ws.webservice.deprecated;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.generated.RetrieveMessageFault;
import eu.domibus.plugin.ws.webservice.WebServiceOperation;
import eu.domibus.plugin.ws.webservice.interceptor.WebServiceFaultOutInterceptor;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.AttachmentOutInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.UnexpectedRollbackException;

import javax.persistence.OptimisticLockException;
import java.util.Arrays;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

/**
 * @author Cosmin Baciu
 * @since 4.1.4
 * @deprecated since 5.0 Use instead {@link WebServiceFaultOutInterceptor}
 */
@Deprecated
@Component("wsPluginFaultOutInterceptorDeprecated")
public class WSPluginFaultOutInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginFaultOutInterceptor.class);
    protected static final CharSequence[] SOAP_FAULT_FORBIDDEN_CODES = {
            "XML_STREAM_EXC",
            "XML_WRITE_EXC"
    };

    @Autowired
    protected WebServicePluginExceptionFactory webServicePluginExceptionFactory;

    public WSPluginFaultOutInterceptor() {
        super(Phase.PRE_STREAM);
        addBefore(Arrays.asList(StaxOutInterceptor.class.getName(), AttachmentOutInterceptor.class.getName()));
    }

    @Override
    public void handleMessage(SoapMessage message) {
        Exception exception = getExceptionContent(message);
        if (exception == null) {
            LOG.debug("No action performed: no exception found");
            return;
        }

        if (soapFaultHasForbiddenCode(exception)) {
            message.setContent(Exception.class, new Fault(exception.getCause()));
        }

        String methodName = getMethodName(message);
        if (WebServiceOperation.RETRIEVE_MESSAGE.equals(methodName)) {
            handleRetrieveMessage(message, exception);
        }
    }

    protected boolean soapFaultHasForbiddenCode(Exception exception) {
        return exception instanceof SoapFault &&
                equalsAnyIgnoreCase(((SoapFault) exception).getCode(), SOAP_FAULT_FORBIDDEN_CODES);
    }

    protected Exception getExceptionContent(SoapMessage message) {
        return message.getContent(Exception.class);
    }

    protected void handleRetrieveMessage(SoapMessage message, Exception exception) {
        LOG.trace("Handling error in [{}] operation", WebServiceOperation.RETRIEVE_MESSAGE);

        Throwable cause = exception.getCause();
        if (cause instanceof UnexpectedRollbackException) {
            handleRetrieveMessageUnexpectedRollbackException(message, exception, (UnexpectedRollbackException) cause);
        }
    }

    protected void handleRetrieveMessageUnexpectedRollbackException(SoapMessage message, Exception exception, UnexpectedRollbackException cause) {
        LOG.error("Error handling request", exception);

        String messageId = LOG.getMDC(DomibusLogger.MDC_MESSAGE_ID);
        String retrieveMessageErrorMessage = getRetrieveMessageErrorMessage(cause, messageId);
        RetrieveMessageFault retrieveMessageFault = new RetrieveMessageFault(retrieveMessageErrorMessage, webServicePluginExceptionFactory.createFault("Error retrieving message"));
        message.setContent(Exception.class, new Fault(retrieveMessageFault));
    }

    protected String getRetrieveMessageErrorMessage(UnexpectedRollbackException unexpectedRollbackException, String messageId) {
        boolean optimisticLockingException = unexpectedRollbackException.contains(OptimisticLockException.class);

        String errorMessage = String.format("Error downloading message [%s]", messageId);
        if (optimisticLockingException) {
            errorMessage = String.format("An attempt was made to download message [%s] multiple times", messageId);
        }
        return errorMessage;
    }

    protected String getMethodName(SoapMessage message) {
        Exchange exchange = message.getExchange();
        if (exchange == null) {
            LOG.trace("Exchange is null");
            return null;
        }
        BindingOperationInfo bindingOperationInfo = exchange.getBindingOperationInfo();
        if (bindingOperationInfo == null) {
            LOG.trace("BindingOperationInfo is null");
            return null;
        }
        OperationInfo operationInfo = bindingOperationInfo.getOperationInfo();
        if (operationInfo == null) {
            LOG.trace("OperationInfo is null");
            return null;
        }

        return operationInfo.getInputName();
    }


}