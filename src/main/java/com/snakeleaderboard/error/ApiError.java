package com.snakeleaderboard.error;

import java.time.Instant;

/**
 * Standard error payload returned by the API.
 *
 * @param timestamp server timestamp when the error was generated
 * @param status HTTP status code
 * @param error HTTP reason phrase
 * @param message human-readable error message
 * @param path request path
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
