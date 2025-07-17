package gr.atc.modapto.validation;

import gr.atc.modapto.validation.validators.ExcelFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation for Excel file validation
 */
@Documented
@Constraint(validatedBy = ExcelFileValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExcelFile {
    String message() default "Invalid Excel file format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}