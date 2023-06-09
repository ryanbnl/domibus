package eu.domibus.plugin.fs.jaxb;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.convert.StringToTemporalAccessorConverter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Custom adapter which extends {@link XmlAdapter} for {@code xsd:dateTime} mapped to {@code LocalDateTime}
 *
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class DateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DateTimeAdapter.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final StringToTemporalAccessorConverter converter;

    public DateTimeAdapter() {
        this.converter = new StringToTemporalAccessorConverter(FORMATTER);
    }

    @Override
    public LocalDateTime unmarshal(String s) {
        TemporalAccessor converted = converter.convert(s);
        if(!(converted instanceof LocalDateTime)) {
            LOG.warn("The source [{}] could not be correctly converted to a local date time instance [{}]", s, converted);
            return null;
        }
        return (LocalDateTime) converter.convert(s);
    }

    @Override
    public String marshal(LocalDateTime dt) throws Exception {
        if (dt == null) {
            LOG.info("Returning null value for a null local date time input");
            return null;
        }
        return dt.format(FORMATTER);
    }
}