package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;

@MicronautTest(transactional = false, environments = {Environment.GOOGLE_COMPUTE})
public abstract class AbstractDataTest {

    @Inject
    ApplicationRepository applicationRepository;

    @Inject
    FeatureRepository featureRepository;

    @AfterEach
    void cleanup() {
        featureRepository.deleteAll();
        applicationRepository.deleteAll();
    }
}
