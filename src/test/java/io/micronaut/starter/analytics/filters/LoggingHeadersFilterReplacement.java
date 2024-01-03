package io.micronaut.starter.analytics.filters;

import java.util.HashMap;
import java.util.Map;

public class LoggingHeadersFilterReplacement extends LoggingHeadersFilter {

    Map<String, String> headers = new HashMap<>();

    @Override
    protected void log(String headerName, String headerValue) {
        headers.put(headerName, headerValue);
    }
}
