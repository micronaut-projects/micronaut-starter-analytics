package io.micronaut.starter.analytics.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

@ConfigurationProperties(StarterAnalyticsConfiguration.PREFIX + ".github")
public class GithubAllowedConfiguration {

    private List<String> allowedUsernames;

    public List<String> getAllowedUsernames() {
        return allowedUsernames == null ? Collections.emptyList() : allowedUsernames;
    }

    public void setAllowedUsernames(List<String> allowedUsernames) {
        this.allowedUsernames = allowedUsernames;
    }
}
