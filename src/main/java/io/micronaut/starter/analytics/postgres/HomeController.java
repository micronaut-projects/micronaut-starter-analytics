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
    public static final @NonNull URI URI_ANALYTICS_PERCENTAGES = UriBuilder.of("/analytics").path("percentages").build();

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
