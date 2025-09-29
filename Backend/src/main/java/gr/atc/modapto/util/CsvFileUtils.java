package gr.atc.modapto.util;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import gr.atc.modapto.dto.crf.CrfKitHolderEventDto;

import static gr.atc.modapto.exception.CustomExceptions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

public class CsvFileUtils {

    private static final Logger logger = LoggerFactory.getLogger(CsvFileUtils.class);

    private CsvFileUtils() {
    }

    /**
     * Parse CSV file to CRF Kit Holders Events
     *
     * @param file : CSV file
     * @return List<CrfKitHolderEventDto>
     * @throws IOException
     */
    public static List<CrfKitHolderEventDto> extractKhEventsDataFromCSV(MultipartFile file) throws IOException {
        // Validate File
        validateCsvFile(file);

        // Extract contents to List of Events Data
        try (InputStream inputStream = file.getInputStream();
             Reader reader = new InputStreamReader(inputStream)) {

            CsvToBean<CrfKitHolderEventDto> csvToBean = new CsvToBeanBuilder<CrfKitHolderEventDto>(reader)
                    .withType(CrfKitHolderEventDto.class)
                    .withSeparator(';')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .withSkipLines(1) // Skip header row
                    .build();

            List<CrfKitHolderEventDto> events = csvToBean.parse();

            // Log any parsing errors
            if (!csvToBean.getCapturedExceptions().isEmpty()) {
                logger.warn("CSV parsing errors occurred:");
                csvToBean.getCapturedExceptions().forEach(ex ->
                        logger.warn("Line {}: {}", ex.getLineNumber(), ex.getMessage()));
            }

            return events;
        } catch (Exception e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new FileHandlingException("Failed to process CSV file");
        }
    }

    /*
     * Helper method to validate that a file is not empty and is in CSV format
     */
    private static void validateCsvFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileHandlingException("CSV file is empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new FileHandlingException("File must be a CSV file");
        }
    }
}
