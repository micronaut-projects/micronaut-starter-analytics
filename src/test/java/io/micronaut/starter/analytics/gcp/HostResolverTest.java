package io.micronaut.starter.analytics.gcp;

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.simple.SimpleHttpHeaders;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.http.simple.cookies.SimpleCookies;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(environments = Environment.GOOGLE_COMPUTE)
class HostResolverTest {

    @Test
    void testHostResolutionInCloudRun(HttpHostResolver httpHostResolver, ConversionService conversionService) {
        String host = httpHostResolver.resolve(new HttpRequest() {
            @Override
            public @NonNull Cookies getCookies() {
                return new SimpleCookies(conversionService);
            }

            @Override
            public @NonNull HttpParameters getParameters() {
                return new SimpleHttpParameters(conversionService);
            }

            @Override
            public @NonNull HttpMethod getMethod() {
                return HttpMethod.GET;
            }

            @Override
            public @NonNull URI getUri() {
                return URI.create("/oauth/login/github");
            }

            @Override
            public @NonNull HttpHeaders getHeaders() {
                Map<String, String> headersMap = Map.of("host", "micronaut-foo-bar-yyyy-uc.a.run.app",
                        "x-api-key", "xxx",
                        "accept", "application/json",
                        "authorization", "Bearer yyy",
                        "content-type", "application/json",
                        "content-length", "523",
                        "x-forwarded-for", "107.178.207.38",
                        "x-forwarded-proto", "https",
                        "forwarded:for", "\"107.178.207.38\";proto=https");
                return new SimpleHttpHeaders(headersMap, conversionService);
            }

            @Override
            public @NonNull MutableConvertibleValues<Object> getAttributes() {
                return null;
            }

            @Override
            public @NonNull Optional getBody() {
                return Optional.empty();
            }
        });

        assertEquals("https://micronaut-foo-bar-yyyy-uc.a.run.app", host);
    }
}
