package io.micronaut.starter.analytics.security;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties("micronaut.starter.analytics.github")
class GithubAllowedConfiguration {

    private List<String> allowedUsernames;

    public List<String> getAllowedUsernames() {
        return allowedUsernames == null ? Collections.emptyList() : allowedUsernames;
    }

    public void setAllowedUsernames(List<String> allowedUsernames) {
        this.allowedUsernames = allowedUsernames;
    }
}
