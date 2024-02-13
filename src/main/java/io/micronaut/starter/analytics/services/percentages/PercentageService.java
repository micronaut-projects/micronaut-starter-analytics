package io.micronaut.starter.analytics.services.percentages;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.analytics.repositories.FeatureRepository;
import io.micronaut.starter.analytics.services.TotalDTO;
import io.micronaut.starter.analytics.services.charts.Row;
import io.micronaut.starter.options.Language;
import io.micronaut.views.fields.messages.Message;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
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
    public List<Row> buildToolPieChart(@NonNull @NotNull @Past LocalDate from) {
        return pieChartRows(buildTool(from));
    }

    @NonNull
    public List<Row> buildToolPieChart(@NonNull @NotNull @Past LocalDate from, @NonNull @NotNull Language language) {
        return pieChartRows(buildTool(from, language));
    }

    @NonNull
    public List<Row> gradleDslPieChart(@NonNull @NotNull @Past LocalDate from) {
        return pieChartRows(gradleDsl(from));
    }

    @NonNull
    public List<Row> jdksPieChart(@NonNull @NotNull @Past LocalDate from) {
        return pieChartRows(jdks(from));
    }

    @NonNull
    public List<Row> languagesPieChart(@NonNull @NotNull @Past LocalDate from) {
        return pieChartRows(languages(from));
    }

    @NonNull
    public List<Row> testFrameworksPieChart(@NonNull @NotNull @Past LocalDate from) {
        return pieChartRows(testFrameworks(from));
    }

    public PercentageResponse buildTool(@NonNull @NotNull @Past LocalDate from) {
        return toPercentage(() -> featureRepository.topBuildTools(from), KEEP_ALL, MAP_ALL_GRADLE_TYPES_TO_GRADLE);
    }

    public PercentageResponse buildTool(@NonNull @NotNull @Past LocalDate from, @NonNull @NotNull Language language) {
        return toPercentage(() -> featureRepository.topBuildTools(from, language), KEEP_ALL, MAP_ALL_GRADLE_TYPES_TO_GRADLE);
    }

    public PercentageResponse gradleDsl(@NonNull @NotNull @Past LocalDate from) {
        return toPercentage(() -> featureRepository.topBuildTools(from), EXCLUDE_MAVEN, MAP_GRADLE_TO_DSL_LANGUAGE);
    }

    public PercentageResponse jdks(@NonNull @NotNull @Past LocalDate from) {
        return toPercentage(() -> featureRepository.topJdkVersion(from), KEEP_ALL, IDENTITY);
    }

    public PercentageResponse languages(@NonNull @NotNull @Past LocalDate from) {
        return toPercentage(() -> featureRepository.topLanguages(from), KEEP_ALL, IDENTITY);
    }

    public PercentageResponse testFrameworks(@NonNull @NotNull @Past LocalDate from) {
        return toPercentage(() -> featureRepository.topTestFrameworks(from), KEEP_ALL, IDENTITY);
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
