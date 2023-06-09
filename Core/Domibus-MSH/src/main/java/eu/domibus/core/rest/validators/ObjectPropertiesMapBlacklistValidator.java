package eu.domibus.core.rest.validators;

import eu.domibus.api.validators.CustomWhiteListed;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.validators.ObjectWhiteListed;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

/**
 * Custom validator that checks that all Strings in the map do not contain any char from the blacklist
 * Uses the custom annotation if it is declared on the fields of the type class
 *
 * {@link StringEscapeUtils#escapeHtml4} is needed to avoid reflecting XSS injection back to a backend client
 *
 * @author Ion Perpegel
 * @since 4.2
 */
@Component
public class ObjectPropertiesMapBlacklistValidator extends BaseBlacklistValidator<ObjectWhiteListed, ObjectPropertiesMapBlacklistValidator.Parameter> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ObjectPropertiesMapBlacklistValidator.class);

    private static ThreadLocal<String> messageHolder = ThreadLocal.withInitial(() -> ObjectWhiteListed.MESSAGE);

    private ItemsBlacklistValidator listValidator;

    public ObjectPropertiesMapBlacklistValidator(ItemsBlacklistValidator listValidator) {
        this.listValidator = listValidator;
    }

    @Override
    public void init() {
        super.init();
        listValidator.init();
    }

    @Override
    public String getErrorMessage() {
        return messageHolder.get();
    }

    @Override
    public boolean isValid(ObjectPropertiesMapBlacklistValidator.Parameter value, CustomWhiteListed customAnnotation) {
        Map<String, String[]> valuesMap = value.getValues();
        if (valuesMap == null || valuesMap.isEmpty()) {
            LOG.trace("Parameters map is empty, exiting");
            return true;
        }

        for (Map.Entry<String, String[]> pair : valuesMap.entrySet()) {
            CustomWhiteListed whitelistAnnotation = null;
            Class parameterType = value.getParameterType();
            if (parameterType != null) {
                // use custom whitelist chars, if declared on the parameter or corresponding field
                whitelistAnnotation = value.getParameterAnnotation();
                String prop = pair.getKey();
                try {
                    Field field = parameterType.getDeclaredField(prop);
                    CustomWhiteListed fieldWhitelistAnnotation = field.getAnnotation(CustomWhiteListed.class);
                    if (fieldWhitelistAnnotation != null) {
                        // field annotation takes precedence
                        whitelistAnnotation = fieldWhitelistAnnotation;
                    }
                } catch (NoSuchFieldException e) {
                    LOG.trace("Could not find property named [{}] in metadata class", prop);
                }
            }

            String[] val = pair.getValue();
            if (!listValidator.isValid(val, whitelistAnnotation)) {
                messageHolder.set("Forbidden character(s) detected in the parameter ["
                        + pair.getKey() + "]: ["
                        + Arrays.stream(val).reduce("", (subtotal, msg) -> subtotal + StringEscapeUtils.escapeHtml4(msg)) + "]");
                LOG.debug(messageHolder.get());
                return false;
            }
        }

        LOG.trace("Successfully validated values: [{}]", valuesMap);
        return true;
    }

    public static class Parameter {
        private Map<String, String[]> values;
        private Class parameterType;
        private CustomWhiteListed parameterAnnotation;

        public Class getParameterType() {
            return parameterType;
        }

        public void setParameterType(Class parameterType) {
            this.parameterType = parameterType;
        }

        public CustomWhiteListed getParameterAnnotation() {
            return parameterAnnotation;
        }

        public void setParameterAnnotation(CustomWhiteListed parameterAnnotation) {
            this.parameterAnnotation = parameterAnnotation;
        }

        public Map<String, String[]> getValues() {
            return values;
        }

        public void setValues(Map<String, String[]> values) {
            this.values = values;
        }

        public Parameter(Map<String, String[]> values, Class parameterType, CustomWhiteListed parameterAnnotation) {
            this.values = values;
            this.parameterType = parameterType;
            this.parameterAnnotation = parameterAnnotation;
        }
    }
}
