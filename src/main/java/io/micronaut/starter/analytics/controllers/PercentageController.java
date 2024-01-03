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

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.starter.analytics.configuration.StarterAnalyticsConfiguration;
import io.micronaut.starter.analytics.services.charts.PieChart;
import io.micronaut.starter.analytics.services.percentages.PercentageService;
import io.micronaut.views.View;
import io.micronaut.views.fields.messages.Message;
import io.swagger.v3.oas.annotations.Hidden;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
class PercentageController {
    private static final String ID_BUILD_TOOL = "buildTool";
    private static final String ID_GRADLE_DSL = "gradleDsl";
    private static final String ID_JDK = "jdk";
    private static final String ID_LANGUAGE = "language";
    private static final String ID_TEST_FRAMEWORK = "testFramework";
    private static final Message MESSAGE_PERCENTAGE_BUILD_TOOLS = Message.of("Build tools", "percentage.buildtool");
    private static final Message MESSAGE_PERCENTAGE_GRADLE_DSLS = Message.of("Gradle DSLs", "percentage.gradledsl");
    private static final Message MESSAGE_PERCENTAGE_JAVA_VERSIONS = Message.of("Java versions", "percentage.javaversions");
    private static final Message MESSAGE_PERCENTAGE_LANGUAGES = Message.of("Languages", "percentage.languages");
    private static final Message MESSAGE_PERCENTAGE_TEST_FRAMEWORKS = Message.of("Test frameworks", "percentage.testframeworks");
    private static final String MODEL_METRICS = "charts";
    private final PercentageService percentageService;
    private final StarterAnalyticsConfiguration starterAnalyticsConfiguration;

    PercentageController(PercentageService percentageService,
                         StarterAnalyticsConfiguration starterAnalyticsConfiguration) {
        this.percentageService = percentageService;
        this.starterAnalyticsConfiguration = starterAnalyticsConfiguration;
    }

    @Get(AnalyticsController.PATH + "/percentages")
    @View("percentages")
    @Hidden
    @Produces(MediaType.TEXT_HTML)
    @Secured(SecurityRule.IS_ANONYMOUS)
    @ExecuteOn(TaskExecutors.BLOCKING)
    Map<String, Object> index() {
        LocalDate from = sinceDate();
        return Collections.singletonMap(MODEL_METRICS, List.of(
                new PieChart(ID_BUILD_TOOL, MESSAGE_PERCENTAGE_BUILD_TOOLS, percentageService.buildToolPieChart(from)),
                new PieChart(ID_GRADLE_DSL, MESSAGE_PERCENTAGE_GRADLE_DSLS, percentageService.gradleDslPieChart(from)),
                new PieChart(ID_JDK, MESSAGE_PERCENTAGE_JAVA_VERSIONS, percentageService.jdksPieChart(from)),
                new PieChart(ID_LANGUAGE, MESSAGE_PERCENTAGE_LANGUAGES, percentageService.languagesPieChart(from)),
                new PieChart(ID_TEST_FRAMEWORK, MESSAGE_PERCENTAGE_TEST_FRAMEWORKS, percentageService.testFrameworksPieChart(from))
        ));
    }

    private LocalDate sinceDate() {
        return LocalDate.now().minusDays(starterAnalyticsConfiguration.getDays());
    }
}
