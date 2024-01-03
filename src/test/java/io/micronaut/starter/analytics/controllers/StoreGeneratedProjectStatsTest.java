package io.micronaut.starter.analytics.controllers;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.starter.analytics.Generated;
import io.micronaut.starter.analytics.SelectedFeature;
import io.micronaut.starter.analytics.entities.Application;
import io.micronaut.starter.analytics.repositories.AbstractDataTest;
import io.micronaut.starter.analytics.services.TotalDTO;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.MicronautJdkVersionConfiguration;
import io.micronaut.starter.options.TestFramework;
import io.micronaut.starter.util.VersionInfo;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "StoreGeneratedProjectStatsSpec")
@Property(name = "api.key", value = "wonderful")
class StoreGeneratedProjectStatsTest extends AbstractDataTest {

    @Inject UnauthorizedAnalyticsClient unauthorizedClient;
    @Inject WrongApiKeyClient wrongApiKeyClient;
    @Inject AnalyticsClient client;

    @Test
    void testSaveGenerationDataWithoutApiKey() {
        Generated generated = new Generated(
                ApplicationType.FUNCTION,
                Language.KOTLIN,
                BuildTool.MAVEN,
                TestFramework.SPOCK,
                MicronautJdkVersionConfiguration.DEFAULT_OPTION
        );
        generated.setSelectedFeatures(List.of(new SelectedFeature("google-cloud-function")));

        ExecutionException thrown = assertThrows(ExecutionException.class, () -> unauthorizedClient.applicationGenerated(generated).get());
        assertInstanceOf(HttpClientResponseException.class, thrown.getCause());
        HttpClientResponseException ex = (HttpClientResponseException) thrown.getCause();
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void testSaveGenerationDataWithAWrongApiKey() {
        Generated generated = new Generated(
                ApplicationType.FUNCTION,
                Language.KOTLIN,
                BuildTool.MAVEN,
                TestFramework.SPOCK,
                MicronautJdkVersionConfiguration.DEFAULT_OPTION
        );
        generated.setSelectedFeatures(List.of(new SelectedFeature("google-cloud-function")));
        Executable e = () -> wrongApiKeyClient.applicationGenerated(generated).get();

        ExecutionException thrown = assertThrows(ExecutionException.class, e);
        assertInstanceOf(HttpClientResponseException.class, thrown.getCause());
        HttpClientResponseException ex = (HttpClientResponseException) thrown.getCause();
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void testSaveGenerationData() throws ExecutionException, InterruptedException {
        Generated generated = new Generated(
                ApplicationType.FUNCTION,
                Language.KOTLIN,
                BuildTool.MAVEN,
                TestFramework.SPOCK,
                MicronautJdkVersionConfiguration.DEFAULT_OPTION
        );
        generated.setSelectedFeatures(List.of(new SelectedFeature("google-cloud-function")));

        HttpStatus status = client.applicationGenerated(generated).get();
        assertEquals(HttpStatus.ACCEPTED, status);

        Application application = applicationRepository.list(Pageable.UNPAGED).getContent().get(0);

        assertEquals(generated.getType(), application.getType());
        assertEquals(application.getLanguage(), generated.getLanguage());
        assertEquals(application.getBuildTool(), generated.getBuildTool());
        assertEquals(application.getJdkVersion(), generated.getJdkVersion());
        assertEquals(application.getTestFramework(), generated.getTestFramework());
        assertTrue(application.getFeatures().stream().anyMatch(it -> it.getName().equals("google-cloud-function")));
        assertEquals(VersionInfo.getMicronautVersion(), application.getMicronautVersion());
        assertNotNull(application.getDateCreated());
        LocalDate from = LocalDate.now().minusDays(30);
        List<TotalDTO> topFeatures = featureRepository.topFeatures(from);

        assertFalse(topFeatures.isEmpty());
        assertEquals("google-cloud-function", topFeatures.get(0).getName());
        assertEquals(1, topFeatures.get(0).getTotal());

        List<TotalDTO> languages = featureRepository.topLanguages(from);

        assertNotNull(languages);
        assertEquals("kotlin", languages.get(0).getName());
        assertTrue(CollectionUtils.isNotEmpty(featureRepository.topBuildTools(from)));
        assertTrue(CollectionUtils.isNotEmpty(featureRepository.topJdkVersion(from)));
        assertTrue(CollectionUtils.isNotEmpty(featureRepository.topTestFrameworks(from)));
    }

    @Requires(property = "spec.name", value = "StoreGeneratedProjectStatsSpec")
    @Client(AnalyticsController.PATH)
    @Header(name = "X-API-KEY", value = "wonderful")
    interface AnalyticsClient {

        @Post("/report")
        CompletableFuture<HttpStatus> applicationGenerated(@NonNull @Body Generated generated);
    }

    @Requires(property = "spec.name", value = "StoreGeneratedProjectStatsSpec")
    @Client(AnalyticsController.PATH)
    interface UnauthorizedAnalyticsClient {

        @Post("/report")
        CompletableFuture<HttpStatus> applicationGenerated(@NonNull @Body Generated generated);
    }

    @Requires(property = "spec.name", value = "StoreGeneratedProjectStatsSpec")
    @Client(AnalyticsController.PATH)
    @Header(name = "X-API-KEY", value = "WRONG!")
    interface WrongApiKeyClient {

        @Post("/report")
        CompletableFuture<HttpStatus> applicationGenerated(@NonNull @Body Generated generated);
    }
}
