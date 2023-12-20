package io.micronaut.starter.analytics;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @Test
    void mainStartsContext() {
        try(ApplicationContext context = Main.start()) {
            assertTrue(context.isRunning());
        }
    }
}