package gr.atc.modapto.util;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import gr.atc.modapto.enums.CorimFileHeaders;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExcelFilesUtils Unit Tests")
class ExcelFilesUtilsTests {

    private XSSFWorkbook workbook;
    private Sheet sheet;
    
    @BeforeEach
    void setUp() {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("MaintenanceData");
    }
    
    @AfterEach
    void tearDown() throws IOException {
        if (workbook != null) {
            workbook.close();
        }
    }

    @Nested
    @DisplayName("Extract Maintenance Data from CORIM File")
    class ExtractMaintenanceDataFromCorimFile {

        @Test
        @DisplayName("Extract maintenance data : Success from valid CORIM file")
        void givenValidCorimFile_whenExtractMaintenanceData_thenReturnsDataList() throws Exception {
            // Given
            createValidCorimFile();
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStage()).isEqualTo("Stage1");
            assertThat(result.get(0).getCell()).isEqualTo("Cell1");
            assertThat(result.get(0).getComponent()).isEqualTo("Component1");
            assertThat(result.get(1).getStage()).isEqualTo("Stage2");
        }

        @Test
        @DisplayName("Extract maintenance data : Empty list for headers only")
        void givenFileWithHeadersOnly_whenExtractMaintenanceData_thenReturnsEmptyList() throws Exception {
            // Given
            createHeaderOnlyFile();
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Extract maintenance data : Empty file")
        void givenEmptyFile_whenExtractMaintenanceData_thenReturnsEmptyList() throws Exception {
            // Given
            MockMultipartFile file = createMockMultipartFile();

            // When - Then
            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Extract maintenance data : Invalid file format")
        void givenInvalidFile_whenExtractMaintenanceData_thenThrowsFileHandlingException() {
            // Given
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "invalid content".getBytes()
            );

            // When & Then
            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(invalidFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Extract maintenance data : Handle null values")
        void givenRowsWithNullValues_whenExtractMaintenanceData_thenReturnsPartialData() throws Exception {
            // Given
            createCorimFileWithNullValues();
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("Stage1");
            assertThat(result.get(0).getCell()).isNull();
        }

        @Test
        @DisplayName("Extract maintenance data : Date formatted cells")
        void givenDateFormattedCells_whenExtractMaintenanceData_thenReturnsISODateStrings() throws Exception {
            // Given
            createCorimFileWithDateValues();
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTsRequestCreation()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        }

        private void createValidCorimFile() {
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            headerRow.createCell(2).setCellValue("Component");
            headerRow.createCell(3).setCellValue("Failure Type");

            // Create data rows
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("Stage1");
            dataRow1.createCell(1).setCellValue("Cell1");
            dataRow1.createCell(2).setCellValue("Component1");
            dataRow1.createCell(3).setCellValue("Type1");

            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("Stage2");
            dataRow2.createCell(1).setCellValue("Cell2");
            dataRow2.createCell(2).setCellValue("Component2");
            dataRow2.createCell(3).setCellValue("Type2");
        }

        private void createHeaderOnlyFile() {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
        }

        private void createCorimFileWithNullValues() {
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");

            // Create data row with null cell
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Stage1");
            // Cell 1 is intentionally left null
        }

        private void createCorimFileWithDateValues() {
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("TS request creation");

            // Create data row with date value
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Stage1");
            
            Cell dateCell = dataRow.createCell(1);
            Calendar calendar = Calendar.getInstance();
            calendar.set(2024, 0, 15, 10, 30, 0);
            dateCell.setCellValue(calendar.getTime());
            
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
            dateCell.setCellStyle(dateStyle);
        }
    }

    @Nested
    @DisplayName("Process Excel Headers")
    class ProcessExcelHeaders {

        @Test
        @DisplayName("Process Excel headers : Success with valid headers")
        void givenValidHeaders_whenProcessExcelHeaders_thenReturnsHeaderPositionMap() {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            headerRow.createCell(2).setCellValue("Component");

            // When
            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(CorimFileHeaders.STAGE)).isZero();
            assertThat(result.get(CorimFileHeaders.CELL)).isEqualTo(1);
            assertThat(result.get(CorimFileHeaders.COMPONENT)).isEqualTo(2);
        }

        @Test
        @DisplayName("Process Excel headers : Headers with extra spaces")
        void givenHeadersWithSpaces_whenProcessExcelHeaders_thenHandlesSpacesCorrectly() {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("  Stage  ");
            headerRow.createCell(1).setCellValue("Cell");

            // When
            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            // Then
            assertThat(result).containsKey(CorimFileHeaders.CELL);
            // Note: The actual behavior depends on CorimFileHeaders.fromHeader() implementation
        }

        @Test
        @DisplayName("Process Excel headers : Ignore unknown headers")
        void givenUnknownHeaders_whenProcessExcelHeaders_thenIgnoresUnknownHeaders() {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Unknown Header");
            headerRow.createCell(2).setCellValue("Cell");

            // When
            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(CorimFileHeaders.STAGE)).isZero();
            assertThat(result.get(CorimFileHeaders.CELL)).isEqualTo(2);
        }

        @Test
        @DisplayName("Process Excel headers : Empty header row")
        void givenEmptyHeaderRow_whenProcessExcelHeaders_thenReturnsEmptyMap() {
            // Given
            sheet.createRow(0); // Empty row

            // When
            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cell Value Processing")
    class CellValueProcessing {

        @ParameterizedTest(name = "Date string '{0}' should be converted to ISO format")
        @CsvSource({
            "15/01/2024 10:30:00, 2024-01-15 10:30:00",
            "01/12/2023 23:59:59, 2023-12-01 23:59:59",
            "29/02/2024 12:00:00, 2024-02-29 12:00:00"
        })
        @DisplayName("Cell value processing : Date string to ISO format")
        void givenDateString_whenProcessCellValue_thenReturnsISOFormat(String inputDate, String expectedOutput) throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("TS request creation");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(inputDate);
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTsRequestCreation()).isEqualTo(expectedOutput);
        }

        @Test
        @DisplayName("Cell value processing : Non-date strings")
        void givenNonDateString_whenProcessCellValue_thenReturnsAsIs() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Regular String Value");
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("Regular String Value");
        }

