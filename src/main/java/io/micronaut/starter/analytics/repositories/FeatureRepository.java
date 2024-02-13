/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.starter.analytics.repositories;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.jdbc.runtime.PreparedStatementCallback;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.entities.Feature;
import io.micronaut.starter.analytics.services.TotalDTO;
import io.micronaut.starter.options.Language;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@JdbcRepository(dialect = Dialect.POSTGRES)
public abstract class FeatureRepository implements CrudRepository<Feature, Long> {
    private static final String FIELD_BUILD_TOOL = "build_tool";
    private static final String FIELD_JDK_VERSION = "jdk_version";
    private static final String FIELD_LANGUAGE = "language";
    private static final String FIELD_TEST_FRAMEWORK = "test_framework";
    private static final String TABLE_APPLICATION = Application.class.getSimpleName().toLowerCase();
    private static final String TABLE_FEATURE = Feature.class.getSimpleName().toLowerCase();
    private static final Logger LOG = LoggerFactory.getLogger(FeatureRepository.class);

    private static final List<String> TABLE_APPLICATION_FIELDS = List.of(FIELD_BUILD_TOOL, FIELD_JDK_VERSION, FIELD_LANGUAGE, FIELD_TEST_FRAMEWORK);
    private static final List<String> EXISTING_TABLES = List.of(
            Feature.class.getSimpleName().toLowerCase(),
            TABLE_APPLICATION);
    private final JdbcOperations jdbcOperations;

    FeatureRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @ReadOnly
    public List<TotalDTO> topFeatures(@NonNull @NotNull @Past LocalDate from) {
        return this.jdbcOperations.prepareStatement(query("name", "feature", from), callback(from));
    }

    @ReadOnly
    public List<TotalDTO> topLanguages(@NonNull @NotNull @Past LocalDate from) {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_LANGUAGE, TABLE_APPLICATION, from), callback(from));
    }

    @ReadOnly
    public List<TotalDTO> topBuildTools(@NonNull @NotNull @Past LocalDate from) {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_BUILD_TOOL, TABLE_APPLICATION, from), callback(from));
    }

    @ReadOnly
    public List<TotalDTO> topBuildTools(@NonNull @NotNull @Past LocalDate from, @NonNull @NotNull Language language) {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_BUILD_TOOL, TABLE_APPLICATION, from, "%s.language = ?".formatted(TABLE_APPLICATION)), callback(from, language));
    }

    @ReadOnly
    public List<TotalDTO> topTestFrameworks(@NonNull @NotNull @Past LocalDate from) {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_TEST_FRAMEWORK, TABLE_APPLICATION, from), callback(from));
    }

    @ReadOnly
    public List<TotalDTO> topJdkVersion(@NonNull @NotNull @Past LocalDate from) {
        return this.jdbcOperations
                .prepareStatement(query(FIELD_JDK_VERSION, TABLE_APPLICATION, from), callback(from));
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

    @NonNull
    private PreparedStatementCallback<List<TotalDTO>> callback(@Nullable LocalDate from) {
        return callback(from, null);
    }

    @NonNull
    private PreparedStatementCallback<List<TotalDTO>> callback(@Nullable LocalDate from, @Nullable Language language) {
        return statement -> {
            int idx = 1;
            if (from != null) {
                statement.setDate(idx++, Date.valueOf(from));
            }
            if (language != null) {
                statement.setString(idx++, language.toString());
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSetToTotals(resultSet);
            }
        };
    }

    @NonNull
    private String query(@NonNull String name,
                         @NonNull String table,
                         @Nullable LocalDate from) {
        return query(name, table, from, null);
    }

    @NonNull
    private String query(@NonNull String name,
                         @NonNull String table,
                         @Nullable LocalDate from,
                         @Nullable String where) {
        if (!EXISTING_TABLES.contains(table)) {
            throw new IllegalArgumentException(table + " does not exist");
        }
        if (table.equals(TABLE_APPLICATION) && !TABLE_APPLICATION_FIELDS.contains(name)) {
            throw new IllegalArgumentException("field " + name + " does not exist in " + table);
        }
        String query;
        if (from == null) {
            query = """
                    SELECT %s AS name, count(*) AS total \
                    FROM %s %s \
                    GROUP BY name \
                    ORDER BY total"""
                    .formatted(
                            name,
                            table,
                            where == null ? "" : "WHERE " + where
                    );
        } else if (table.equals(TABLE_APPLICATION)) {
            query = """
                    SELECT %s AS name, count(*) AS total \
                    FROM %s \
                    WHERE date_created::date >= ? %s \
                    GROUP BY name \
                    ORDER BY total"""
                    .formatted(
                            name,
                            table,
                            where == null ? "" : "AND " + where
                    );
        } else {
            query = """
                    SELECT %s.%s AS name, count(*) AS total \
                    FROM %s INNER JOIN %s ON %s.id = %s.application_id \
                    WHERE %s.date_created::date >= ? %s \
                    GROUP BY %s.%s \
                    ORDER BY total"""
                    .formatted(
                            TABLE_FEATURE, name,
                            TABLE_FEATURE, TABLE_APPLICATION, TABLE_APPLICATION, TABLE_FEATURE,
                            TABLE_APPLICATION, where == null ? "" : "AND " + where,
                            TABLE_FEATURE, name
                    );
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Query: {}", query);
        }
        return query;
    }
}
