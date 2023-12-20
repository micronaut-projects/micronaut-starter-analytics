package io.micronaut.starter.analytics.security;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Serdeable
public record GithubUser(@NonNull @NotBlank String login) {
}