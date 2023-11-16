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
package io.micronaut.starter.analytics.postgres.gcp;

import io.micronaut.configuration.jdbc.hikari.DatasourceConfiguration;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

@Requires(bean = CloudSQLConnection.class)
@Singleton
public class DatasourceConfigurationPopulator {
    public static final String SOCKET_FACTORY = "socketFactory";
    public static final String CLOUD_SQL_INSTANCE = "cloudSqlInstance";
    public static final String COM_GOOGLE_CLOUD_SQL_POSTGRES_SOCKET_FACTORY = "com.google.cloud.sql.postgres.SocketFactory";
    public static final String IP_TYPES = "ipTypes";
    public static final String COMMA = ",";
    private final CloudSQLConnection cloudSQLConnection;
    private final String dbName;

    public DatasourceConfigurationPopulator(CloudSQLConnection cloudSQLConnection,
                               @Nullable DbConfiguration dbConfiguration) {
        this.cloudSQLConnection = cloudSQLConnection;
        this.dbName = dbConfiguration == null ? null : dbConfiguration.name();
    }


    public void populate(@NonNull DatasourceConfiguration config) {
        if (dbName != null) {
            config.setJdbcUrl("jdbc:postgresql:///%s".formatted(dbName));
        }
        config.addDataSourceProperty(SOCKET_FACTORY, COM_GOOGLE_CLOUD_SQL_POSTGRES_SOCKET_FACTORY);
        config.addDataSourceProperty(CLOUD_SQL_INSTANCE, cloudSQLConnection.name());

        // The ipTypes argument can be used to specify a comma delimited list of preferred IP types
        // for connecting to a Cloud SQL instance. The argument ipTypes=PRIVATE will force the
        // SocketFactory to connect with an instance's associated private IP.
        config.addDataSourceProperty(IP_TYPES,
                String.join(COMMA, cloudSQLConnection.ipTypes().stream().map(IP_TYPE::name).toList()));

    }
}
