package eu.domibus.web.rest.validators;

import com.google.common.base.Strings;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * Custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@Component
public class BlacklistValidator implements ConstraintValidator<NotBlacklisted, String> {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(BlacklistValidator.class);

    public static final String BLACKLIST_PROPERTY = "domibus.userInput.blackList";

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    Character[] blacklist = null;

    public void init() {
        if (blacklist == null) {
            String blacklistValue = domibusPropertyProvider.getProperty(BLACKLIST_PROPERTY);
            if (!Strings.isNullOrEmpty(blacklistValue)) {
                this.blacklist = ArrayUtils.toObject(blacklistValue.toCharArray());
            }
        }
    }

    @Override
    public void initialize(NotBlacklisted attr) {
        init();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isValid(value);
    }

    public void validate(String value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(NotBlacklisted.MESSAGE);
        }
    }

    public boolean isValid(String value) {
        if (ArrayUtils.isEmpty(blacklist)) {
            return true;
        }
        if (Strings.isNullOrEmpty(value)) {
            return true;
        }

        return !Arrays.stream(blacklist).anyMatch(el -> value.contains(el.toString()));
    }

}