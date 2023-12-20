package io.micronaut.starter.analytics.security;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.security.authentication.AuthenticationFailureReason;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Collections;
import reactor.core.publisher.Mono;

@Named("github")
@Singleton
class GithubAuthenticationMapper implements OauthAuthenticationMapper {

    public static final String TOKEN_PREFIX = "token ";
    private final GithubApiClient apiClient;

    private final GithubAllowedConfiguration githubAllowedConfiguration;

    public GithubAuthenticationMapper(GithubApiClient apiClient, GithubAllowedConfiguration githubAllowedConfiguration) {
        this.apiClient = apiClient;
        this.githubAllowedConfiguration = githubAllowedConfiguration;
    }

    @Override
    public Publisher<AuthenticationResponse> createAuthenticationResponse(TokenResponse tokenResponse,
                                                                          @Nullable State state) {
        return Mono.from(apiClient.getUser(TOKEN_PREFIX + tokenResponse.getAccessToken()))
                .map(user -> authenticationResponseForUser(tokenResponse, user));
    }

    @NonNull
    public AuthenticationResponse authenticationResponseForUser(@NonNull TokenResponse tokenResponse,
                                                                 @NonNull GithubUser githubUser) {
        return githubAllowedConfiguration.getAllowedUsernames().contains(githubUser.login())
                ? AuthenticationResponse.success(githubUser.login())
                : AuthenticationResponse.failure(AuthenticationFailureReason.USER_DISABLED);
    }


}
