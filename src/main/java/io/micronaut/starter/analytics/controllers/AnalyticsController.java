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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.starter.analytics.Generated;
import io.micronaut.starter.analytics.configuration.StarterAnalyticsConfiguration;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.entities.Feature;
import io.micronaut.starter.analytics.repositories.ApplicationRepository;
import io.micronaut.starter.analytics.repositories.FeatureRepository;
import io.micronaut.starter.analytics.services.TotalDTO;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@Controller(AnalyticsController.PATH)
@ExecuteOn(TaskExecutors.BLOCKING)
@Secured(SecurityRule.IS_AUTHENTICATED)
public class AnalyticsController {

    public static final String PATH = "/analytics";

    private final StarterAnalyticsConfiguration starterAnalyticsConfiguration;
    private final ApplicationRepository applicationRepository;
    private final FeatureRepository featureRepository;

    public AnalyticsController(
            StarterAnalyticsConfiguration starterAnalyticsConfiguration,
            ApplicationRepository applicationRepository,
            FeatureRepository featureRepository) {
        this.starterAnalyticsConfiguration = starterAnalyticsConfiguration;
        this.applicationRepository = applicationRepository;
        this.featureRepository = featureRepository;
    }

    @Get("/top/features")
    List<TotalDTO> topFeatures() {
        return featureRepository.topFeatures(sinceDate());
    }

    @Get("/top/jdks")
    List<TotalDTO> topJdks() {
        return featureRepository.topJdkVersion(sinceDate());
    }

    @Get("/top/buildTools")
    List<TotalDTO> topBuilds() {
        return featureRepository.topBuildTools(sinceDate());
    }

    @Get("/top/languages")
    List<TotalDTO> topLanguages() {
        return featureRepository.topLanguages(sinceDate());
    }

    @Get("/top/testFrameworks")
    List<TotalDTO> topTestFrameworks() {
        return featureRepository.topTestFrameworks(sinceDate());
    }

    private LocalDate sinceDate() {
        return LocalDate.now().minusDays(starterAnalyticsConfiguration.getDays());
    }
    /**
     * Report analytics.
     * @param generated The generated data
     * @return A future
     */
    @Post("/report")
    @Transactional
    HttpStatus applicationGenerated(@NonNull @Body Generated generated) {
        Application application = new Application(
                generated.getType(),
                generated.getLanguage(),
                generated.getBuildTool(),
                generated.getTestFramework(),
                generated.getJdkVersion(),
                generated.getMicronautVersion()
        );
        Application saved = applicationRepository.save(application);
        List<Feature> features = generated.getSelectedFeatures().stream()
                .map(f -> new Feature(saved, f.getName()))
                .toList();

        featureRepository.saveAll(features);
        return HttpStatus.ACCEPTED;
    }
}
