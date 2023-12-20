package io.micronaut.starter.analytics.security;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

@Property(name = "micronaut.starter.analytics.github.allowed-usernames[0]", value = "sdelamo")
@MicronautTest(startApplication = false)
class GithubAuthenticationMapperTest {

    @Inject
    BeanContext beanContext;

    @Test
    void authenticationResponse() {
        OauthAuthenticationMapper oauthAuthenticationMapper = beanContext.getBean(OauthAuthenticationMapper.class, Qualifiers.byName("github"));
        assertTrue(oauthAuthenticationMapper instanceof GithubAuthenticationMapper);
        GithubAuthenticationMapper githubAuthenticationMapper = (GithubAuthenticationMapper) oauthAuthenticationMapper;
        assertNotNull(githubAuthenticationMapper);
        TokenResponse tokenResponse = new TokenResponse();
        assertTrue(githubAuthenticationMapper.authenticationResponseForUser(tokenResponse, new GithubUser("sdelamo")).isAuthenticated());
        assertFalse(githubAuthenticationMapper.authenticationResponseForUser(tokenResponse, new GithubUser("tim")).isAuthenticated());
    }
}