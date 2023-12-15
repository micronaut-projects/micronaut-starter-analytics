package io.micronaut.starter.analytics.postgres;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentageControllerTest extends AbstractDataTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    JsonMapper jsonMapper;

    @ParameterizedTest
    @ValueSource(strings = {
            "/analytics/percentages/types",
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
        applicationRepository.saveAll(List.of(
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1")
        ));

        BlockingHttpClient client = httpClient.toBlocking();

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
}