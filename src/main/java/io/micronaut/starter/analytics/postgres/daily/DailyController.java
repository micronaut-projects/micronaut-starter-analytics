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
package io.micronaut.starter.analytics.postgres.daily;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.starter.analytics.postgres.AnalyticsController;
import io.micronaut.starter.analytics.postgres.charts.DailyBarChart;
import io.micronaut.starter.analytics.postgres.charts.Day;
import io.micronaut.views.View;
import io.micronaut.views.fields.messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Daily statistics rendering controller
 */
@Controller(AnalyticsController.PATH + "/daily")
@Secured(SecurityRule.IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class DailyController {

    private static final Message DAILY_TITLE = Message.of("Daily application creation", "daily.title");
    private static final Logger LOG = LoggerFactory.getLogger(DailyController.class);

    private final DailyConfiguration dailyConfiguration;
    private final DailyRepository dailyRepository;

    public DailyController(DailyConfiguration dailyConfiguration, DailyRepository dailyRepository) {
        this.dailyConfiguration = dailyConfiguration;
        this.dailyRepository = dailyRepository;
    }

    @Produces(MediaType.TEXT_HTML)
    @Get
    @View("daily")
    HttpResponse<DailyBarChart> view(
            @Nullable @QueryValue("from") LocalDate fromQuery,
            @Nullable @QueryValue("to") LocalDate toQuery,
            @Nullable @QueryValue Integer days
    ) {
        return HttpResponse.ok(barChartFrom(api(fromQuery, toQuery, days)));
    }

    @Get("/counts")
    List<DailyDTO> api(
            @Nullable @QueryValue("from") LocalDate fromQuery,
            @Nullable @QueryValue("to") LocalDate toQuery,
            @Nullable @QueryValue Integer days
    ) {
        LocalDate to = toQuery != null ? toQuery : LocalDate.now();
        LocalDate from = fromQuery != null ? fromQuery : to.minusDays(days != null ? days : dailyConfiguration.getDays() - 1);
        if (from.isAfter(to)) {
            LOG.error("Requested from date is after to date: {} > {}", from, to);
            throw new IllegalArgumentException("from date is after to date");
        }
        if (DAYS.between(from, to) > dailyConfiguration.getMaxDays()) {
            LOG.error("Requested too many days: {}", DAYS.between(from, to));
            throw new IllegalArgumentException("too many days (max " + dailyConfiguration.getMaxDays() + ")");
        }
        return pad(dailyRepository.dailyCount(from, to), from, to);
    }

    private List<DailyDTO> pad(List<DailyDTO> results, LocalDate from, LocalDate to) {
        int index = 0;
        LocalDate current = from;
        List<DailyDTO> res = new ArrayList<>(((Long)DAYS.between(from, to)).intValue());
        while (current.isBefore(to) || current.isEqual(to)) {
            if (index < results.size() && results.get(index).date().equals(current)) {
                res.add(results.get(index));
                index++;
            } else {
                res.add(new DailyDTO(current, 0));
            }
            current = current.plusDays(1);
        }
        return res;
    }

    private DailyBarChart barChartFrom(List<DailyDTO> dailyDTOS) {
        return dailyDTOS.stream()
                .map(dailyDTO -> new Day(dailyDTO.date(), dailyDTO.count()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), rows -> new DailyBarChart(DAILY_TITLE, rows)));
    }
}
