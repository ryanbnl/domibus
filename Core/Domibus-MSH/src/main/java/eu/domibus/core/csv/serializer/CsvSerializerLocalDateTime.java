package eu.domibus.core.csv.serializer;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static eu.domibus.core.csv.CsvServiceImpl.CSV_DATE_PATTERN;

/**
 * @author François Gautier
 * @since 4.2
 */
@Service
public class CsvSerializerLocalDateTime implements CsvSerializer {

    @Override
    public boolean canHandle(Object fieldValue) {
        return fieldValue instanceof LocalDateTime;
    }

    @Override
    public String serialize(Object fieldValue) {
        DateTimeFormatter f = DateTimeFormatter.ofPattern(CSV_DATE_PATTERN);
        ZonedDateTime d = ((LocalDateTime) fieldValue).atZone(ZoneOffset.UTC);
        return d.format(f);
    }
}
