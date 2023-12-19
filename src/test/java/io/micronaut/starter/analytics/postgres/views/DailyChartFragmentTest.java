package io.micronaut.starter.analytics.postgres.views;

import io.micronaut.core.io.Writable;
import io.micronaut.starter.analytics.postgres.WritableUtils;
import io.micronaut.starter.analytics.postgres.charts.DailyBarChart;
import io.micronaut.starter.analytics.postgres.charts.Day;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.views.ViewsRenderer;
import io.micronaut.views.fields.messages.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class DailyChartFragmentTest {

    @Test
    void renderPieChartFragement(ViewsRenderer<Map<String, Object >, ?> viewsRenderer) throws IOException {
        DailyBarChart model = new DailyBarChart(
                Message.of("Daily", "daily.title"),
                List.of(new Day(LocalDate.now(), 1), new Day(LocalDate.now().minusDays(1), 2))
        );
        Writable writable = viewsRenderer.render("daily.html", Collections.singletonMap("chart", model), null);
        assertNotNull(writable);
        String html = WritableUtils.writeableToString(writable);
        assertNotNull(html);
        assertTrue(html.contains("Daily"));
        assertFalse(html.contains("daily.title"));
        assertTrue(html.contains("data.addRow([ new Date( \"" + LocalDate.now() + "\" ), 1 ]);"));
        assertTrue(html.contains("data.addRow([ new Date( \"" + LocalDate.now().minusDays(1) + "\" ), 2 ]);"));
    }
}
