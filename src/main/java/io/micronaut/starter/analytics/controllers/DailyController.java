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
package io.micronaut.starter.analytics.controllers;

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
import io.micronaut.starter.analytics.services.DailyService;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.View;
import io.micronaut.views.fields.messages.Message;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.*;

/**
 * Daily statistics rendering controller
 */
@Controller(AnalyticsController.PATH)
class DailyController {
    private static final String MODEL_CHART = "chart";
    private final DailyService dailyService;

    DailyController(DailyService dailyService) {
        this.dailyService = dailyService;
    }

    @Get("/daily")
    @Produces(MediaType.TEXT_HTML)
    @Hidden
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @ExecuteOn(TaskExecutors.BLOCKING)
    ModelAndView<Map<String, Object>> view(
            @Nullable @QueryValue("from") @Past LocalDate fromQuery,
            @Nullable @QueryValue("to") @Past LocalDate toQuery,
            @Nullable @QueryValue Integer days) {
        try {
            return new ModelAndView<>("daily.html", Collections.singletonMap(MODEL_CHART, dailyService.dailyBarChart(fromQuery, toQuery, days)));
        } catch (IllegalArgumentException e) {
            return new ModelAndView<>("message.html", Collections.singletonMap("message", Message.of(e.getMessage())));
        }
    }
}
