package io.micronaut.starter.analytics.postgres.charts;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.util.StringUtils;
import io.micronaut.views.fields.messages.Message;

@Introspected
public record Row(Message name, double value) {
}
