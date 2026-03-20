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
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
