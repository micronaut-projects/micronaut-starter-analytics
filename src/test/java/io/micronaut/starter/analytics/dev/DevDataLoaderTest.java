package io.micronaut.starter.analytics.dev;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.starter.analytics.repositories.ApplicationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DevDataLoaderTest {

    @Test
    void devDataLoaderNotLoadedInGCPEnv() {
        try (EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class, Environment.GOOGLE_COMPUTE)) {
            assertFalse(server.getApplicationContext().containsBean(DevDataLoader.class));
            Assertions.assertEquals(0, server.getApplicationContext().getBean(ApplicationRepository.class).count());
        }
        try (EmbeddedServer server = ApplicationContext.run(EmbeddedServer.class, Environment.DEVELOPMENT)) {
            assertTrue(server.getApplicationContext().containsBean(DevDataLoader.class));
            assertNotEquals(0, server.getApplicationContext().getBean(ApplicationRepository.class).count());

        }
    }
}