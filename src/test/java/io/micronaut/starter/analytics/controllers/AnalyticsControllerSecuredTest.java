package io.micronaut.starter.analytics.controllers;

import io.micronaut.context.env.Environment;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@MicronautTest(environments = {Environment.GOOGLE_COMPUTE})
class AnalyticsControllerSecuredTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @ParameterizedTest
    @ValueSource(strings = {"/analytics/top/features", "/analytics/top/jdks", "/analytics/top/buildTools", "/analytics/top/languages", "/analytics/top/testFrameworks"})
    void apiRequiresAuthentication(String path) {
        BlockingHttpClient client = httpClient.toBlocking();
        Executable e = () -> client.retrieve(path);
        HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, e);
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatus());
    }
}