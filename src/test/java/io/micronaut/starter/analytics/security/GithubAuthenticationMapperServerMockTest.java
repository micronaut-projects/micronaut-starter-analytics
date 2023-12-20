package io.micronaut.starter.analytics.security;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.micronaut.security.rules.SecurityRule;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GithubAuthenticationMapperServerMockTest {
    @Test
    void github() {
        EmbeddedServer github = ApplicationContext.run(EmbeddedServer.class, Collections.singletonMap("spec.name", "github"));
        Map<String, Object> configuration = Map.of("micronaut.http.services.githubv3.url", github.getURL().toString(),
                "micronaut.starter.analytics.github.allowed-usernames[0]", "sdelamo");
        EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class, configuration);
        GithubAuthenticationMapper githubAuthenticationMapper = server.getApplicationContext().getBean(GithubAuthenticationMapper.class);
        assertTrue(Mono.from(githubAuthenticationMapper.createAuthenticationResponse(new TokenResponse("xxx", "bearer"), null)).block().isAuthenticated());
        assertFalse(Mono.from(githubAuthenticationMapper.createAuthenticationResponse(new TokenResponse("yyy", "bearer"), null)).block().isAuthenticated());
        server.close();
        github.close();
    }

    @Requires(property = "spec.name", value = "github")
    @Controller
    static class GithubController {

        @Secured(SecurityRule.IS_ANONYMOUS)
        @Get("/user")
        String user(@Header(HttpHeaders.AUTHORIZATION) String authorization) {
            if (authorization.equals("token xxx")) {
                return """
                        {"login":"sdelamo"}""";
            } else {
                return """
                        {"login":"tim"}""";
            }
        }
    }
}
