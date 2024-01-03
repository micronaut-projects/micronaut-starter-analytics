package io.micronaut.starter.analytics.gcp;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "cloud.sql.connection.name", value = "micronaut-foo:us-central1:foo-postgresdb")
@Property(name = "cloud.sql.connection.ip-types[0]", value = "PUBLIC")
@MicronautTest(startApplication = false)
class CloudSQLConnectionTest {

    @Inject
    BeanContext beanContext;

    @Test
    void beanOfTypeCloudSQLConnectionExists() {
        assertTrue(beanContext.containsBean(CloudSQLConnection.class));
        assertEquals("micronaut-foo:us-central1:foo-postgresdb", beanContext.getBean(CloudSQLConnection.class).name());
    }
}