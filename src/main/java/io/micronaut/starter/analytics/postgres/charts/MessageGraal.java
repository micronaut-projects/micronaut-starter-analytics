package io.micronaut.starter.analytics.postgres.charts;

import io.micronaut.core.annotation.TypeHint;
import io.micronaut.views.fields.messages.Message;

import static io.micronaut.core.annotation.TypeHint.AccessType.*;
@TypeHint(accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS,
        ALL_PUBLIC_FIELDS
}, value = { Message.class })
public class MessageGraal {
}
