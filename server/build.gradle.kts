plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBoot)
    alias(libs.plugins.dependencyManagement)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.ktlint)
}

group = "com.bigong.oguri"
version = "1.0.0"

springBoot {
    mainClass.set("com.bigong.oguri.Application")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.kotlin.reflect)
    implementation(libs.springdoc.openapi)
    implementation(libs.jjwt.api)
    implementation(libs.google.api.client)
    implementation(libs.google.oauth.client)
    implementation(libs.google.http.client)
    implementation(libs.google.http.client.gson)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("server-1.0.0.jar")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    val verifyTravelPostgres = providers.environmentVariable("TRAVEL_TEST_DATABASE_URL")
        .map { it.startsWith("jdbc:postgresql:") }
        .getOrElse(false)
    inputs.property("travelPostgresIntegrationEnabled", verifyTravelPostgres)
    outputs.upToDateWhen { !verifyTravelPostgres }
    outputs.doNotCacheIf("PostgreSQL integration tests require a live database") { verifyTravelPostgres }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}
