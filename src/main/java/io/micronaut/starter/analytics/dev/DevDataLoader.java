/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.starter.analytics.dev;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.repositories.ApplicationRepository;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Loads dummy data when run locally.
 */
@Singleton
@Requires(env = Environment.DEVELOPMENT)
public class DevDataLoader implements ApplicationEventListener<ApplicationStartupEvent> {

    /**
     * The version of Micronaut to use for the dummy data.
     * Added TESTING suffix, so we can find and remove them if they ever leak into the production analytics.
     */
    private static final String MICRONAUT_VERSION = "4.0.0-TESTING";
    private static final Logger LOG = LoggerFactory.getLogger(DevDataLoader.class);

    private final ApplicationRepository applicationRepository;

    public DevDataLoader(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        if (applicationRepository.count() == 0) {
            LOG.info("Loading dummy data");
            applicationRepository.saveAll(List.of(
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, MICRONAUT_VERSION),
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_21, MICRONAUT_VERSION),
                    new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.MAVEN, TestFramework.SPOCK, JdkVersion.JDK_21, MICRONAUT_VERSION),
                    new Application(ApplicationType.FUNCTION, Language.GROOVY, BuildTool.GRADLE_KOTLIN, TestFramework.SPOCK, JdkVersion.JDK_17, MICRONAUT_VERSION),
                    new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.MAVEN, TestFramework.KOTEST, JdkVersion.JDK_17, MICRONAUT_VERSION)
            ));
        }
    }
}
