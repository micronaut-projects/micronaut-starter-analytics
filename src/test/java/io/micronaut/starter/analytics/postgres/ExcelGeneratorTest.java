package io.micronaut.starter.analytics.postgres;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledInNativeImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.micronaut.starter.analytics.postgres.ExcelGenerator.ExcelColumn.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "micronaut.starter.analytics.page-size", value = "5")
@Property(name = "spec.name", value = "ExcelGeneratorSpec")
@DisabledInNativeImage
class ExcelGeneratorTest extends AbstractDataTest {
    @Inject
    ExcelGenerator excelGenerator;

    @Test
    void generateSpreadsheetWithOneApplication() {
        //given: "there is one application stored in the repository"
        Application app = applicationRepository.save(new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_8, "4.0.0"));
        featureRepository.saveAll(List.of(new Feature(app, "foo"), new Feature(app, "bar")));

        //when: "the spreadsheet is generated"
        Workbook workbook = excelGenerator.generateExcel();

        //then: "the contents of the spreadsheet are correct"
        assertEquals(1, workbook.getNumberOfSheets());
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals(ExcelGenerator.SHEET_NAME, sheet.getSheetName());
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
        assertTrue(arr.contains("foo"));
        assertTrue(arr.contains("bar"));

        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), "DEFAULT");
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), "java");
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), "gradle");
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), "spock");
        assertEquals(8, row.getCell(JDK_VERSION.ordinal()).getNumericCellValue());
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), "4.0.0");
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getNumericCellValue(), DateUtil.getExcelDate(app.getDateCreated(), false));
    }

    @Test
    void generateSpreadsheetWith12Applications() {
        //given: "there are two applications stored in the repository"
        List<Application> apps = applicationRepository.saveAll(List.of(
                new Application(ApplicationType.FUNCTION, Language.KOTLIN, BuildTool.GRADLE_KOTLIN, TestFramework.KOTEST, JdkVersion.JDK_17, "4.0.1"),
                new Application(ApplicationType.CLI, Language.GROOVY, BuildTool.MAVEN, TestFramework.JUNIT, JdkVersion.JDK_20, "4.0.2"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.3"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.4"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.5"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.6"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.7"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.8"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.9"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.10"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.11"),
                new Application(ApplicationType.DEFAULT, Language.JAVA, BuildTool.GRADLE, TestFramework.SPOCK, JdkVersion.JDK_17, "4.0.12")
        ));
        featureRepository.saveAll(List.of(
                new Feature(apps.get(0), "one"),
                new Feature(apps.get(1), "two"),
                new Feature(apps.get(2), "three"),
                new Feature(apps.get(3), "four"),
                new Feature(apps.get(4), "five"),
                new Feature(apps.get(5), "six"),
                new Feature(apps.get(6), "seven"),
                new Feature(apps.get(7), "eight"),
                new Feature(apps.get(8), "nine"),
                new Feature(apps.get(9), "ten"),
                new Feature(apps.get(10), "eleven"),
                new Feature(apps.get(11), "twelve")
        ));
        //when: "the spreadsheet is generated"
        Workbook workbook = excelGenerator.generateExcel();

        //then: "the contents of the spreadsheet are correct"
        assertEquals(1, workbook.getNumberOfSheets());
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals(ExcelGenerator.SHEET_NAME, sheet.getSheetName());
        assertEquals(13, sheet.getPhysicalNumberOfRows());
        Row row = sheet.getRow(0);
        assertTrue(row.getRowStyle().getLocked());
        assertEquals(row.getCell(ID.ordinal()).getStringCellValue(), ID.title);
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), FEATURES.title);
        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), TYPE.title);
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), LANGUAGE.title);
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), BUILD_TOOL.title);
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), TEST_FRAMEWORK.title);
        assertEquals(row.getCell(JDK_VERSION.ordinal()).getStringCellValue(), JDK_VERSION.title);
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), MICRONAUT_VERSION.title);
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getStringCellValue(), DATE_CREATED.title);

        row = sheet.getRow(1);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(0).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "one");
        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), "FUNCTION");
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), "kotlin");
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), "gradle_kotlin");
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), "kotest");
        assertEquals(row.getCell(JDK_VERSION.ordinal()).getNumericCellValue(), 17);
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), "4.0.1");
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getNumericCellValue(), DateUtil.getExcelDate(apps.get(0).getDateCreated(), false));

        row = sheet.getRow(2);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(1).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "two");
        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), "CLI");
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), "groovy");
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), "maven");
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), "junit");
        assertEquals(row.getCell(JDK_VERSION.ordinal()).getNumericCellValue(), 20);
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), "4.0.2");
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getNumericCellValue(), DateUtil.getExcelDate(apps.get(1).getDateCreated(), false));

        row = sheet.getRow(3);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(2).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "three");

        row = sheet.getRow(4);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(3).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "four");

        row = sheet.getRow(5);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(4).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "five");

        row = sheet.getRow(6);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(5).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "six");

        row = sheet.getRow(7);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(6).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "seven");

        row = sheet.getRow(8);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(7).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "eight");

        row = sheet.getRow(9);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(8).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "nine");

        row = sheet.getRow(10);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(9).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "ten");

        row = sheet.getRow(11);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(10).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "eleven");

        row = sheet.getRow(12);
        assertEquals(row.getCell(ID.ordinal()).getNumericCellValue(), apps.get(11).getId().doubleValue());
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), "twelve");
    }

    @Test
    void generateSpreadsheetWithNoApplications() {
        //when: "there is no application stored in the repository"
        Workbook workbook = excelGenerator.generateExcel();

        //then: "the contents of the spreadsheet are correct"
        assertEquals(1, workbook.getNumberOfSheets());
        Sheet sheet = workbook.getSheetAt(0);
        assertEquals(ExcelGenerator.SHEET_NAME, sheet.getSheetName());
        assertEquals(1, sheet.getPhysicalNumberOfRows());
        Row row = sheet.getRow(0);
        assertTrue(row.getRowStyle().getLocked());
        assertEquals(row.getCell(ID.ordinal()).getStringCellValue(), ID.title);
        assertEquals(row.getCell(FEATURES.ordinal()).getStringCellValue(), FEATURES.title);
        assertEquals(row.getCell(TYPE.ordinal()).getStringCellValue(), TYPE.title);
        assertEquals(row.getCell(LANGUAGE.ordinal()).getStringCellValue(), LANGUAGE.title);
        assertEquals(row.getCell(BUILD_TOOL.ordinal()).getStringCellValue(), BUILD_TOOL.title);
        assertEquals(row.getCell(TEST_FRAMEWORK.ordinal()).getStringCellValue(), TEST_FRAMEWORK.title);
        assertEquals(row.getCell(JDK_VERSION.ordinal()).getStringCellValue(), JDK_VERSION.title);
        assertEquals(row.getCell(MICRONAUT_VERSION.ordinal()).getStringCellValue(), MICRONAUT_VERSION.title);
        assertEquals(row.getCell(DATE_CREATED.ordinal()).getStringCellValue(), DATE_CREATED.title);
    }
}
