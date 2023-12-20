package io.micronaut.starter.analytics.configuration;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "micronaut.starter.analytics.days", value = "7")
@Property(name = "micronaut.starter.analytics.max-days", value = "31")
@MicronautTest(startApplication = false)
class DailyConfigurationTest {

    @Test
    void dailyConfigurationCanBePopulatedViaConfiguration(StarterAnalyticsConfiguration configuration) {
        assertEquals(7, configuration.getDays());
        assertEquals(31, configuration.getMaxDays());
    }

}