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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Serdeable
public record PercentageResponse(@JsonIgnore List<PercentageDTO> percentages) {

    /**
     * Collector that groups by name and sums the percentages.
     */
    private static final Collector<PercentageDTO, ?, Map<String, Double>> PERCENTAGE_DTO_MAP_COLLECTOR =
            Collectors.groupingBy(PercentageDTO::name, Collectors.summingDouble(PercentageDTO::percentage));

    public Optional<Double> percentageFor(String name) {
        return percentages.stream()
                .filter(p -> p.name().equals(name))
                .map(PercentageDTO::percentage)
                .findFirst();
    }

    @JsonAnyGetter
    public Map<String, Map<String, Double>> getPercentage() {
        return Map.of("percentages", percentages.stream().collect(PERCENTAGE_DTO_MAP_COLLECTOR));
    }
}
