package com.runningolle.global.client;

import com.runningolle.global.exception.ExternalApiException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

public final class ExternalApiRestClientSupport {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private ExternalApiRestClientSupport() {
    }

    public static RestClient restClient(String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public static RestClient.ResponseSpec.ErrorHandler errorHandler(String provider) {
        return (request, response) -> {
            throw new ExternalApiException(
                    provider,
                    response.getStatusCode().value(),
                    buildUpstreamErrorMessage(provider, response)
            );
        };
    }

    private static String buildUpstreamErrorMessage(String provider, ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            return provider + " API 호출에 실패했습니다.";
        }
        return provider + " API 호출에 실패했습니다. upstreamStatus=" + response.getStatusCode().value();
    }
}
