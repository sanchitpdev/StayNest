package com.staynest.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response format
 * Used by GlobalExceptionHandler to return consistent error message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    //TimeStamp when error occurred
    private LocalDateTime timeStamp;

    //HTTP status code
    private Integer status;

    //HTTP status text
    private String error;

    //Detailed error message
    private String message;

    //Request path when error occurred
    private String path;

    public ErrorResponse(Integer status, String error, String message, String path) {
        this.timeStamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
