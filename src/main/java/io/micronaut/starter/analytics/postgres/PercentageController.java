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

@Controller(AnalyticsController.PATH + "/percentages")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
class PercentageController {

    private final FeatureRepository featureRepository;

    PercentageController(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @Get("/types")
    PercentageResponse applicationType() {
        return new PercentageResponse(featureRepository.applicationTypePercentages());
    }

    @Get("/buildTools")
    PercentageResponse buildTool() {
        return new PercentageResponse(featureRepository.buildToolPercentages());
    }

    @Get("/jdks")
    PercentageResponse jdks() {
        return new PercentageResponse(featureRepository.jdkPercentages());
    }

    @Get("/languages")
    PercentageResponse languages() {
        return new PercentageResponse(featureRepository.languagePercentages());
    }

    @Get("/testFrameworks")
    PercentageResponse testFrameworks() {
        return new PercentageResponse(featureRepository.testFrameworkPercentages());
    }
}