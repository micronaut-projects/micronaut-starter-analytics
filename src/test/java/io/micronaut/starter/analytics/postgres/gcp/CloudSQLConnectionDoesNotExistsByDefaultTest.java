package io.micronaut.starter.analytics.postgres.gcp;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class CloudSQLConnectionDoesNotExistsByDefaultTest {

    @Inject
    BeanContext beanContext;

    @Test
    void beanOfTypeCloudSQLConnectionExists() {
        assertFalse(beanContext.containsBean(CloudSQLConnection.class));
    }
}