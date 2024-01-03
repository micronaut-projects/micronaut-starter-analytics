package io.micronaut.starter.analytics.views;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.io.Writable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.starter.analytics.utils.WritableUtils;
import io.micronaut.starter.analytics.services.charts.Row;
import io.micronaut.starter.analytics.services.percentages.PercentageDTO;
import io.micronaut.starter.analytics.services.charts.PieChart;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.views.ViewsRenderer;
import io.micronaut.views.fields.messages.Message;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class PieChartFragmentTest {

    @Test
    void renderPieChartFragement(ViewsRenderer<Map<String, Object>, ?> viewsRenderer) throws IOException {
        PieChart model = new PieChart("xxx",
                Message.of("Build tools", "percentage.buildtool"),
                List.of(rowOfPercentageDto(new PercentageDTO("gradle", 0.6)), rowOfPercentageDto(new PercentageDTO("maven", 0.4))));
        Writable writable = viewsRenderer.render("_piechart.html", Map.of("chart", model), null);
        assertNotNull(writable);
        String html = WritableUtils.writeableToString(writable);
        assertNotNull(html);
        assertTrue(html.contains("Build tools"));
        assertTrue(html.contains("<div id=\"xxx\">"));
        assertFalse(html.contains("percentage.buildtool"));
        assertTrue(html.contains("data.addRow([ \"Gradle\", 60.0 ]);"));
        assertTrue(html.contains("data.addRow([ \"Maven\", 40.0 ]);"));
    }

    @NonNull
    private Row rowOfPercentageDto(@NonNull PercentageDTO dto) {
        return new Row(Message.of(StringUtils.capitalize(dto.name()), "percentage." + dto.name()),
                dto.percentage() * 100.0);
    }
}
