package io.micronaut.starter.analytics.postgres;

import io.micronaut.data.model.query.builder.sql.Dialect;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgreSQL {

    private static PostgreSQLContainer postgres;

    public static Map<String, String> getProperties() {
        if (postgres == null) {
             postgres = new PostgreSQLContainer<>("postgres:10")
                    .withDatabaseName("test-database")
                    .withUsername("test")
                    .withPassword("test");
        }
        if (!postgres.isRunning()) {
            postgres.start();
        }
        return Map.of("datasources.default.url", postgres.getJdbcUrl(),
                "datasources.default.username", postgres.getUsername(),
                "datasources.default.password", postgres.getPassword(),
                "datasources.default.dialect", Dialect.POSTGRES.name());
    }
}
