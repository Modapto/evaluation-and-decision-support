package gr.atc.modapto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
        ApiResponseInfo<String> response = ApiResponseInfo.error(null, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PaginationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponseInfo<String>> handlePaginationException(PaginationException ex) {
        ApiResponseInfo<String> response = ApiResponseInfo.error(null, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
