package io.micronaut.starter.analytics.postgres.security;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.views.ModelAndView;
import io.micronaut.views.model.ViewModelProcessor;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ApiKeyViewModelProcessor implements ViewModelProcessor<Map<String, Object>> {

    private static final String MODEL_API_KEY = "apiKey";

    private final ApiKeyConfiguration apiKeyConfiguration;

    ApiKeyViewModelProcessor(ApiKeyConfiguration apiKeyConfiguration) {
        this.apiKeyConfiguration = apiKeyConfiguration;
    }

    @Override
    public void process(@NonNull HttpRequest<?> request, @NonNull ModelAndView<Map<String, Object>> modelAndView) {
        Map<String, Object> viewModel = modelAndView.getModel().orElseGet(() -> {
            final HashMap<String, Object> newModel = new HashMap<>(1);
            modelAndView.setModel(newModel);
            return newModel;
        });
        try {
            viewModel.putIfAbsent(MODEL_API_KEY, apiKeyConfiguration.key());
        } catch (UnsupportedOperationException ex) {
            final HashMap<String, Object> modifiableModel = new HashMap<>(viewModel);
            modifiableModel.putIfAbsent(MODEL_API_KEY, apiKeyConfiguration.key());
            modelAndView.setModel(modifiableModel);
        }
    }
}
