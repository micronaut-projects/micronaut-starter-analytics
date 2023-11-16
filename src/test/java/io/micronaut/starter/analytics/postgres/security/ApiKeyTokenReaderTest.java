package io.micronaut.starter.analytics.postgres.security;

import io.micronaut.context.BeanContext;
import io.micronaut.security.token.reader.TokenReader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class ApiKeyTokenReaderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void apiKeyTokenReaderTakesPrecedence() {
        Collection<TokenReader> tokenReaders = beanContext.getBeansOfType(TokenReader.class);
        assertNotNull(tokenReaders);
        assertEquals(1, tokenReaders.size());
        assertTrue(tokenReaders.iterator().next() instanceof ApiKeyTokenReader);
    }
}