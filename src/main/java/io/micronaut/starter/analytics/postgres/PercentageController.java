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

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

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

    public static final Predicate<TotalDTO> KEEP_ALL = t -> false;
    public static final UnaryOperator<TotalDTO> IDENTITY = UnaryOperator.identity();
    public static final UnaryOperator<TotalDTO> MAP_ALL_GRADLE_TYPES_TO_GRADLE = t -> new TotalDTO(t.getName().startsWith("gradle") ? "gradle" : "maven", t.getTotal());
    public static final Predicate<TotalDTO> EXCLUDE_MAVEN = t -> "maven".equals(t.getName());
    public static final UnaryOperator<TotalDTO> MAP_GRADLE_TO_DSL_LANGUAGE = t -> new TotalDTO("gradle".equals(t.getName()) ? "groovy" : "kotlin", t.getTotal());

    private final FeatureRepository featureRepository;

    PercentageController(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
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
}
