package io.micronaut.starter.analytics.gcp;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "cloud.sql.connection.name", value = "micronaut-foo:us-central1:foo-postgresdb")
@Property(name = "db.name", value = "foo")
@Property(name = "cloud.sql.connection.ip-types[0]", value = "PUBLIC")
@Property(name = "cloud.sql.connection.ip-types[1]", value = "PRIVATE")
@MicronautTest(startApplication = false)
class DatasourceConfigurationPopulatorTest {

    @Inject
    DatasourceConfigurationPopulator populator;

    @Test
    void popoulateDataSourceConfiguration() {
        DatasourceConfiguration config = new DatasourceConfiguration("default");
        assertDoesNotThrow(() -> populator.populate(config));
        assertEquals("jdbc:postgresql:///foo", config.getJdbcUrl());
        assertEquals("com.google.cloud.sql.postgres.SocketFactory", config.getDataSourceProperties().get("socketFactory"));
        assertEquals("micronaut-foo:us-central1:foo-postgresdb", config.getDataSourceProperties().get("cloudSqlInstance"));
        assertEquals("PUBLIC,PRIVATE", config.getDataSourceProperties().get("ipTypes"));
    }
}