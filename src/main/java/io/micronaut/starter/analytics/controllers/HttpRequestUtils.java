package io.micronaut.starter.analytics.controllers;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;

public final class HttpRequestUtils {
    private HttpRequestUtils() {

    }

    public static boolean accepts(@NonNull HttpRequest<?> request,
                                   @NonNull MediaType mediaType) {
        return request.getHeaders()
                .accept()
                .stream()
                .anyMatch(it -> it.getName().contains(mediaType));
    }
}
