package io.micronaut.starter.analytics.postgres.security;

import io.micronaut.context.BeanContext;
import io.micronaut.security.token.cookie.CookieTokenReader;
import io.micronaut.security.token.cookie.TokenCookieTokenReader;
import io.micronaut.security.token.reader.TokenReader;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class ApiKeyTokenReaderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void apiKeyTokenReaderTakesPrecedence() {
        Collection<TokenReader> tokenReaders = beanContext.getBeansOfType(TokenReader.class);
        assertNotNull(tokenReaders);
        assertEquals(3, tokenReaders.size());
        assertTrue(tokenReaders.iterator().next() instanceof ApiKeyTokenReader);
        List<TokenReader> tokenReaderList = new ArrayList<>(tokenReaders);
        assertTrue(tokenReaderList.stream().anyMatch(tr -> tr instanceof TokenCookieTokenReader));
        assertTrue(tokenReaderList.stream().anyMatch(tr -> tr instanceof CookieTokenReader));
    }
}