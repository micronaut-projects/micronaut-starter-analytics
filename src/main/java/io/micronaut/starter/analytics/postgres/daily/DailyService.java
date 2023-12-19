package io.micronaut.starter.analytics.postgres.daily;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.starter.analytics.postgres.charts.DailyBarChart;
import io.micronaut.starter.analytics.postgres.charts.Day;
import io.micronaut.views.fields.messages.Message;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Singleton
public class DailyService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyService.class);
    private static final Message DAILY_TITLE = Message.of("Daily application creation", "daily.title");
    private final DailyConfiguration dailyConfiguration;
    private final DailyRepository dailyRepository;

    public DailyService(DailyConfiguration dailyConfiguration,
                        DailyRepository dailyRepository) {
        this.dailyConfiguration = dailyConfiguration;
        this.dailyRepository = dailyRepository;
    }

    public DailyBarChart dailyBarChart(@Nullable LocalDate fromQuery,
                                       @Nullable LocalDate toQuery,
                                       @Nullable Integer days
    ) {
        return barChartFrom(dailyStats(fromQuery, toQuery, days));
    }

    public List<DailyDTO> dailyStats(
            @Nullable LocalDate fromQuery,
            @Nullable LocalDate toQuery,
            @Nullable Integer days
    ) {
        LocalDate to = toQuery != null ? toQuery : LocalDate.now();
        LocalDate from = fromQuery != null ? fromQuery : to.minusDays(days != null ? days : dailyConfiguration.getDays() - 1);
        if (from.isAfter(to)) {
            LOG.error("Requested from date is after to date: {} > {}", from, to);
            throw new IllegalArgumentException("from date is after to date");
        }
        if (DAYS.between(from, to) > dailyConfiguration.getMaxDays()) {
            LOG.error("Requested too many days: {}", DAYS.between(from, to));
            throw new IllegalArgumentException("too many days (max " + dailyConfiguration.getMaxDays() + ")");
        }
        return pad(dailyRepository.dailyCount(from, to), from, to);
    }

    private List<DailyDTO> pad(List<DailyDTO> results, LocalDate from, LocalDate to) {
        int index = 0;
        LocalDate current = from;
        List<DailyDTO> res = new ArrayList<>(((Long)DAYS.between(from, to)).intValue());
        while (current.isBefore(to) || current.isEqual(to)) {
            if (index < results.size() && results.get(index).date().equals(current)) {
                res.add(results.get(index));
                index++;
            } else {
                res.add(new DailyDTO(current, 0));
            }
            current = current.plusDays(1);
        }
        return res;
    }

    private DailyBarChart barChartFrom(List<DailyDTO> dailyDTOS) {
        return dailyDTOS.stream()
                .map(dailyDTO -> new Day(dailyDTO.date(), dailyDTO.count()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), rows -> new DailyBarChart(DAILY_TITLE, rows)));
    }
}
