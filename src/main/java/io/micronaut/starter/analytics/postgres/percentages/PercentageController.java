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
package io.micronaut.starter.analytics.postgres.percentages;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.starter.analytics.postgres.AnalyticsController;
import io.micronaut.starter.analytics.postgres.FeatureRepository;
import io.micronaut.starter.analytics.postgres.TotalDTO;
import io.micronaut.starter.analytics.postgres.charts.PieChart;
import io.micronaut.starter.analytics.postgres.charts.Row;
import io.micronaut.views.View;
import io.micronaut.views.fields.messages.Message;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Controller(AnalyticsController.PATH + "/percentages")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
class PercentageController {

    private static final Predicate<TotalDTO> KEEP_ALL = t -> false;
    private static final UnaryOperator<TotalDTO> IDENTITY = UnaryOperator.identity();
    private static final String GRADLE = "gradle";
    private static final String MAVEN = "maven";
    private static final UnaryOperator<TotalDTO> MAP_ALL_GRADLE_TYPES_TO_GRADLE = t -> new TotalDTO(t.getName().startsWith(GRADLE) ? GRADLE : MAVEN, t.getTotal());
    private static final Predicate<TotalDTO> EXCLUDE_MAVEN = t -> MAVEN.equals(t.getName());
    private static final String GROOVY = "groovy";
    private static final String KOTLIN = "kotlin";
    private static final UnaryOperator<TotalDTO> MAP_GRADLE_TO_DSL_LANGUAGE = t -> new TotalDTO(GRADLE.equals(t.getName()) ? GROOVY : KOTLIN, t.getTotal());
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
    private final FeatureRepository featureRepository;

    private static final String MODEL_METRICS = "charts";

    PercentageController(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @Produces(MediaType.TEXT_HTML)
    @Get
    @View("percentages")
    Map<String, Object> index() {
        return Collections.singletonMap(MODEL_METRICS, List.of(
                new PieChart(ID_BUILD_TOOL, MESSAGE_PERCENTAGE_BUILD_TOOLS, pieChartRows(buildTool())),
                new PieChart(ID_GRADLE_DSL, MESSAGE_PERCENTAGE_GRADLE_DSLS, pieChartRows(gradleDsl())),
                new PieChart(ID_JDK, MESSAGE_PERCENTAGE_JAVA_VERSIONS, pieChartRows(jdks())),
                new PieChart(ID_LANGUAGE, MESSAGE_PERCENTAGE_LANGUAGES, pieChartRows(languages())),
                new PieChart(ID_TEST_FRAMEWORK, MESSAGE_PERCENTAGE_TEST_FRAMEWORKS, pieChartRows(testFrameworks()))
        ));
    }

    @Get("/buildTool")
    PercentageResponse buildTool() {
        return toPercentage(featureRepository::topBuildTools, KEEP_ALL, MAP_ALL_GRADLE_TYPES_TO_GRADLE);
    }

    @Get("/gradleDsl")
    PercentageResponse gradleDsl() {
        return toPercentage(featureRepository::topBuildTools, EXCLUDE_MAVEN, MAP_GRADLE_TO_DSL_LANGUAGE);
    }

    @Get("/jdk")
    PercentageResponse jdks() {
        return toPercentage(featureRepository::topJdkVersion, KEEP_ALL, IDENTITY);
    }

    @Get("/language")
    PercentageResponse languages() {
        return toPercentage(featureRepository::topLanguages, KEEP_ALL, IDENTITY);
    }

    @Get("/testFramework")
    PercentageResponse testFrameworks() {
        return toPercentage(featureRepository::topTestFrameworks, KEEP_ALL, IDENTITY);
    }

    private PercentageResponse toPercentage(
            Supplier<List<TotalDTO>> supplier,
            Predicate<TotalDTO> removeIf,
            UnaryOperator<TotalDTO> mapper
    ) {
        List<TotalDTO> totalDTOS = supplier.get();

        // Remove any totals that match the predicate
        totalDTOS.removeIf(removeIf);

        // Sum all the remaining totals
        long sum = totalDTOS.stream().mapToLong(TotalDTO::getTotal).sum();

        // Collect the totals into a map of name to total (we need to re-group as the mapper may have changed the name)
        Map<String, Long> collect = totalDTOS
                .stream()
                .map(mapper)
                .collect(Collectors.groupingBy(TotalDTO::getName, Collectors.summingLong(TotalDTO::getTotal)));

        // Convert the map into a list of PercentageDTOs
        List<PercentageDTO> list = collect.entrySet()
                .stream()
                .map(e -> new PercentageDTO(e.getKey(), (double) e.getValue() / sum))
                .toList();

        return new PercentageResponse(list);
    }

    @NonNull
    private List<Row> pieChartRows(@NonNull PercentageResponse percentageResponse) {
        return percentageResponse.percentages()
                .stream()
                .map(this::rowOfPercentageDto)
                .toList();
    }

    @NonNull
    private Row rowOfPercentageDto(@NonNull PercentageDTO dto) {
        return new Row(Message.of(StringUtils.capitalize(dto.name()), "percentage." + dto.name()),
                dto.percentage() * 100.0);
    }
}
