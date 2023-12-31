package io.micronaut.starter.analytics.controllers;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.starter.analytics.repositories.AbstractDataTest;
import io.micronaut.starter.analytics.services.TotalDTO;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static io.micronaut.starter.analytics.controllers.AnalyticsControllerTest.API_KEY;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "api.key", value = API_KEY)
class AnalyticsControllerTest extends AbstractDataTest {

    public static final String API_KEY = "xxx";

    @Inject
    @Client("/")
    HttpClient httpClient;

    @ParameterizedTest
    @ValueSource(strings = {
            "/analytics/top/features",
            "/analytics/top/jdks",
            "/analytics/top/buildTools",
            "/analytics/top/languages",
            "/analytics/top/testFrameworks"
    })
    void apiRequiresAuthentication(String path) {
        BlockingHttpClient client = httpClient.toBlocking();
        HttpResponse<List<TotalDTO>> response = assertDoesNotThrow(() -> client.exchange(HttpRequest.GET(path).header("X-API-KEY", API_KEY), Argument.listOf(TotalDTO.class)));
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<List<TotalDTO>> bodyOptional = response.getBody(Argument.listOf(TotalDTO.class));
        assertTrue(bodyOptional.isPresent());
    }

    @Test
    void saveApplication() {
        BlockingHttpClient client = httpClient.toBlocking();
        String json = "{\"type\":\"DEFAULT\",\"language\":\"java\",\"testFramework\":\"junit\",\"buildTool\":\"gradle\",\"jdkVersion\":\"JDK_17\"}";
        assertDoesNotThrow(() -> client.exchange(decorateRequest(HttpRequest.POST("/analytics/report", json))));
        List<TotalDTO> totalDTOList = totalDtoRequest(client, "/analytics/top/jdks");
        assertFalse(totalDTOList.isEmpty());
        assertTrue(totalDTOList.stream().anyMatch(dto -> dto.getName().equals("JDK_17") && dto.getTotal() == 1));

        totalDTOList = totalDtoRequest(client, "/analytics/top/languages");
        assertFalse(totalDTOList.isEmpty());
        assertEquals("java", totalDTOList.get(0).getName());
        assertEquals(1, totalDTOList.get(0).getTotal());

        totalDTOList = totalDtoRequest(client, "/analytics/top/buildTools");
        assertFalse(totalDTOList.isEmpty());
        assertEquals("gradle", totalDTOList.get(0).getName());
        assertEquals(1, totalDTOList.get(0).getTotal());

        totalDTOList = totalDtoRequest(client, "/analytics/top/testFrameworks");
        assertFalse(totalDTOList.isEmpty());
        assertEquals("junit", totalDTOList.get(0).getName());
        assertEquals(1, totalDTOList.get(0).getTotal());
    }

    private static List<TotalDTO> totalDtoRequest(BlockingHttpClient client, String path) {
        HttpResponse<List<TotalDTO>> response = assertDoesNotThrow(() -> client.exchange(decorateRequest(HttpRequest.GET(path)), Argument.listOf(TotalDTO.class)));
        assertEquals(HttpStatus.OK, response.getStatus());
        Optional<List<TotalDTO>> bodyOptional = response.getBody(Argument.listOf(TotalDTO.class));
        assertTrue(bodyOptional.isPresent());
        return bodyOptional.get();
    }

    private static MutableHttpRequest<?> decorateRequest(MutableHttpRequest<?> request) {
        request.header("X-API-KEY", API_KEY);
        return request;
    }
}