package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.data.jdbc.operations.JdbcRepositoryOperations;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.analytics.postgres.daily.DailyDTO;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "api.key", value = DailyControllerTest.API_KEY)
class DailyControllerTest extends AbstractDataTest {

    public static final String API_KEY = "xxx";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    JsonMapper jsonMapper;

    @Inject
    JdbcRepositoryOperations jdbcOperations;

    @Test
    void dailyFailsWithNoAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        MutableHttpRequest<Object> get = HttpRequest.GET("/analytics/daily");
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> client.retrieve(get));
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());
    }

    @Test
    void dailyChartWithHeaderAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        String html = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET("/analytics/daily").header("X-API-KEY", API_KEY)));
        assertTrue(html.contains("Count"));
        Stream.iterate(0, i -> i + 1).limit(30).map(i -> LocalDate.now().minusDays(i)).forEach(date -> {
            assertTrue(html.contains(date.toString()));
        });
    }

    @Test
    void dailyChartWithQueryAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        String html = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET("/analytics/daily?api=" + API_KEY)));
        assertTrue(html.contains("Count"));
        Stream.iterate(0, i -> i + 1).limit(30).map(i -> LocalDate.now().minusDays(i)).forEach(date -> {
            assertTrue(html.contains(date.toString()));
        });
    }

    @Test
    void checkAccuracy() {
        // I cannot currently find a way to mock or update the date_created field
        applicationRepository.saveAll(List.of(
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1")
        ));

        BlockingHttpClient client = httpClient.toBlocking();
        checkDailyCounts("/analytics/daily/counts", client, 30);
        checkDailyCounts("/analytics/daily/counts?days=10", client, 10);
    }

    private void checkDailyCounts(String path, BlockingHttpClient client, int expected) {
        List<DailyDTO> dailyDTOS = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET(path).header("X-API-KEY", API_KEY), Argument.listOf(DailyDTO.class)));
        // The columns are ordered from oldest to newest, so reverse the list
        Collections.reverse(dailyDTOS);
        assertEquals(expected, dailyDTOS.size());
        for (int i = 0; i < expected; i++) {
            if (i == 0) {
                assertEquals(4, dailyDTOS.get(i).count());
            } else {
                assertEquals(0, dailyDTOS.get(i).count());
            }
        }
    }
}