package gr.atc.modapto.util;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class UnixTimestampConverter extends AbstractBeanField<LocalDateTime, String> {

    @Override
    protected Object convert(String s) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }

        try {
            long unixTimestamp = Long.parseLong(s.trim());

            // Handle both seconds and milliseconds timestamps
            if (unixTimestamp > 9999999999L) {
                // Milliseconds timestamp
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTimestamp), ZoneId.systemDefault());
            } else {
                // Seconds timestamp
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
            }
        } catch (NumberFormatException e) {
            throw new CsvDataTypeMismatchException("Invalid UNIX timestamp: " + s);
        }
    }

    @Override
    protected String convertToWrite(Object value) throws CsvDataTypeMismatchException {
        if (value instanceof LocalDateTime) {
            LocalDateTime dateTime = (LocalDateTime) value;
            return String.valueOf(dateTime.atZone(ZoneId.systemDefault()).toEpochSecond());
        }
        return "";
    }
}
