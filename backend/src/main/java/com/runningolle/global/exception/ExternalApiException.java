package com.runningolle.global.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {

    private final String provider;
    private final Integer upstreamStatusCode;

    public ExternalApiException(String provider, String message) {
        super(message);
        this.provider = provider;
        this.upstreamStatusCode = null;
    }

    public ExternalApiException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.upstreamStatusCode = null;
    }

    public ExternalApiException(String provider, int upstreamStatusCode, String message) {
        super(message);
        this.provider = provider;
        this.upstreamStatusCode = upstreamStatusCode;
    }
}
