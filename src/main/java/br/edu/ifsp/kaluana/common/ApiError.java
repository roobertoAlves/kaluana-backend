package br.edu.ifsp.kaluana.common;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String message,
        OffsetDateTime timestamp,
        Map<String, String> fieldErrors
) {

    public static ApiError of(int status, String message) {
        return new ApiError(status, message, OffsetDateTime.now(), null);
    }

    public static ApiError ofFieldErrors(int status, String message, Map<String, String> fieldErrors) {
        return new ApiError(status, message, OffsetDateTime.now(), fieldErrors);
    }
}
