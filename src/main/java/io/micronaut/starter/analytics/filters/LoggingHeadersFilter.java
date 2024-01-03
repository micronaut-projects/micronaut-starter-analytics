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
package io.micronaut.starter.analytics.filters;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.filter.ServerFilterPhase;
import io.micronaut.starter.analytics.security.ApiKeyTokenReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.micronaut.http.annotation.Filter.MATCH_ALL_PATTERN;

@ServerFilter(MATCH_ALL_PATTERN)
class LoggingHeadersFilter implements Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingHeadersFilter.class);

    @RequestFilter
    void filterRequest(HttpRequest<?> request) {
        if (LOG.isTraceEnabled()) {
            logHeaders(request);
        }
    }

    void logHeaders(@NonNull HttpHeaders headers) {
        for (String headerName : headers.names()) {
            if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION) || headerName.equalsIgnoreCase(ApiKeyTokenReader.X_API_TOKEN)) {
                continue;
            }
            log(headerName, headers.get(headerName));
        }
    }

    protected void log(@NonNull String headerName,
                       @Nullable String headerValue) {
        LOG.trace("H {}:{}", headerName, headerValue);
    }

    private void logHeaders(@NonNull HttpRequest<?> request) {
        LOG.trace("{} {} uri {}", request.getMethod(), request.getPath(), request.getUri().toString());
        logHeaders(request.getHeaders());
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.FIRST.order();
    }
}