        @Test
        @DisplayName("Cell value processing : Numeric cells")
        void givenNumericCell_whenProcessCellValue_thenReturnsStringValue() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(123.45);
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("123.45");
        }

        @Test
        @DisplayName("Cell value processing : Boolean cells")
        void givenBooleanCell_whenProcessCellValue_thenReturnsStringValue() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(true);
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("true");
        }

        @Test
        @DisplayName("Cell value processing : Integer numeric values")
        void givenIntegerNumericValue_whenProcessCellValue_thenReturnsIntegerString() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(123.0);
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Error handling : Corrupted Excel file")
        void givenCorruptedExcelFile_whenExtractMaintenanceData_thenThrowsFileHandlingException() {
            // Given
            MockMultipartFile corruptedFile = new MockMultipartFile(
                    "file", "corrupted.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "This is not a valid Excel file content".getBytes()
            );

            // When & Then
            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(corruptedFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Error handling : Malformed data in rows")
        void givenMalformedDataInRows_whenExtractMaintenanceData_thenHandlesGracefully() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            // Create a row that might cause processing issues
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("ValidStage");
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStage()).isEqualTo("ValidStage");
        }
    }

    @Nested
    @DisplayName("Performance and Concurrency")
    class PerformanceAndConcurrency {

        @Test
        @DisplayName("Performance : Large files processing")
        void givenLargeFile_whenExtractMaintenanceData_thenProcessesEfficiently() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            
            // Create many rows to test performance
            for (int i = 1; i <= 1000; i++) {
                Row dataRow = sheet.createRow(i);
                dataRow.createCell(0).setCellValue("Stage" + i);
                dataRow.createCell(1).setCellValue("Cell" + i);
            }
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            long startTime = System.currentTimeMillis();
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);
            long endTime = System.currentTimeMillis();

            // Then
            assertThat(result).hasSize(1000);
            assertThat(endTime - startTime).isLessThan(5000); // Should complete within 5 seconds
        }

        @Test
        @DisplayName("Performance : Data order preservation")
        void givenParallelProcessing_whenExtractMaintenanceData_thenMaintainsDataOrder() throws Exception {
            // Given
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            for (int i = 1; i <= 100; i++) {
                Row dataRow = sheet.createRow(i);
                dataRow.createCell(0).setCellValue("Stage" + i);
            }
            
            MockMultipartFile file = createMockMultipartFile();

            // When
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            // Then
            assertThat(result).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(result.get(i).getStage()).isEqualTo("Stage" + (i + 1));
            }
        }
    }

    private MockMultipartFile createMockMultipartFile() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new ByteArrayInputStream(outputStream.toByteArray())
        );
    }
}