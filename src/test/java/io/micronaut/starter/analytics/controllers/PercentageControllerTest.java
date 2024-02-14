package io.micronaut.starter.analytics.controllers;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.analytics.repositories.AbstractDataTest;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.services.percentages.PercentageDTO;
import io.micronaut.starter.analytics.services.percentages.PercentageResponse;
import io.micronaut.starter.analytics.services.percentages.PercentageService;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Property(name = "api.key", value = AnalyticsControllerTest.API_KEY)
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
        LocalDate from = LocalDate.now().minusDays(30);
        assertPercentage("jdks", percentageService.jdks(from), Map.entry("JDK_21", 0.25d), Map.entry("JDK_17", 0.75d));
        assertPercentage("gradleDsl", percentageService.gradleDsl(from), Map.entry("kotlin", 0.6d), Map.entry("groovy", 0.4d));
        assertPercentage("buildTool", percentageService.buildTool(from), Map.entry("gradle", 0.625d), Map.entry("maven", 0.375d));
        assertPercentage("buildToolJava", percentageService.buildTool(from, Language.JAVA), Map.entry("gradle", 0.66d), Map.entry("maven", 0.33d));
        assertPercentage("buildToolGroovy", percentageService.buildTool(from, Language.GROOVY), Map.entry("gradle", 0.5d), Map.entry("maven", 0.5d));
        assertPercentage("buildToolKotlin", percentageService.buildTool(from, Language.KOTLIN), Map.entry("gradle", 0.66d), Map.entry("maven", 0.33d));
        assertPercentage("languages", percentageService.languages(from), Map.entry("java", 0.375d), Map.entry("groovy", 0.25d), Map.entry("kotlin", 0.375d));
        assertPercentage("test frameworks", percentageService.testFrameworks(from), Map.entry("spock", 0.5d), Map.entry("junit", 0.125d), Map.entry("kotest", 0.375d));
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
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.MAVEN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.MAVEN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.GRADLE, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1"),
                    new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.GRADLE_KOTLIN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1")
            ));
        }
    }
}