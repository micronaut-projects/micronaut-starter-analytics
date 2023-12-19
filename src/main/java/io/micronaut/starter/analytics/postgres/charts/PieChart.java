package io.micronaut.starter.analytics.postgres.charts;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.views.fields.messages.Message;

import java.util.List;

@Introspected
public record PieChart(@NonNull String id,
                       @NonNull Message title,
                       @NonNull List<Row> rows) {
}
