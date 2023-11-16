package io.micronaut.starter.analytics.postgres.gcp;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "db.name", value = "foo")
@MicronautTest
class DbConfigurationTest {

    @Inject
    BeanContext beanContext;

    @Test
    void beanOfTypeCloudSQLConnectionExists() {
        assertTrue(beanContext.containsBean(DbConfiguration.class));
        assertEquals("foo", beanContext.getBean(DbConfiguration.class).name());
    }
}