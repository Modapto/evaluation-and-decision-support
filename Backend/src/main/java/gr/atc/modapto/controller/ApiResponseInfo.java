package gr.atc.modapto.controller;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class ApiResponseInfo<T> {
    private Boolean success;
    private String message;
    private T data;
    private Object errors;
    @Builder.Default
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public static <T> ApiResponseInfo<T> success(T data) {
        return ApiResponseInfo.<T>builder()
                .success(true)
                .message("Operation successful")
                .data(data)
                .build();
    }

    public static <T> ApiResponseInfo<T> success(T data, String message) {
        return ApiResponseInfo.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseInfo<T> error(String message) {
        return ApiResponseInfo.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponseInfo<T> error(String message, Object errors) {
        return ApiResponseInfo.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
}