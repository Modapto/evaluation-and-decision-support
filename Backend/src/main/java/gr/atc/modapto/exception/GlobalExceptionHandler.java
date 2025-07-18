package gr.atc.modapto.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.modelmapper.MappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import gr.atc.modapto.controller.BaseResponse;

import static gr.atc.modapto.exception.CustomExceptions.*;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/*
 * Exception Handling Responses
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR = "Validation failed";

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<BaseResponse<String>> handleOrderNotFoundException(OrderNotFoundException ex) {
        BaseResponse<String> response = BaseResponse.error("Order not found", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaginationException.class)
    public ResponseEntity<BaseResponse<String>> handlePaginationException(PaginationException ex) {
        BaseResponse<String> response = BaseResponse.error("Invalid pagination parameters were given", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> handleDtoValidationExceptions(@NotNull MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        Objects.requireNonNull(ex.getBindingResult()).getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(BaseResponse.error(VALIDATION_ERROR, errors),
                HttpStatus.BAD_REQUEST);
    }

    /*
     * Validation fails on request parameters, path variables, or method arguments
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Map<String, String>>> constraintValidationExceptionHandler(
            @NotNull ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        return new ResponseEntity<>(BaseResponse.error(VALIDATION_ERROR, errors),
                HttpStatus.BAD_REQUEST);
    }

    /*
     * Handles validation for Method Parameters
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<BaseResponse<String>> validationExceptionHandler(
            @NonNull HandlerMethodValidationException ex) {
        return new ResponseEntity<>(BaseResponse.error(VALIDATION_ERROR, "Invalid input provided - wrong format"),
                HttpStatus.BAD_REQUEST);
    }

    /*
     * Handles missing request body or missing data in request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseResponse<String>> handleHttpMessageNotReadableExceptionHandler(
            HttpMessageNotReadableException ex) {
        String errorMessage = "Required request body is missing or invalid.";

        // Check if instance is for InvalidFormat Validation
        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx
                && invalidFormatEx.getTargetType().isEnum()) {
            String fieldName = invalidFormatEx.getPath().getFirst().getFieldName();
            String invalidValue = invalidFormatEx.getValue().toString();

            // Format the error message according to the Validation Type failure
            errorMessage = String.format("Invalid value '%s' for field '%s'. Allowed values are: %s",
                    invalidValue, fieldName, Arrays.stream(invalidFormatEx.getTargetType().getEnumConstants())
                            .map(Object::toString).collect(Collectors.joining(", ")));

        }
        // Generic error handling
        return ResponseEntity.badRequest().body(BaseResponse.error(VALIDATION_ERROR, errorMessage));
    }

    @ExceptionHandler(FileHandlingException.class)
    public ResponseEntity<BaseResponse<String>> handleFileHandlingException(FileHandlingException ex) {
        BaseResponse<String> response = BaseResponse.error("Input file handling error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ModelMappingException.class)
    public ResponseEntity<BaseResponse<String>> handleModelMappingException(ModelMappingException ex) {
        BaseResponse<String> response = BaseResponse.error("Data mapping exception", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<BaseResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        BaseResponse<String> response = BaseResponse.error("Resource not found", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
