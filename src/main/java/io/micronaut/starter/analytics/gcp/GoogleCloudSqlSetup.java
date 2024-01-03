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
package io.micronaut.starter.analytics.gcp;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;

import jakarta.inject.Singleton;

/**
 * Configuration for the Cloud SQL environment.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Singleton
@Requires(env = Environment.GOOGLE_COMPUTE)
@Requires(bean = CloudSQLConnection.class)
public class GoogleCloudSqlSetup implements BeanCreatedEventListener<DatasourceConfiguration> {
    private final DatasourceConfigurationPopulator datasourceConfigurationPopulator;

    public GoogleCloudSqlSetup(DatasourceConfigurationPopulator datasourceConfigurationPopulator) {
        this.datasourceConfigurationPopulator = datasourceConfigurationPopulator;
    }

    @Override
    public DatasourceConfiguration onCreated(BeanCreatedEvent<DatasourceConfiguration> event) {
        DatasourceConfiguration config = event.getBean();
        datasourceConfigurationPopulator.populate(config);
        return config;
    }
}
