package com.staynest.exception;

/**
 * Exception thrown when a requested resources is not found
 * Result in HTTP 404 not found
 */
public class ResourceNotFoundException extends RuntimeException{
    /**
     * Constructor with message
     * @Param message - Error message description  what was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * constructor with message and cause
     * @param message - Error message
     * @param cause - Root cause exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
