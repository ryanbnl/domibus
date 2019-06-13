package eu.domibus.web.rest.validators;

import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.annotation.PostConstruct;
import javax.validation.ValidationException;
import java.lang.reflect.Type;

/**
 * @author Ion Perpegel
 * @since 4.1
 * A Spring interceptor that ensures that the request body of a REST call does not contain blacklisted chars in any of its String properties
 */
@ControllerAdvice(annotations = RestController.class)
public class RequestBodyValidationInterceptor extends RequestBodyAdviceAdapter {
    private static final Logger LOG = DomibusLoggerFactory.getLogger(RequestBodyValidationInterceptor.class);

    @PostConstruct
    public void init() {
        blacklistValidator.init();
    }

    @Autowired
    ObjectBlacklistValidator blacklistValidator;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return handleRequestBody(body);
    }

    protected Object handleRequestBody(Object body) {
        LOG.debug("Validate body:[{}]", body);
        try {
            blacklistValidator.validate(body);
            LOG.debug("Body:[{}] is valid", body);
            return body;
        } catch (ValidationException ex) {
            LOG.debug("Body:[{}] is invalid: [{}]", body, ex);
            throw ex;
        } catch (Exception ex) {
            LOG.debug("Unexpected exception caught [{}] when validating body: [{}]. Request will be processed downhill.", ex, body);
            return body;
        }
    }
}