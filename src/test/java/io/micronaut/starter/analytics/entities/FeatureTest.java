package io.micronaut.starter.analytics.entities;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.entities.Feature;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import org.junit.jupiter.api.Test;

import static io.micronaut.core.version.VersionUtils.MICRONAUT_VERSION;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FeatureTest {

    @Test
    void isAnnotatedWithIntrospected() {
        assertDoesNotThrow(() -> BeanIntrospection.getIntrospection(Feature.class));
    }

    @Test
    void featureSetter() {
        Application application = new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.JUNIT, JdkVersion.JDK_17, MICRONAUT_VERSION);
        Feature feature = new Feature(application, "security-jwt");
        feature.setId(1L);
        assertEquals(1L, feature.getId());
    }
}