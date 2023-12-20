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
package io.micronaut.starter.analytics.repositories;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.starter.analytics.services.DailyDTO;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Singleton;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Repository for daily analytics.
 */
@Singleton
public class DailyRepository {

    private final JdbcOperations jdbcOperations;

    protected DailyRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @ReadOnly
    @NonNull
    public List<DailyDTO> dailyCount(LocalDate from, LocalDate to) {
        Long between = DAYS.between(from, to);
        return jdbcOperations.prepareStatement(
                "SELECT date_created::date, count(*) FROM application WHERE date_created::date >= ? AND date_created::date <= ? GROUP BY date_created::date ORDER BY date_created",
                statement -> {
                    statement.setDate(1, Date.valueOf(from));
                    statement.setDate(2, Date.valueOf(to));
                    try (var resultSet = statement.executeQuery()) {
                        return resultSetToDaily(resultSet, between.intValue());
                    }
                });
    }

    @NonNull
    private List<DailyDTO> resultSetToDaily(@NonNull ResultSet resultSet, int size) throws SQLException {
        List<DailyDTO> results = new ArrayList<>(size);
        while (resultSet.next()) {
            results.add(
                    new DailyDTO(
                            resultSet.getDate(1).toLocalDate(),
                            resultSet.getLong(2)
                    )
            );
        }
        return results;
    }
}
