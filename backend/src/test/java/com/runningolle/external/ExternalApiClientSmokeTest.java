package com.runningolle.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.runningolle.domain.place.client.KakaoPlaceClient;
import com.runningolle.domain.routing.client.OpenRouteServiceClient;
import com.runningolle.domain.routing.client.OpenRouteServiceClient.Waypoint;
import com.runningolle.domain.tourism.client.TourApiClient;
import com.runningolle.global.config.properties.ExternalApiProperties;
import java.util.List;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

@EnabledIfSystemProperty(named = "runningolle.external-smoke", matches = "true")
class ExternalApiClientSmokeTest {

    private static final double JEJU_CITY_HALL_LAT = 33.4996213;
    private static final double JEJU_CITY_HALL_LNG = 126.5311884;

    @Test
    void kakaoPlaceClientCanSearchJejuPlaces() {
        ExternalApiProperties properties = properties();
        assumeTrue(StringUtils.hasText(properties.getKakaoMapApiKey()), "Kakao REST API key is required.");

        KakaoPlaceClient client = new KakaoPlaceClient(properties);

        assertThat(client.searchKeyword("제주 카페", JEJU_CITY_HALL_LAT, JEJU_CITY_HALL_LNG, 5_000))
                .isNotEmpty();
    }

    @Test
    void tourApiClientCanSearchNearbyTourismItems() {
        ExternalApiProperties properties = properties();
        assumeTrue(StringUtils.hasText(properties.getTourApiKey()), "TourAPI service key is required.");

        TourApiClient client = new TourApiClient(properties);

        assertThat(client.findNearbyTourism(JEJU_CITY_HALL_LAT, JEJU_CITY_HALL_LNG, 10_000))
                .isNotNull();
    }

    @Test
    void tourApiClientCanLoadJejuAreaBasedTourismItems() {
        ExternalApiProperties properties = properties();
        assumeTrue(StringUtils.hasText(properties.getTourApiKey()), "TourAPI service key is required.");

        TourApiClient client = new TourApiClient(properties);

        var page = client.getAreaBasedList("39", "12", 1, 5);

        assertThat(page.items()).isNotNull();
        assertThat(page.totalCount()).isNotNegative();
    }

    @Test
    void openRouteServiceClientCanCalculateJejuWalkingRoute() {
        ExternalApiProperties properties = properties();
        assumeTrue(StringUtils.hasText(properties.getOpenRouteServiceKey()), "OpenRouteService API key is required.");

        OpenRouteServiceClient client = new OpenRouteServiceClient(properties);

        var result = client.calculateFootWalkingRoute(List.of(
                new Waypoint("제주시청", JEJU_CITY_HALL_LAT, JEJU_CITY_HALL_LNG),
                new Waypoint("용두암", 33.5161104, 126.5119574)
        ));

        assertThat(result.distanceKm()).isPositive();
        assertThat(result.routeLineStringWkt()).startsWith("LINESTRING");
    }

    private static ExternalApiProperties properties() {
        ExternalApiProperties properties = new ExternalApiProperties();
        properties.setKakaoMapApiKey(readSecret(
                "RUNNING_OLLE_KAKAO_REST_API_KEY",
                "kakaoRestApiKey",
                "external-api.kakao-map-api-key"
        ));
        properties.setTourApiKey(readSecret(
                "RUNNING_OLLE_TOUR_API_KEY",
                "tourApiKey",
                "external-api.tour-api-key"
        ));
        properties.setOpenRouteServiceKey(readSecret(
                "RUNNING_OLLE_ORS_API_KEY",
                "openRouteServiceKey",
                "external-api.open-route-service-key"
        ));
        return properties;
    }

    private static String readSecret(String environmentVariable, String systemProperty, String yamlProperty) {
        String value = System.getenv(environmentVariable);
        if (StringUtils.hasText(value)) {
            return value;
        }
        value = System.getProperty(systemProperty);
        if (StringUtils.hasText(value)) {
            return value;
        }

        Properties localSecret = loadLocalSecretProperties();
        return localSecret.getProperty(yamlProperty);
    }

    private static Properties loadLocalSecretProperties() {
        YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlPropertiesFactoryBean.setResources(new FileSystemResource("src/main/resources/application-secret.yml"));

        Properties properties = yamlPropertiesFactoryBean.getObject();
        return properties == null ? new Properties() : properties;
    }
}
