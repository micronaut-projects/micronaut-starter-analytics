/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.analytics.postgres;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.transaction.annotation.ReadOnly;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public abstract class FeatureRepository implements CrudRepository<Feature, Long> {

    private static final String FIELD_APPLICATION_TYPE = "type";
    private static final String FIELD_BUILD_TOOL = "build_tool";
    private static final String FIELD_JDK_VERSION = "jdk_version";
    private static final String FIELD_LANGUAGE = "language";
    private static final String FIELD_TEST_FRAMEWORK = "test_framework";

    private static final String TABLE_APPLICATION = "application";

    private final JdbcOperations jdbcOperations;

    FeatureRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @ReadOnly
    List<TotalDTO> topFeatures() {
        return this.jdbcOperations
                .prepareStatement(query("name", "feature"),
                        statement -> {
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetToTotals(resultSet);
            }
        });
    }

    @ReadOnly
    List<TotalDTO> topLanguages() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_LANGUAGE, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    List<TotalDTO> topBuildTools() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_BUILD_TOOL, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    List<TotalDTO> topTestFrameworks() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_TEST_FRAMEWORK, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    List<TotalDTO> topJdkVersion() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_JDK_VERSION, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    private List<TotalDTO> resultSetToTotals(ResultSet resultSet) throws SQLException {
        List<TotalDTO> results = new ArrayList<>(40);
        while (resultSet.next()) {
            results.add(
                    new TotalDTO(
                        resultSet.getString("name"),
                        resultSet.getLong("total")
                    )
            );
        }
        return results;
    }

    private String query(String name, String table) {
        return "SELECT " + name + " AS name, count(*) AS total FROM " + table + " GROUP BY name ORDER BY total";
    }

    @ReadOnly
    List<PercentageDTO> applicationTypePercentages() {
        return getPercentages(FIELD_APPLICATION_TYPE);
    }

    @ReadOnly
    List<PercentageDTO> buildToolPercentages() {
        return getPercentages(FIELD_BUILD_TOOL);
    }

    @ReadOnly
    List<PercentageDTO> jdkPercentages() {
        return getPercentages(FIELD_JDK_VERSION);
    }

    @ReadOnly
    List<PercentageDTO> languagePercentages() {
        return getPercentages(FIELD_LANGUAGE);
    }

    @ReadOnly
    List<PercentageDTO> testFrameworkPercentages() {
        return getPercentages(FIELD_TEST_FRAMEWORK);
    }

    private List<PercentageDTO> getPercentages(String name) {
        return this.jdbcOperations
                .prepareStatement(percentageQuery(name), statement -> {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        List<PercentageDTO> results = new ArrayList<>(40);
                        while (resultSet.next()) {
                            results.add(
                                    new PercentageDTO(
                                            resultSet.getString("name"),
                                            resultSet.getDouble("percentage")
                                    )
                            );
                        }
                        return results;
                    }
                });
    }

    private String percentageQuery(String name) {
        return """
                WITH totals AS (
                    SELECT %s AS name, count(%s) AS total FROM application GROUP BY name
                ),
                summed_totals AS (
                    SELECT SUM(total) AS total FROM totals
                )
                SELECT totals.name, ROUND(totals.total / summed_totals.total, 4) AS percentage
                FROM totals, summed_totals
                """.formatted(name, name);
    }
}
