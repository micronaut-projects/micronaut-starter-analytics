package io.micronaut.starter.analytics.filters;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.simple.SimpleHttpHeaders;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LoggingHeadersFilterTest extends LoggingHeadersFilter {
    @Test
    void sensistiveHeadersAreNotLogged() {
        Map<String, String> headersMap = Map.of("host", "micronaut-foo-bar-yyyy-uc.a.run.app",
                "x-api-key", "xxx",
                "accept", "application/json",
                "authorization", "Bearer yyy",
                "content-type", "application/json",
                "content-length", "523",
                "x-forwarded-for", "107.178.207.38",
                "x-forwarded-proto", "https",
                "forwarded:for", "\"107.178.207.38\";proto=https");
        HttpHeaders headers = new SimpleHttpHeaders(headersMap, null);
        LoggingHeadersFilterReplacement filter = new LoggingHeadersFilterReplacement();
        filter.logHeaders(headers);
        assertFalse(filter.headers.containsKey(HttpHeaders.AUTHORIZATION));
        assertFalse(filter.headers.containsKey("X-API-KEY"));
        assertFalse(filter.headers.containsKey("authorization"));
        assertFalse(filter.headers.containsKey("x-api-key"));
        assertTrue(filter.headers.containsKey("host"));

    }
}