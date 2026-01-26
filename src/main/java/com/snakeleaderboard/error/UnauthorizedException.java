package com.snakeleaderboard.error;

/**
 * Raised when a request is not authorized to perform an action.
 *
 * <p>This is mapped to HTTP 401 by {@link ApiExceptionHandler}.</p>
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
