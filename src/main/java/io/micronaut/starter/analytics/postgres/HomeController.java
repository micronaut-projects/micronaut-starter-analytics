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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.net.URI;

@Controller
public class HomeController {

    private static final @NonNull URI URI_ANALYTICS_PERCENTAGES = UriBuilder.of("/analytics").path("percentages").build();

    @Produces(MediaType.TEXT_HTML)
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    HttpResponse<?> index(HttpRequest<?> request) {
        return accepts(request, MediaType.TEXT_HTML_TYPE)
                ? HttpResponse.seeOther(URI_ANALYTICS_PERCENTAGES)
                : HttpResponse.notFound();
    }

    private static boolean accepts(@NonNull HttpRequest<?> request,
                                   @NonNull MediaType mediaType) {
        return request.getHeaders()
                .accept()
                .stream()
                .anyMatch(it -> it.getName().contains(mediaType));
    }
}
