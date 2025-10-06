package gr.atc.modapto.util;

import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.enums.CorimFileHeaders;
import gr.atc.modapto.exception.CustomExceptions.FileHandlingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
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
            createValidCorimFile();
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getStage()).isEqualTo("Stage1");
            assertThat(result.getFirst().getCell()).isEqualTo("Cell1");
            assertThat(result.getFirst().getComponent()).isEqualTo("Component1");
            assertThat(result.get(1).getStage()).isEqualTo("Stage2");
        }

        @Test
        @DisplayName("Extract maintenance data : Empty list for headers only")
        void givenFileWithHeadersOnly_whenExtractMaintenanceData_thenReturnsEmptyList() throws Exception {
            createHeaderOnlyFile();
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Extract maintenance data : Empty file")
        void givenEmptyFile_whenExtractMaintenanceData_thenReturnsEmptyList() throws Exception {
            MockMultipartFile file = createMockMultipartFile();

            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Extract maintenance data : Invalid file format")
        void givenInvalidFile_whenExtractMaintenanceData_thenThrowsFileHandlingException() {
            MockMultipartFile invalidFile = new MockMultipartFile(
                    "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "invalid content".getBytes()
            );

            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(invalidFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Extract maintenance data : Handle null values")
        void givenRowsWithNullValues_whenExtractMaintenanceData_thenReturnsPartialData() throws Exception {
            createCorimFileWithNullValues();
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("Stage1");
            assertThat(result.getFirst().getCell()).isNull();
        }

        @Test
        @DisplayName("Extract maintenance data : Date formatted cells")
        void givenDateFormattedCells_whenExtractMaintenanceData_thenReturnsISODateStrings() throws Exception {
            createCorimFileWithDateValues();
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getTsRequestCreation())
                    .isNotNull()
                    .isAfter(LocalDateTime.of(2020, 1, 1, 0, 0))
                    .isBefore(LocalDateTime.of(2030, 12, 31, 23, 59));
        }

        private void createValidCorimFile() {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            headerRow.createCell(2).setCellValue("Component");
            headerRow.createCell(3).setCellValue("Failure Type");

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
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Stage1");
            // Cell 1 is intentionally left null
        }

        private void createCorimFileWithDateValues() {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("TS request creation");

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Stage1");
            
            Cell dateCell = dataRow.createCell(1);
            Calendar calendar = Calendar.getInstance();
            calendar.set(2024, Calendar.JANUARY, 15, 10, 30, 0);
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
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            headerRow.createCell(2).setCellValue("Component");

            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            assertThat(result).hasSize(3);
            assertThat(result.get(CorimFileHeaders.STAGE)).isZero();
            assertThat(result.get(CorimFileHeaders.CELL)).isEqualTo(1);
            assertThat(result.get(CorimFileHeaders.COMPONENT)).isEqualTo(2);
        }

        @Test
        @DisplayName("Process Excel headers : Headers with extra spaces")
        void givenHeadersWithSpaces_whenProcessExcelHeaders_thenHandlesSpacesCorrectly() {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("  Stage  ");
            headerRow.createCell(1).setCellValue("Cell");

            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            assertThat(result).containsKey(CorimFileHeaders.CELL);
        }

        @Test
        @DisplayName("Process Excel headers : Ignore unknown headers")
        void givenUnknownHeaders_whenProcessExcelHeaders_thenIgnoresUnknownHeaders() {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Unknown Header");
            headerRow.createCell(2).setCellValue("Cell");

            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            assertThat(result).hasSize(2);
            assertThat(result.get(CorimFileHeaders.STAGE)).isZero();
            assertThat(result.get(CorimFileHeaders.CELL)).isEqualTo(2);
        }

        @Test
        @DisplayName("Process Excel headers : Empty header row")
        void givenEmptyHeaderRow_whenProcessExcelHeaders_thenReturnsEmptyMap() {
            sheet.createRow(0); // Empty row

            Map<CorimFileHeaders, Integer> result = ExcelFilesUtils.processExcelHeaders(sheet);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Cell Value Processing")
    class CellValueProcessing {

        @Test
        @DisplayName("Cell value processing : Non-date strings")
        void givenNonDateString_whenProcessCellValue_thenReturnsAsIs() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("Regular String Value");
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("Regular String Value");
        }

        @Test
        @DisplayName("Cell value processing : Numeric cells")
        void givenNumericCell_whenProcessCellValue_thenReturnsStringValue() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(123.45);
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("123.45");
        }

        @Test
        @DisplayName("Cell value processing : Boolean cells")
        void givenBooleanCell_whenProcessCellValue_thenReturnsStringValue() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(true);
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("true");
        }

        @Test
        @DisplayName("Cell value processing : Integer numeric values")
        void givenIntegerNumericValue_whenProcessCellValue_thenReturnsIntegerString() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(123.0);
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Error handling : Corrupted Excel file")
        void givenCorruptedExcelFile_whenExtractMaintenanceData_thenThrowsFileHandlingException() {
            MockMultipartFile corruptedFile = new MockMultipartFile(
                    "file", "corrupted.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "This is not a valid Excel file content".getBytes()
            );

            assertThatThrownBy(() -> ExcelFilesUtils.extractMaintenanceDataFromCorimFile(corruptedFile))
                    .isInstanceOf(FileHandlingException.class)
                    .hasMessageContaining("Unable to read CORIM file");
        }

        @Test
        @DisplayName("Error handling : Malformed data in rows")
        void givenMalformedDataInRows_whenExtractMaintenanceData_thenHandlesGracefully() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("ValidStage");
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getStage()).isEqualTo("ValidStage");
        }
    }

    @Nested
    @DisplayName("Performance and Concurrency")
    class PerformanceAndConcurrency {

        @Test
        @DisplayName("Performance : Large files processing")
        void givenLargeFile_whenExtractMaintenanceData_thenProcessesEfficiently() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            headerRow.createCell(1).setCellValue("Cell");
            
            for (int i = 1; i <= 1000; i++) {
                Row dataRow = sheet.createRow(i);
                dataRow.createCell(0).setCellValue("Stage" + i);
                dataRow.createCell(1).setCellValue("Cell" + i);
            }
            
            MockMultipartFile file = createMockMultipartFile();

            long startTime = System.currentTimeMillis();
            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);
            long endTime = System.currentTimeMillis();

            assertThat(result).hasSize(1000);
            assertThat(endTime - startTime).isLessThan(5000); // Should complete within 5 seconds
        }

        @Test
        @DisplayName("Performance : Data order preservation")
        void givenParallelProcessing_whenExtractMaintenanceData_thenMaintainsDataOrder() throws Exception {
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Stage");
            
            for (int i = 1; i <= 100; i++) {
                Row dataRow = sheet.createRow(i);
                dataRow.createCell(0).setCellValue("Stage" + i);
            }
            
            MockMultipartFile file = createMockMultipartFile();

            List<MaintenanceDataDto> result = ExcelFilesUtils.extractMaintenanceDataFromCorimFile(file);

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