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

import io.micronaut.core.util.StringUtils;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Serdeable
public class PercentageViewModel {

    private final List<PercentageMetrics> percentages;

    PercentageViewModel(List<PercentageMetrics> percentages) {
        this.percentages = percentages;
    }

    public List<PercentageMetrics> getMetrics() {
        return percentages;
    }

    @Serdeable
    public static class PercentageMetrics {

        private final String id;
        private final String title;
        private final List<IndividualMetric> metrics;

        PercentageMetrics(String id, String title, PercentageResponse percentageResponse) {
            this.id = id;
            this.title = title;
            this.metrics = percentageResponse.percentages().stream().map(IndividualMetric::new).toList();
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public List<IndividualMetric> getMetrics() {
            return metrics;
        }
    }

    @Serdeable
    public static class IndividualMetric {

        private static final Map<String, String> NAME_LOOKUP = Map.of(
                "junit", "JUnit5",
                "kotest", "Kotest5",
                "jdk_17", "17",
                "jdk_21", "21",
                "groovy", "Apache Groovy"
        );

        private final String name;
        private final double value;

        IndividualMetric(PercentageDTO percentages) {
            String fixed = NAME_LOOKUP.get(percentages.name());
            this.name = Objects.requireNonNullElseGet(fixed, () -> StringUtils.capitalize(percentages.name()));
            this.value = percentages.percentage() * 100.0;
        }

        public String getName() {
            return name;
        }

        public double getValue() {
            return value;
        }
    }
}
