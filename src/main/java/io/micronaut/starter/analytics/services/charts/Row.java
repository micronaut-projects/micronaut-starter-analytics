package io.micronaut.starter.analytics.services.charts;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.views.fields.messages.Message;

@ReflectiveAccess
@Introspected
public record Row(Message name, double value) {
}
