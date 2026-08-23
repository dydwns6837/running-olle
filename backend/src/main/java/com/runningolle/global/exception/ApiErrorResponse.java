package com.runningolle.global.exception;

public record ApiErrorResponse(
        String code,
        String message
) {
}
