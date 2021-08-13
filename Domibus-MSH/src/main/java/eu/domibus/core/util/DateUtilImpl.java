package eu.domibus.core.util;

import eu.domibus.api.util.DateUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Cosmin Baciu
 * @author Sebastian-Ion TINCU
 * @since 3.3
 */
@Component
public class DateUtilImpl implements DateUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DateUtilImpl.class);

    @Override
    public Date fromString(String value) {
        Date result = null;

        if (StringUtils.isNotEmpty(value)) {
            if (StringUtils.isNumeric(value)) {
                result = fromNumber(Long.parseLong(value));
            } else {
                result = fromISO8601(value);
            }
        }

        return result;
    }

    public Timestamp fromNumber(Number value) {
        return new Timestamp(value.longValue());
    }

    public Timestamp fromISO8601(String value) {
        Date date = null;
        try {
            try {
                LOG.debug("Parsing an offset date time value");
                OffsetDateTime dateTime = OffsetDateTime.parse(value);
                date = Date.from(dateTime.toInstant());
            } catch (DateTimeParseException ex) {
                LOG.debug("Error during Parsing offset date time value");
                try {
                    LOG.debug("Parsing local date time value");
                    LocalDateTime dateTime = LocalDateTime.parse(value);
                    date = Date.from(dateTime.toInstant(ZoneOffset.UTC));

                } catch (DateTimeParseException timeParseException) {
                    LOG.debug("Error during Parsing local date time value");
                    throw timeParseException;
                }
                throw ex;
            }
        } catch (DateTimeParseException ex) {
            LOG.debug("Exception occurred during parsing of date time. [{}]", ex);
            if (date == null) {
                throw new DateTimeParseException("Cannot parse datetime value", value, 0);
            }
        }

        return new Timestamp(date.getTime());
    }

    @Override
    public Date getStartOfDay() {
        return Date.from(LocalDateTime.now(ZoneOffset.UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC));
    }

    @Override
    public String getCurrentTime(DateTimeFormatter dateTimeFormatter) {
        return java.time.LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter);
    }

    @Override
    public String getCurrentTime() {
        return getCurrentTime(DEFAULT_FORMATTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getUtcDate() {
        return new Date();
    }

    @Override
    public long getDiffMinutesBetweenDates(Date date1, Date date2) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        return TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
}
