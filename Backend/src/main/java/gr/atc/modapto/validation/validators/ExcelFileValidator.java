package gr.atc.modapto.validation.validators;

import gr.atc.modapto.validation.ValidExcelFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class ExcelFileValidator implements ConstraintValidator<ValidExcelFile, MultipartFile> {

    // Valid Excel MIME types
    private static final List<String> VALID_EXCEL_MIME_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel", // .xls
            "application/excel",
            "application/x-excel",
            "application/x-msexcel"
    );


    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String mimeType = file.getContentType();
        if (mimeType != null && VALID_EXCEL_MIME_TYPES.contains(mimeType)) {
            return true;
        }

        String fileName = file.getOriginalFilename();
        if (fileName != null) {
            return fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls");
        }

        return false;
    }
}