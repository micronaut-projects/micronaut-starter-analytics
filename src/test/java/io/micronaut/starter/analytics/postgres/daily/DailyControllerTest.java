package io.micronaut.starter.analytics.postgres.daily;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.http.*;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.JsonMapper;
import io.micronaut.starter.analytics.postgres.AbstractDataTest;
import io.micronaut.starter.analytics.postgres.Application;
import io.micronaut.starter.analytics.postgres.ApplicationRepository;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "micronaut.security.reject-not-found", value = StringUtils.FALSE)
@Property(name = "spec.name", value = "DailyControllerTest")
@Property(name = "micronaut.http.client.follow-redirects", value = StringUtils.FALSE)
@Property(name = "api.key", value = DailyControllerTest.API_KEY)
class DailyControllerTest extends AbstractDataTest {

    public static final String API_KEY = "xxx";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    JsonMapper jsonMapper;

    @Inject
    TestApplicationRepository applicationRepository;

    @Test
    void dailyFailsWithNoAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpResponse response = assertDoesNotThrow(
                () -> client.exchange(HttpRequest.GET("/analytics/daily").accept(MediaType.TEXT_HTML)));
        assertEquals(HttpStatus.SEE_OTHER, response.getStatus());
        assertEquals("/unauthorized", response.getHeaders().get(HttpHeaders.LOCATION));
    }

    @Test
    void dailyChartWithHeaderAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        String html = assertDoesNotThrow(() -> client.retrieve(createHtmlRequest("/analytics/daily")));
        assertTrue(html.contains("Count"));
        Stream.iterate(0, i -> i + 1).limit(30).map(i -> LocalDate.now().minusDays(i)).forEach(date -> {
            assertTrue(html.contains(date.toString()));
        });
    }

    @Test
    void dailyChartWithQueryAuth() {
        BlockingHttpClient client = httpClient.toBlocking();
        String html = assertDoesNotThrow(() -> client.retrieve(createHtmlRequest("/analytics/daily")));
        assertTrue(html.contains("Count"));
        Stream.iterate(0, i -> i + 1).limit(30).map(i -> LocalDate.now().minusDays(i)).forEach(date -> {
            assertTrue(html.contains(date.toString()));
        });
    }

    @Test
    void checkAccuracy(DailyService dailyService) {
        // I cannot currently find a way to mock or update the date_created field
        List<Application> applications = applicationRepository.saveAll(List.of(
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.DEFAULT, Language.GROOVY, BuildTool.MAVEN, TestFramework.SPOCK, JdkVersion.JDK_21, "4.0.1")
        ));
        applicationRepository.update(applications.get(2).getId(), LocalDate.now().minusDays(1).atStartOfDay());
        applicationRepository.update(applications.get(3).getId(), LocalDate.now().minusDays(2).atStartOfDay());
        applicationRepository.update(applications.get(4).getId(), LocalDate.now().minusDays(2).atStartOfDay());
        applicationRepository.update(applications.get(5).getId(), LocalDate.now().minusDays(2).atStartOfDay());

        checkDailyCounts(dailyService.dailyStats(null, null, null), 30);
        checkDailyCounts(dailyService.dailyStats(null, null, 10), 11);
    }

    private void checkDailyCounts(List<DailyDTO> dailyDTOS, int expected) {
        // The columns are ordered from oldest to newest, so reverse the list
        Collections.reverse(dailyDTOS);
        assertEquals(expected, dailyDTOS.size());
        for (int i = 0; i < expected; i++) {
            if (i == 0) {
                assertEquals(2, dailyDTOS.get(i).count());
            } else if (i == 1) {
                assertEquals(1, dailyDTOS.get(i).count());
            } else if (i == 2) {
                assertEquals(3, dailyDTOS.get(i).count());
            } else {
                assertEquals(0, dailyDTOS.get(i).count());
            }
        }
    }


    @JdbcRepository(dialect = Dialect.POSTGRES)
    @Replaces(ApplicationRepository.class)
    @Requires(property = "spec.name", value = "DailyControllerTest")
    @SuppressWarnings("unused")
    interface TestApplicationRepository extends ApplicationRepository {

        void update(@Id Long id, LocalDateTime dateCreated);
    }

    private MutableHttpRequest<?> createHtmlRequest(String uri) {
        return HttpRequest.GET(uri)
                .accept(MediaType.TEXT_HTML)
                .header("X-API-KEY", API_KEY);
    }
}