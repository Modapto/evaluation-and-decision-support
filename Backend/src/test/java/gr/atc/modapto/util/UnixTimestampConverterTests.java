package gr.atc.modapto.util;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UnixTimestampConverter Unit Tests")
class UnixTimestampConverterTests {

    private UnixTimestampConverter converter;

    @BeforeEach
    void setUp() {
        converter = new UnixTimestampConverter();
    }

    @Test
    @DisplayName("Convert UNIX timestamp : Success with seconds timestamp")
    void givenSecondsTimestamp_whenConvert_thenReturnsLocalDateTime() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        String timestamp = "1672531200"; // January 1, 2023 00:00:00 UTC

        Object result = converter.convert(timestamp);

        assertThat(result).isInstanceOf(LocalDateTime.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Convert UNIX timestamp : Success with milliseconds timestamp")
    void givenMillisecondsTimestamp_whenConvert_thenReturnsLocalDateTime() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        String timestamp = "1672531200000"; // January 1, 2023 00:00:00 UTC in milliseconds

        Object result = converter.convert(timestamp);

        assertThat(result).isInstanceOf(LocalDateTime.class);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Convert UNIX timestamp : Null input returns null")
    void givenNullInput_whenConvert_thenReturnsNull() throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        String timestamp = null;

        Object result = converter.convert(timestamp);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Convert UNIX timestamp : Invalid format throws exception")
    void givenInvalidTimestamp_whenConvert_thenThrowsCsvDataTypeMismatchException() {
        String invalidTimestamp = "not-a-number";

        assertThatThrownBy(() -> converter.convert(invalidTimestamp))
                .isInstanceOf(CsvDataTypeMismatchException.class)
                .hasMessageContaining("Invalid UNIX timestamp");
    }

    @Test
    @DisplayName("Convert to write : Success with LocalDateTime")
    void givenLocalDateTime_whenConvertToWrite_thenReturnsTimestampString() throws CsvDataTypeMismatchException {
        LocalDateTime dateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0);

        String result = converter.convertToWrite(dateTime);

        assertThat(result).isNotEmpty();
        assertThat(result).matches("\\d+"); // Should be a numeric string
    }
}