package io.micronaut.starter.analytics.repositories;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;

@MicronautTest(transactional = false)
public abstract class AbstractDataTest {

    @Inject
    protected ApplicationRepository applicationRepository;

    @Inject
    protected FeatureRepository featureRepository;

    @AfterEach
    void cleanup() {
        featureRepository.deleteAll();
        applicationRepository.deleteAll();
    }
}
