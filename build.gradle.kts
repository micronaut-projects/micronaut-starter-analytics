plugins {
    id("groovy")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.0.4"
}

version = "0.1"
group = "io.micronaut.starter.analytics.postgres"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.micronaut.starter:micronaut-starter-core:${gradle.rootProject.extra["micronautVersion"]}")

    // GraalVM
    annotationProcessor("io.micronaut:micronaut-graal")

    // Serialization
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    // Security
    annotationProcessor("io.micronaut.security:micronaut-security-annotations")
    implementation("io.micronaut.security:micronaut-security")

    // Validation
    annotationProcessor("io.micronaut:micronaut-http-validation")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    implementation("io.micronaut.validation:micronaut-validation")

    // Connection Pool
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    // Micronaut Data
    annotationProcessor("io.micronaut.data:micronaut-data-processor")
    implementation("io.micronaut.data:micronaut-data-jdbc")

    // Flyway
    implementation("io.micronaut.flyway:micronaut-flyway")

    // PostgresSQL
    implementation("org.postgresql:postgresql")
    testImplementation("org.testcontainers:postgresql")
    // Cloud SQL https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory/blob/main/docs/jdbc.md#postgres
    runtimeOnly("com.google.cloud.sql:postgres-socket-factory:1.15.0")

    // Logging
    runtimeOnly("ch.qos.logback:logback-classic")

    // Excel
    implementation("org.apache.poi:poi-ooxml:5.2.4")

    compileOnly("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut:micronaut-http-client")

    testAnnotationProcessor("io.micronaut:micronaut-inject-java")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

application {
    mainClass.set("io.micronaut.starter.analytics.postgres.Main")
}
java {
    sourceCompatibility = JavaVersion.toVersion("17")
    targetCompatibility = JavaVersion.toVersion("17")
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("netty")
    testRuntime("spock2")
    processing {
        incremental(true)
        annotations("io.micronaut.starter.analytics.postgres.*")
    }
}



