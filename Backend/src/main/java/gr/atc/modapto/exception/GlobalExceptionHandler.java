package gr.atc.modapto.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import gr.atc.modapto.controller.ApiResponseInfo;
import gr.atc.modapto.exception.CustomExceptions.OrderNotFoundException;
import gr.atc.modapto.exception.CustomExceptions.PaginationException;

/*
 * Exception Handling Responses
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponseInfo<String>> handleOrderNotFoundException(OrderNotFoundException ex) {
        ApiResponseInfo<String> response = ApiResponseInfo.error("Order not found", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaginationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseInfo<String>> handlePaginationException(PaginationException ex) {
        ApiResponseInfo<String> response = ApiResponseInfo.error("Invalid pagination parameters were given", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleValidationExceptions(HandlerMethodValidationException ex) {
        List<String> errorMessages = ex.getAllValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream())
            .map(error -> error.getDefaultMessage())
            .toList();
            
            ApiResponseInfo<String> response = ApiResponseInfo.error("Validation Error", errorMessages);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
