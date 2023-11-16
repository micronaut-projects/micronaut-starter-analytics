package io.micronaut.starter.analytics.postgres.security;

import io.micronaut.context.BeanContext;
import io.micronaut.security.token.bearer.BearerTokenReader;
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
        List<TokenReader> tokenReaderList = new ArrayList<>(tokenReaders);
        assertTrue(tokenReaderList.get(0) instanceof ApiKeyTokenReader);
        assertTrue(tokenReaderList.get(1) instanceof BearerTokenReader);
    }
}