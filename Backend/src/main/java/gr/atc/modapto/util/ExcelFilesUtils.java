package gr.atc.modapto.util;

import gr.atc.modapto.dto.files.MaintenanceDataDto;
import gr.atc.modapto.enums.CorimFileHeaders;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private static final DateTimeFormatter ISO_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
            case REQUEST_ID, RECIPIENT, EQUIPMENT_ID, INTERVENTION_ID, INTERVENTION_STATUS:
            case null:
                break;
            case STAGE:
                rowData.setStage(getCellValueAsString(cell));
                break;
            case CELL:
                rowData.setCell(getCellValueAsString(cell));
                break;
            case MODULE:
                rowData.setModule(getCellValueAsString(cell));
                break;
            case COMPONENT:
                rowData.setComponent(getCellValueAsString(cell));
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
            case COMPONENT_NAME:
                rowData.setComponentName(getCellValueAsString(cell));
                break;
            case TS_REQUEST_CREATION:
                rowData.setTsRequestCreation(getCellValueAsString(cell));
                break;
            case TS_REQUEST_ACKNOWLEDGED:
                rowData.setTsRequestAcknowledged(getCellValueAsString(cell));
                break;
            case TS_INTERVENTION_STARTED:
                rowData.setTsInterventionStarted(getCellValueAsString(cell));
                break;
            case TS_INTERVENTION_FINISHED:
                rowData.setTsInterventionFinished(getCellValueAsString(cell));
                break;
            case MTBF:
                rowData.setMtbf(getCellValueAsString(cell));
                break;
            case MTBF_STAGE_LEVEL:
                rowData.setMtbfStageLevel(getCellValueAsString(cell));
                break;
            case DURATION_CREATION_TO_ACKNOWLEDGED:
                rowData.setDurationCreationToAcknowledged(getCellValueAsString(cell));
                break;
            case DURATION_CREATION_TO_INTERVENTION_START:
                rowData.setDurationCreationToInterventionStart(getCellValueAsString(cell));
                break;
            case DURATION_INTERVENTION_STARTED_TO_FINISHED:
                rowData.setDurationInterventionStartedToFinished(getCellValueAsString(cell));
                break;
            case TOTAL_DURATION_CREATION_TO_FINISHED:
                rowData.setTotalDurationCreationToFinished(getCellValueAsString(cell));
                break;
            case TOTAL_MAINTENANCE_TIME_ALLOCATED:
                rowData.setTotalMaintenanceTimeAllocated(getCellValueAsString(cell));
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
            case STRING -> {
                String stringValue = cell.getStringCellValue().trim();
                // Ensure that String is not a Date value otherwise convert it to ISO format
                yield convertCustomDateStringToISO(stringValue);
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Handle date cells - Convert to ISO format
                    Date date = cell.getDateCellValue();
                    LocalDateTime localDateTime = date.toInstant()
                            .atZone(SYSTEM_TIMEZONE)
                            .toLocalDateTime();

                    yield localDateTime.format(ISO_DATETIME_FORMAT);
                } else {
                    // Handle numeric cells
                    double numValue = cell.getNumericCellValue();
                    if (numValue == (long) numValue) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * Convert custom date string (dd/MM/yyyy HH:mm:ss) to ISO format in France timezone
     */
    private static String convertCustomDateStringToISO(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "";
        }

        try {
            // Parse the custom format (dd/MM/yyyy HH:mm:ss)
            LocalDateTime excelDatetime = LocalDateTime.parse(dateString, STORED_FORMAT);

            // Convert to ISO format
            return excelDatetime.format(ISO_DATETIME_FORMAT);

        } catch (DateTimeParseException e) {
            // If not a date string, return as-is
            return dateString;
        }
    }

}
