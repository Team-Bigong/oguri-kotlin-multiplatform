import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.ktlint)
}

abstract class GenerateNetworkConfigTask : DefaultTask() {
    @get:Input
    abstract val debugBaseUrl: Property<String>

    @get:Input
    abstract val releaseBaseUrl: Property<String>

    @get:Input
    abstract val kakaoNativeAppKey: Property<String>

    @get:Input
    abstract val googleWebClientId: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val targetFile = outputFile.get().asFile
        targetFile.parentFile.mkdirs()
        targetFile.writeText(
            """
            package com.bigong.oguri.core.network

            const val DEBUG_BASE_URL: String = "${debugBaseUrl.get()}"
            const val RELEASE_BASE_URL: String = "${releaseBaseUrl.get()}"
            const val KAKAO_NATIVE_APP_KEY: String = "${kakaoNativeAppKey.get()}"
            const val GOOGLE_WEB_CLIENT_ID: String = "${googleWebClientId.get()}"
            """.trimIndent(),
        )
    }
}

val localProperties: Properties =
    Properties().apply {
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { inputStream ->
                load(inputStream)
            }
        }
    }

val debugBaseUrlValue: String =
    (localProperties.getProperty("debug.base.url") ?: "")
        .trim()
        .trimEnd('/')
val releaseBaseUrlValue: String =
    (localProperties.getProperty("release.base.url") ?: "")
        .trim()
        .trimEnd('/')
val kakaoNativeAppKeyValue: String = localProperties.getProperty("kakao.key")?.trim().orEmpty()
val googleWebClientIdValue: String = localProperties.getProperty("google.web.client.id")?.trim().orEmpty()
val admobAndroidAppIdValue: String = "ca-app-pub-2833810411143763~1974881745"

val generatedNetworkConfigDirectory =
    layout.buildDirectory
        .dir("generated/source/networkConfig/commonMain/kotlin")
        .get()
        .asFile
val generatedNetworkConfigFile =
    generatedNetworkConfigDirectory.resolve("com/bigong/oguri/core/network/DebugNetworkConfig.kt")

val generateNetworkConfigTask =
    tasks.register<GenerateNetworkConfigTask>("generateNetworkConfig") {
        debugBaseUrl.set(debugBaseUrlValue)
        releaseBaseUrl.set(releaseBaseUrlValue)
        kakaoNativeAppKey.set(kakaoNativeAppKeyValue)
        googleWebClientId.set(googleWebClientIdValue)
        outputFile.set(generatedNetworkConfigFile)
    }

tasks
    .matching { task ->
        task.name.contains("compile", ignoreCase = true) &&
            task.name.contains("kotlin", ignoreCase = true)
    }.configureEach {
        dependsOn(generateNetworkConfigTask)
    }

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generatedNetworkConfigDirectory)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.kakao.android.user)
            implementation(libs.kakao.android.share)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.google.android.googleid)
            implementation(libs.google.mobile.ads)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.runtime.saveable)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.navigation.compose)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
            implementation(libs.coil.svg)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.bigong.oguri"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.bigong.oguri"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKeyValue
        manifestPlaceholders["admobAndroidAppId"] = admobAndroidAppIdValue
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
