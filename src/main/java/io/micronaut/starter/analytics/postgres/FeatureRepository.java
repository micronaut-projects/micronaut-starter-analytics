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
    public List<TotalDTO> topFeatures() {
        return this.jdbcOperations
                .prepareStatement(query("name", "feature"),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    public List<TotalDTO> topLanguages() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_LANGUAGE, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    public List<TotalDTO> topBuildTools() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_BUILD_TOOL, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    public List<TotalDTO> topTestFrameworks() {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_TEST_FRAMEWORK, TABLE_APPLICATION),
                        statement -> {
                            try (ResultSet resultSet = statement.executeQuery()) {
                                return resultSetToTotals(resultSet);
                            }
                        });
    }

    @ReadOnly
    public List<TotalDTO> topJdkVersion() {
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
}
