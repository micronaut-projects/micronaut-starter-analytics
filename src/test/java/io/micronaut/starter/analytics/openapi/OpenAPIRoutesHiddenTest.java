package io.micronaut.starter.analytics.openapi;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Property(name = "endpoints.routes.enabled", value = StringUtils.TRUE)
@Property(name = "endpoints.routes.sensitive", value = StringUtils.FALSE)
@MicronautTest
class OpenAPIRoutesHiddenTest {

    private static final List<String> ALLOWED_PATHS_IDS = Stream.of(
            "/analytics/report",
            "/analytics/top/buildTools",
            "/analytics/top/features",
            "/analytics/top/jdks",
            "/analytics/top/languages",
            "/analytics/top/testFrameworks"
    ).map(str -> str + ":").toList();

    @Test
    void hiddenRoutesTest(@Client("/") HttpClient httpClient) {
        BlockingHttpClient client = httpClient.toBlocking();
        String yml = assertDoesNotThrow(() -> client.retrieve("/swagger/micronaut-launch-analytics-1.0.yml"));
        List<String> routes = routes(client);
        for (String route : routes) {
            String pathId = route + ":";
            if (ALLOWED_PATHS_IDS.contains(pathId)) {
                continue;
            }
            assertFalse(yml.contains(pathId));
        }
    }

    private List<String> routes(BlockingHttpClient client) {
        Map<String, Object> routes = assertDoesNotThrow(() -> client.retrieve(HttpRequest.GET("/routes"), Argument.mapOf(String.class, Object.class)));
        Set<String> routesKey = routes.keySet();
        return routesKey.stream().map(k -> {
                    String str = k.substring(k.indexOf("[") + 1);
                    str = str.substring(0, str.indexOf("]"));
                    return str;
                }).distinct()
                .toList();
    }
}
