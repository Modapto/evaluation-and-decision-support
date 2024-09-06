package gr.atc.modapto.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PilotCodeValidator.class)
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPilotCode {
    String message() default "Invalid pilot code. Only SEW, ILTAR, FFT or CRF are applicable!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
