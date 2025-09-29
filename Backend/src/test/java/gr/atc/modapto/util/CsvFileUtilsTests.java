package gr.atc.modapto.util;

import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvFileUtils Unit Tests")
class CsvFileUtilsTests {

    @Nested
    @DisplayName("Extract KH Events from CSV")
    class ExtractKhEventsFromCsv {

        @Test
        @DisplayName("Extract KH events : Success with valid CSV data")
        void givenValidCsvFile_whenExtractKhEventsDataFromCSV_thenReturnsEventsList() throws IOException {
            String csvContent = """
                    hex;eventType;rfidStation;timestamp;khType;khId
                    1;1;3;1672531200;2;12345
                    1;2;5;1672531260;1;67890
                    """;
            MockMultipartFile file = new MockMultipartFile(
                    "events", "events.csv", "text/csv", csvContent.getBytes()
            );

            List<CrfKitHolderEventDto> result = CsvFileUtils.extractKhEventsDataFromCSV(file);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getEventType()).isEqualTo(1);
            assertThat(result.getFirst().getRfidStation()).isEqualTo(3);
            assertThat(result.getFirst().getKhType()).isEqualTo(2);
            assertThat(result.getFirst().getKhId()).isEqualTo(12345);
        }

        @Test
        @DisplayName("Extract KH events : Success with minimal valid data")
        void givenMinimalValidCsv_whenExtractKhEventsDataFromCSV_thenReturnsEventsList() throws IOException {
            String csvContent = """
                    hex;eventType;rfidStation;timestamp;khType;khId
                    1;1;1;1672531200;1;1
                    """;
            MockMultipartFile file = new MockMultipartFile(
                    "events", "events.csv", "text/csv", csvContent.getBytes()
            );

            List<CrfKitHolderEventDto> result = CsvFileUtils.extractKhEventsDataFromCSV(file);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventType()).isEqualTo(1);
            assertThat(result.get(0).getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Extract KH events : Empty file validation")
        void givenEmptyFile_whenExtractKhEventsDataFromCSV_thenThrowsFileHandlingException() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "events", "events.csv", "text/csv", new byte[0]
            );

            assertThatThrownBy(() -> CsvFileUtils.extractKhEventsDataFromCSV(emptyFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessage("CSV file is empty");
        }

        @Test
        @DisplayName("Extract KH events : Invalid file extension")
        void givenNonCsvFile_whenExtractKhEventsDataFromCSV_thenThrowsFileHandlingException() {
            MockMultipartFile txtFile = new MockMultipartFile(
                    "events", "events.txt", "text/plain", "some content".getBytes()
            );

            assertThatThrownBy(() -> CsvFileUtils.extractKhEventsDataFromCSV(txtFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessage("File must be a CSV file");
        }

        @Test
        @DisplayName("Extract KH events : Null filename validation")
        void givenFileWithNullName_whenExtractKhEventsDataFromCSV_thenThrowsFileHandlingException() {
            MockMultipartFile fileWithNullName = new MockMultipartFile(
                    "events", null, "text/csv", "data".getBytes()
            );

            assertThatThrownBy(() -> CsvFileUtils.extractKhEventsDataFromCSV(fileWithNullName))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessage("File must be a CSV file");
        }
    }
}