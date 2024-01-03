package io.micronaut.starter.analytics.filters;

import io.micronaut.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

public class LoggingHeadersFilterReplacement extends LoggingHeadersFilter {

    Map<String, String> headers = new HashMap<>();

    @Override
    protected void log(HttpMethod method, String path, String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }
}
