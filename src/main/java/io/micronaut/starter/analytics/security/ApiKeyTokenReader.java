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
package io.micronaut.starter.analytics.security;

import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.reader.HttpHeaderTokenReader;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Reads token {@code X-API-KEY} from an HTTP request.
 */
@Singleton
public class ApiKeyTokenReader extends HttpHeaderTokenReader {

    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyTokenReader.class);

    public static final String X_API_TOKEN = "X-API-KEY";


    @Override
    protected String getPrefix() {
        return null;
    }

    @Override
    protected String getHeaderName() {
        return X_API_TOKEN;
    }

    @Override
    public Optional<String> findToken(HttpRequest<?> request) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for bearer token in {} header", getHeaderName());
        }
        HttpHeaders headers = request.getHeaders();
        Optional<String> authorizationHeader = headers.findFirst(getHeaderName());
        if (authorizationHeader.isEmpty()) {
            authorizationHeader = headers.findFirst(getHeaderName().toLowerCase());
        }
        return authorizationHeader.flatMap(this::extractTokenFromAuthorization);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
