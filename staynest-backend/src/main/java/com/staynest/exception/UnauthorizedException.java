package com.staynest.exception;

/**
 * Exception thrown when a user attempts an action they are not authorize to perform
 * Result in HTTP 403 Forbidden
 */
public class UnauthorizedException extends RuntimeException{
    /**
     * Constructor with message
     * @param message - Error message describing the authorization failure
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     * @param message - Error message
     * @param cause - Root cause exception
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
