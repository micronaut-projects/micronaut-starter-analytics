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

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.micronaut.http.annotation.Filter.MATCH_ALL_PATTERN;

@Filter(MATCH_ALL_PATTERN)
public class LoggingHeadersFilter implements HttpServerFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHeadersFilter.class);
    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (request.getPath().startsWith("/analytics")) {
            for (String headerName : request.getHeaders().names()) {
                LOG.trace("{} {} H {}:{}", request.getMethod(), request.getPath(), headerName, request.getHeaders().get(headerName));
            }
        }
        return chain.proceed(request);
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.FIRST.order();
    }
}
