package io.micronaut.starter.analytics.percentages;

import io.micronaut.json.JsonMapper;
import io.micronaut.starter.analytics.services.percentages.PercentageDTO;
import io.micronaut.starter.analytics.services.percentages.PercentageResponse;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class PercentageResponseTest {

    @Test
    void renderPercentageResponse(JsonMapper jsonMapper) throws IOException {
        String json = jsonMapper.writeValueAsString(new PercentageResponse(List.of(new PercentageDTO("maven", 30), new PercentageDTO("gradle", 70))));
        String expected = """
                {"percentages":{"gradle":70.0,"maven":30.0}}""";
        assertEquals(expected, json);
    }
}