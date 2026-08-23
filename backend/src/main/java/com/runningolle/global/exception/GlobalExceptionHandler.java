package com.runningolle.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalApiException(ExternalApiException exception) {
        log.warn(
                "External API call failed. provider={}, upstreamStatusCode={}",
                exception.getProvider(),
                exception.getUpstreamStatusCode(),
                exception
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorResponse("EXTERNAL_API_ERROR", exception.getMessage()));
    }
}
