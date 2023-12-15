package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Map;

import static io.micronaut.starter.analytics.postgres.AnalyticsControllerTest.API_KEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "api.key", value = API_KEY)
@MicronautTest(environments = {Environment.GOOGLE_COMPUTE})
class PercentageControllerTest {

    private static final String API_KEY = "xxx";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    JsonMapper jsonMapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "/analytics/percentages/jdks",
            "/analytics/percentages/buildTools",
            "/analytics/percentages/languages",
            "/analytics/percentages/testFrameworks"
    })
    void apiDoesNotRequireAuthentication(String path) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpResponse<String> response = assertDoesNotThrow(() -> client.exchange(HttpRequest.GET(path), Argument.of(String.class)));
        assertEquals(HttpStatus.OK, response.getStatus());
        assertDoesNotThrow(() -> response.getBody().map(this::parse).orElseThrow());
    }

    @Test
    void checkAccuracy() {
        BlockingHttpClient client = httpClient.toBlocking();
        String setupJson = """
                {"type":"DEFAULT","language":"java","testFramework":"junit","buildTool":"gradle","jdkVersion":"JDK_17"}
                {"type":"DEFAULT","language":"java","testFramework":"spock","buildTool":"gradle_kotlin","jdkVersion":"JDK_21"}
                {"type":"FUNCTION","language":"groovy","testFramework":"spock","buildTool":"gradle_kotlin","jdkVersion":"JDK_17"}
                {"type":"FUNCTION","language":"kotlin","testFramework":"kotest","buildTool":"maven","jdkVersion":"JDK_17"}
                """;
        setupJson.lines().forEach(line -> {
            assertDoesNotThrow(() -> client.exchange(decorateRequest(HttpRequest.POST("/analytics/report", line))));
        });

        assertPercentage(client, "/analytics/percentages/types", Map.of("FUNCTION", 0.5d, "DEFAULT", 0.5d));
        assertPercentage(client, "/analytics/percentages/jdks", Map.of("JDK_21", 0.25d, "JDK_17", 0.75d));
        assertPercentage(client, "/analytics/percentages/buildTools", Map.of("gradle_kotlin", 0.5d, "gradle", 0.25d, "maven", 0.25d));
        assertPercentage(client, "/analytics/percentages/languages", Map.of("java", 0.5d, "groovy", 0.25d, "kotlin", 0.25d));
        assertPercentage(client, "/analytics/percentages/testFrameworks", Map.of("spock", 0.5d, "junit", 0.25d, "kotest", 0.25d));
    }

    void assertPercentage(BlockingHttpClient client, String path, Map<String, Double> expected) {
        HttpResponse<String> response = assertDoesNotThrow(() -> client.exchange(HttpRequest.GET(path), Argument.of(String.class)));
        PercentageResponse percentages = response.getBody().map(this::parse).orElseThrow();
        assertEquals(expected.size(), percentages.percentages().size(), () -> "Expected " + expected + " from " + path + " but got " + percentages.percentages());
        expected.forEach((name, percentage) ->
                // assert equals with a 0.01 delta (as doubles are not exact)
                assertEquals(percentage, percentages.percentageFor(name).orElseThrow(), 0.01d, () -> "Expected " + percentage + " from " + path + " for " + name + " but got " + percentages.percentages())
        );
    }

    @SuppressWarnings("unchecked")
    private PercentageResponse parse(String body) {
        try {
            Map<String, Map<String, Double>> map = jsonMapper.readValue(body, Map.class);
            Map<String, Double> contents = map.get("percentages");
            return new PercentageResponse(
                    contents.entrySet().stream().map(e -> new PercentageDTO(e.getKey(), e.getValue())).toList()
            );
        } catch (IOException e) {
            return null;
        }
    }

    private static MutableHttpRequest<?> decorateRequest(MutableHttpRequest<?> request) {
        return request.header("X-API-KEY", API_KEY);
    }
}