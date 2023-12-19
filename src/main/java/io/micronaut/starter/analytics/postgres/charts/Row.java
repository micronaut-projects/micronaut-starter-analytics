package io.micronaut.starter.analytics.postgres.charts;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.views.fields.messages.Message;

@ReflectiveAccess
@Introspected
public record Row(Message name, double value) {
}