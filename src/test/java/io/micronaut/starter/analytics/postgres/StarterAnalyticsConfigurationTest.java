package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "flyway.datasources.default.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class StarterAnalyticsConfigurationTest {

    @Inject
    StarterAnalyticsConfiguration analyticsConfiguration;

    @Test
    void pageSizeDefaultsToFifty() {
        assertEquals(50, analyticsConfiguration.getPageSize());
    }
}