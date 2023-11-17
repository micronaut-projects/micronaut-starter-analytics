package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.starter.application.ApplicationType;
import io.micronaut.starter.options.BuildTool;
import io.micronaut.starter.options.JdkVersion;
import io.micronaut.starter.options.Language;
import io.micronaut.starter.options.TestFramework;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.micronaut.starter.analytics.postgres.ExcelGenerator.ExcelColumn.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "GenerateExcelSpec")
@Property(name = "api.key", value = "wonderful")
@MicronautTest(transactional = false, environments = {Environment.GOOGLE_COMPUTE})
class GenerateExcelTest {

    @Inject
    AnalyticsClient client;
    @Inject
    ApplicationRepository repository;

    @Inject FeatureRepository featureRepository;

    @Test
    void verifyGeneratedExcel() throws ExecutionException, InterruptedException, IOException {
        //given: "there is one application stored in the repository"
        Application app = repository.save(new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_8, "4.0.1"));
        featureRepository.saveAll(Arrays.asList(new Feature(app, "micronaut-http-validation"), new Feature(app, "http-client")));

        //when: "the spreadsheet is generated"
        byte[] data = client.generateExcel().get();
        Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(data));

        //then: "the contents of the spreadsheet are correct"
        assertEquals(1, workbook.getNumberOfSheets());
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals("Applications", sheet.getSheetName());
        assertEquals(2, sheet.getPhysicalNumberOfRows());
        Row row = sheet.getRow(0);
        assertTrue(row.getRowStyle().getLocked());
        assertEquals(ID.title, row.getCell(ID.ordinal()).getStringCellValue());
        assertEquals(FEATURES.title, row.getCell(FEATURES.ordinal()).getStringCellValue());
        assertEquals(TYPE.title, row.getCell(TYPE.ordinal()).getStringCellValue());
        assertEquals(LANGUAGE.title, row.getCell(LANGUAGE.ordinal()).getStringCellValue());
        assertEquals(BUILD_TOOL.title, row.getCell(BUILD_TOOL.ordinal()).getStringCellValue());
        assertEquals(TEST_FRAMEWORK.title, row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue());
        assertEquals(JDK_VERSION.title, row.getCell(JDK_VERSION.ordinal()).getStringCellValue());
        assertEquals(MICRONAUT_VERSION.title, row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue());
        assertEquals(DATE_CREATED.title, row.getCell(DATE_CREATED.ordinal()).getStringCellValue());

        row = sheet.getRow(1);
        assertEquals(app.getId().doubleValue(), row.getCell(ID.ordinal()).getNumericCellValue());
        List<String> arr = List.of(row.getCell(FEATURES.ordinal()).getStringCellValue().split(", "));
        assertEquals(2, arr.size());
        assertTrue(arr.contains("micronaut-http-validation"));
        assertTrue(arr.contains("http-client"));

        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), "DEFAULT");
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), "java");
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), "gradle");
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), "spock");
        assertEquals(row.getCell(JDK_VERSION.ordinal()).getNumericCellValue(), 8);
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), "4.0.1");
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getNumericCellValue(), DateUtil.getExcelDate(app.getDateCreated(), false));

        featureRepository.deleteAll();
        repository.deleteAll();
    }

    @Requires(property = "spec.name", value = "GenerateExcelSpec")
    @Client("/analytics")
    @Header(name = "X-API-KEY", value = "wonderful")
    static interface AnalyticsClient {

        @Get("/excel")
        @Consumes(MediaType.MICROSOFT_EXCEL_OPEN_XML)
        CompletableFuture<byte[]> generateExcel();
    }
}
