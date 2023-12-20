package io.micronaut.starter.analytics.controllers;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import io.micronaut.views.fields.messages.Message;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.Collections;
import java.util.Map;

@Controller
class UnauthorizedController {
    private static final @NonNull Message MESSAGE_UNAUTHORIZED = Message.of("Unauthorized", "unauthorized");
    private static final String KEY_MESSAGE = "message";

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Produces(MediaType.TEXT_HTML)
    @Hidden
    @Get("/unauthorized")
    @View("message.html")
    Map<String, Object> index() {
        return Collections.singletonMap(KEY_MESSAGE, MESSAGE_UNAUTHORIZED);
    }
}
