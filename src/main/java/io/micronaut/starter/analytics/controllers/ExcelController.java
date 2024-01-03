package io.micronaut.starter.analytics.controllers;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.starter.analytics.services.ExcelGenerator;
import io.swagger.v3.oas.annotations.Hidden;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
class ExcelController {
    private final ExcelGenerator excelGenerator;

    ExcelController(ExcelGenerator excelGenerator) {
        this.excelGenerator = excelGenerator;
    }

    /**
     * Generates an Excel spreadsheet containing all the applications stored in the repository.
     * @return an Excel spreadsheet.
     * @throws IOException if the spreadsheet cannot be written to the buffer.
     */

    @Get(AnalyticsController.PATH + "/excel")
    @Produces(MediaType.MICROSOFT_EXCEL_OPEN_XML)
    @Hidden
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @ExecuteOn(TaskExecutors.BLOCKING)
    HttpResponse<StreamedFile> generateExcel() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            excelGenerator.generateExcel().write(out);
            return HttpResponse.ok()
                    .body(new StreamedFile(new ByteArrayInputStream(out.toByteArray()), MediaType.MICROSOFT_EXCEL_OPEN_XML_TYPE)
                            .attach("applications." + MediaType.EXTENSION_XLSX));
        }
    }
}
