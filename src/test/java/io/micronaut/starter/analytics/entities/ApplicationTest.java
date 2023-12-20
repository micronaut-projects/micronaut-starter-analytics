package io.micronaut.starter.analytics.entities;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.starter.analytics.entities.Application;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ApplicationTest {

    @Test
    void isAnnotatedWithIntrospected() {
        assertDoesNotThrow(() -> BeanIntrospection.getIntrospection(Application.class));
    }
}