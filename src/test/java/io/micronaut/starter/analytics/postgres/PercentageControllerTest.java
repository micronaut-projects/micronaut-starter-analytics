package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.analytics.postgres.percentages.PercentageDTO;
import io.micronaut.starter.analytics.postgres.percentages.PercentageResponse;
import io.micronaut.starter.analytics.postgres.percentages.PercentageService;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.micronaut.starter.analytics.postgres.AnalyticsControllerTest.API_KEY;
import static org.junit.jupiter.api.Assertions.*;

@Property(name = "api.key", value = API_KEY)
class PercentageControllerTest extends AbstractDataTest {

    public static final String API_KEY = "xxx";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    JsonMapper jsonMapper;
    
    @Test
    void htmlPercentages() {
        seedData();
        BlockingHttpClient client = httpClient.toBlocking();
        String html = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET("/analytics/percentages")));
        assertTrue(html.contains("Build tools"));
        assertTrue(html.contains("Gradle DSLs"));
        assertTrue(html.contains("Java versions"));
        assertTrue(html.contains("Programming languages"));
        assertTrue(html.contains("Test frameworks"));
        assertFalse(html.contains("Excel"));
        URI uri = UriBuilder.of("/analytics").path("percentages").build();
        html = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET(uri).header("X-API-KEY", API_KEY)));
        assertTrue(html.contains("Excel"));
        assertTrue(html.contains("/analytics/excel"));
        assertFalse(html.contains("JDK_17"));
        assertTrue(html.contains("JUnit 5"));
    }

    @Test
    void checkAccuracy(PercentageService percentageService) {
        seedData();
        assertPercentage("jdks", percentageService.jdks(), Map.entry("JDK_21", 0.25d), Map.entry("JDK_17", 0.75d));
        assertPercentage("gradleDsl", percentageService.gradleDsl(), Map.entry("kotlin", 0.66d), Map.entry("groovy", 0.33d));
        assertPercentage("buildTool", percentageService.buildTool(), Map.entry("gradle", 0.75d), Map.entry("maven", 0.25d));
        assertPercentage("languages", percentageService.languages(), Map.entry("java", 0.5d), Map.entry("groovy", 0.25d), Map.entry("kotlin", 0.25d));
        assertPercentage("test frameworks", percentageService.testFrameworks(), Map.entry("spock", 0.5d), Map.entry("junit", 0.25d), Map.entry("kotest", 0.25d));
    }

    @SafeVarargs
    private void assertPercentage(String name, PercentageResponse percentages, Map.Entry<String, Double> first, Map.Entry<String, Double>... rest) {
        List<Map.Entry<String, Double>> expected = new ArrayList<>(Arrays.asList(rest));
        expected.add(0, first);
        assertEquals(expected.size(), percentages.percentages().size(), () -> "Expected " + expected + " from " + name + " but got " + percentages.percentages());
        expected.forEach(entry ->
                // assert equals with a 0.01 delta (as doubles are not exact)
                assertEquals(entry.getValue(), percentages.percentageFor(entry.getKey()).orElseThrow(), 0.01d, () -> "Expected " + entry.getValue() + " from " + name + " for " + entry.getKey() + " but got " + percentages.percentages())
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

    private void seedData() {
        if (applicationRepository.count() == 0) {
            applicationRepository.saveAll(List.of(
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1")
            ));
        }
    }
}