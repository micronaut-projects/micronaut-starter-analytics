package io.micronaut.starter.analytics.postgres.percentages;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.analytics.postgres.FeatureRepository;
import io.micronaut.starter.analytics.postgres.TotalDTO;
import io.micronaut.starter.analytics.postgres.charts.Row;
import io.micronaut.views.fields.messages.Message;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Singleton
public class PercentageService {
    private static final Predicate<TotalDTO> KEEP_ALL = t -> false;
    private static final UnaryOperator<TotalDTO> IDENTITY = UnaryOperator.identity();
    private static final String GRADLE = "gradle";
    private static final String MAVEN = "maven";

    private static final String GROOVY = "groovy";
    private static final String KOTLIN = "kotlin";
    private static final UnaryOperator<TotalDTO> MAP_ALL_GRADLE_TYPES_TO_GRADLE = t -> new TotalDTO(t.getName().startsWith(GRADLE) ? GRADLE : MAVEN, t.getTotal());
    private static final Predicate<TotalDTO> EXCLUDE_MAVEN = t -> MAVEN.equals(t.getName());
    private static final UnaryOperator<TotalDTO> MAP_GRADLE_TO_DSL_LANGUAGE = t -> new TotalDTO(GRADLE.equals(t.getName()) ? GROOVY : KOTLIN, t.getTotal());
    private final FeatureRepository featureRepository;

    PercentageService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    @NonNull
    public List<Row> buildToolPieChart() {
        return pieChartRows(buildTool());
    }

    @NonNull
    public List<Row> gradleDslPieChart() {
        return pieChartRows(gradleDsl());
    }

    @NonNull
    public List<Row> jdksPieChart() {
        return pieChartRows(jdks());
    }

    @NonNull
    public List<Row> languagesPieChart() {
        return pieChartRows(languages());
    }

    @NonNull
    public List<Row> testFrameworksPieChart() {
        return pieChartRows(testFrameworks());
    }

    public PercentageResponse buildTool() {
        return toPercentage(featureRepository::topBuildTools, KEEP_ALL, MAP_ALL_GRADLE_TYPES_TO_GRADLE);
    }

    public PercentageResponse gradleDsl() {
        return toPercentage(featureRepository::topBuildTools, EXCLUDE_MAVEN, MAP_GRADLE_TO_DSL_LANGUAGE);
    }

    public PercentageResponse jdks() {
        return toPercentage(featureRepository::topJdkVersion, KEEP_ALL, IDENTITY);
    }

    public PercentageResponse languages() {
        return toPercentage(featureRepository::topLanguages, KEEP_ALL, IDENTITY);
    }

    public PercentageResponse testFrameworks() {
        return toPercentage(featureRepository::topTestFrameworks, KEEP_ALL, IDENTITY);
    }

    private PercentageResponse toPercentage(
            Supplier<List<TotalDTO>> supplier,
            Predicate<TotalDTO> removeIf,
            UnaryOperator<TotalDTO> mapper
    ) {
        List<TotalDTO> totalDTOS = supplier.get();

        // Remove any totals that match the predicate
        totalDTOS.removeIf(removeIf);

        // Sum all the remaining totals
        long sum = totalDTOS.stream().mapToLong(TotalDTO::getTotal).sum();

        // Collect the totals into a map of name to total (we need to re-group as the mapper may have changed the name)
        Map<String, Long> collect = totalDTOS
                .stream()
                .map(mapper)
                .collect(Collectors.groupingBy(TotalDTO::getName, Collectors.summingLong(TotalDTO::getTotal)));

        // Convert the map into a list of PercentageDTOs
        List<PercentageDTO> list = collect.entrySet()
                .stream()
                .map(e -> new PercentageDTO(e.getKey(), (double) e.getValue() / sum))
                .toList();

        return new PercentageResponse(list);
    }

    @NonNull
    private List<Row> pieChartRows(@NonNull PercentageResponse percentageResponse) {
        return percentageResponse.percentages()
                .stream()
                .map(this::rowOfPercentageDto)
                .toList();
    }

    @NonNull
    private Row rowOfPercentageDto(@NonNull PercentageDTO dto) {
        return new Row(Message.of(StringUtils.capitalize(dto.name()), "percentage." + dto.name()),
                dto.percentage() * 100.0);
    }
}
