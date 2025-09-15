package gr.atc.modapto.util;

import gr.atc.modapto.dto.sew.MaintenanceDataDto;
import gr.atc.modapto.enums.CorimFileHeaders;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import gr.atc.modapto.exception.CustomExceptions.*;
import org.springframework.web.multipart.MultipartFile;

public class ExcelFilesUtils {

    private static final Logger logger = LoggerFactory.getLogger(ExcelFilesUtils.class);

    private static final DateTimeFormatter STORED_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Timezones
    private static final ZoneId SYSTEM_TIMEZONE = ZoneId.systemDefault();

    private ExcelFilesUtils() {
    }

    /**
     * Helper class to maintain index with result for order preservation
     */
    private record IndexedResult<T>(int index, T value) { }


    /**
     * Extract Maintenance Data from CORIM File
     *
     * @param file : CORIM File
     * @return List<MaintenanceData>
     */
    public static List<MaintenanceDataDto> extractMaintenanceDataFromCorimFile(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<CorimFileHeaders, Integer> headerPositions = processExcelHeaders(sheet);

            int totalRows = sheet.getLastRowNum();
            if (totalRows == 0) return new ArrayList<>();

            // Process in parallel stream
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

                List<CompletableFuture<IndexedResult<MaintenanceDataDto>>> futures = new ArrayList<>();

                // Process all rows in parallel
                for (int i = 1; i <= totalRows; i++) {
                    final int rowIndex = i;
                    Row row = sheet.getRow(i);

                    CompletableFuture<IndexedResult<MaintenanceDataDto>> future = CompletableFuture
                            .supplyAsync(() -> {
                                MaintenanceDataDto result = processRow(row, headerPositions);
                                return new IndexedResult<>(rowIndex, result);
                            }, executor);

                    futures.add(future);
                }

                // Wait for all tasks to complete
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // Collect and sort results
                return futures.stream()
                        .map(CompletableFuture::join)
                        .filter(indexed -> indexed.value() != null)
                        .sorted(Comparator.comparing(IndexedResult::index))
                        .map(IndexedResult::value)
                        .collect(Collectors.toList());
            }

        } catch (Exception e) {
            logger.error("Unable to read CORIM file '{}' from Request", file.getOriginalFilename());
            throw new FileHandlingException("Unable to read CORIM file '" + file.getOriginalFilename() + "' from input request");
        }
    }

    /**
     * Process a single Excel row
     *
     * @param row Excel row to process
     * @param headerPositions Map of header positions
     * @return Processed MaintenanceDataDto or null if row is empty/invalid
     */
    private static MaintenanceDataDto processRow(Row row, Map<CorimFileHeaders, Integer> headerPositions) {
        if (row == null) {
            return null;
        }

        try {
            MaintenanceDataDto rowData = new MaintenanceDataDto();

            // Process each header column
            for (Map.Entry<CorimFileHeaders, Integer> entry : headerPositions.entrySet()) {
                CorimFileHeaders header = entry.getKey();
                Integer columnPosition = entry.getValue();

                if (columnPosition != null) {
                    Cell cell = row.getCell(columnPosition);
                    if (cell != null) {
                        processCell(rowData, header, cell);
                        rowData.setModaptoModule(null);
                    }
                }
            }
            return rowData;

        } catch (Exception e) {
            logger.warn("Error processing row {}: {}", row.getRowNum(), e.getMessage());
        }
        return null;
    }

    /**
     * Ensure all Excel headers from Corim file are present
     *
     * @param sheet: Excel Data
     * @return Map<CorimDataHeaders, Integer>
     */
    public static Map<CorimFileHeaders, Integer> processExcelHeaders(Sheet sheet) {
        // Reset positions from previous processing and track all new Header Positions
        CorimFileHeaders.resetAllPositions();
        Map<CorimFileHeaders, Integer> headerPositions = new HashMap<>();

        Row headerRow = sheet.getRow(0);
        for (int j = 0; j < headerRow.getLastCellNum(); j++) {
            Cell cell = headerRow.getCell(j);
            if (cell != null) {
                String headerText = cell.getStringCellValue();
                CorimFileHeaders headerEnum = CorimFileHeaders.fromHeader(headerText);
                if (headerEnum != null) {
                    headerEnum.setColumnPosition(j);
                    headerPositions.put(headerEnum, j);
                    logger.debug("Found header: {} at column {}", headerText, j);
                }
            }
        }

        // Validate correct headers
        validateHeaders(headerPositions);

        return headerPositions;
    }

    private static void validateHeaders(Map<CorimFileHeaders, Integer> headerPositions) {
        List<CorimFileHeaders> missingHeaders = Arrays.stream(CorimFileHeaders.values())
                .filter(header -> !headerPositions.containsKey(header))
                .collect(Collectors.toList());

        if (!missingHeaders.isEmpty()) {
            logger.warn("Missing headers: {}", missingHeaders);
        }
    }

    private static void processCell(MaintenanceDataDto rowData, CorimFileHeaders header, Cell cell) {
        switch (CorimFileHeaders.fromHeader(header.getHeader())) {
            case null:
                break;
            case STAGE:
                rowData.setStage(getCellValueAsString(cell));
                break;
            case CELL:
                rowData.setCell(getCellValueAsString(cell));
                break;
            case FAILURE_ELEMENT_ID:
                rowData.setFaultyElementId(getCellValueAsString(cell));
                break;
            case MODULE:
                rowData.setModule(getCellValueAsString(cell));
                break;
            case COMPONENT:
                rowData.setComponent(getCellValueAsString(cell));
                break;
            case MODULE_ID:
                rowData.setModuleId(getCellValueAsString(cell));
                break;
            case COMPONENT_ID:
                rowData.setComponentId(getCellValueAsString(cell));
                break;
            case FAILURE_TYPE:
                rowData.setFailureType(getCellValueAsString(cell));
                break;
            case FAILURE_DESCRIPTION:
                rowData.setFailureDescription(getCellValueAsString(cell));
                break;
            case MAINTENANCE_ACTION_PERFORMED:
                rowData.setMaintenanceActionPerformed(getCellValueAsString(cell));
                break;
            case COMPONENT_REPLACEMENT:
                rowData.setComponentReplacement(getCellValueAsString(cell));
                break;
            case WORKER_NAME:
                rowData.setWorkerName(getCellValueAsString(cell));
                break;
            case TS_REQUEST_CREATION:
                rowData.setTsRequestCreation(getCellValueAsLocalDateTime(cell));
                break;
            case TS_INTERVENTION_STARTED:
                rowData.setTsInterventionStarted(getCellValueAsLocalDateTime(cell));
                break;
            case TS_INTERVENTION_FINISHED:
                rowData.setTsInterventionFinished(getCellValueAsLocalDateTime(cell));
                break;
        }
    }

    /**
     * Retrieve Cell Values based on their Cell Type
     *
     * @param cell : Cell Data
     * @return String
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                    // Handle numeric cells
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * Convert cell value to LocalDateTime
     */
    private static LocalDateTime getCellValueAsLocalDateTime(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String stringValue = cell.getStringCellValue().trim();
                if (stringValue.isEmpty()) {
                    yield null;
                }
                yield parseStringToLocalDateTime(stringValue);
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    yield date.toInstant()
                            .atZone(SYSTEM_TIMEZONE)
                            .toLocalDateTime();
                } else {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private static LocalDateTime parseStringToLocalDateTime(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateString, STORED_FORMAT);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(dateString, ISO_FORMAT);
            } catch (DateTimeParseException e2) {
                logger.warn("Could not parse date string: {} with either format", dateString, e2);
                return null;
            }
        }
    }

}
