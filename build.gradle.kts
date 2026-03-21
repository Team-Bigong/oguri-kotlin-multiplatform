import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
}

subprojects {
    pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
        configure<KtlintExtension> {
            version.set("1.4.0")
            debug.set(false)
            verbose.set(true)
            android.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(true)
            enableExperimentalRules.set(false)

            additionalEditorconfig.set(
                mapOf(
                    "ktlint_standard_license-header" to "disabled",
                ),
            )

            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
                include("**/kotlin/**")
            }
        }

        // 빌드 스크립트(.kts) 검사 태스크를 모두 비활성화
        tasks
            .matching {
                it.name.contains("KotlinScript") || it.name.contains("kotlinScript")
            }.configureEach {
                enabled = false
            }
    }

    // 컴파일 시 자동으로 ktlintFormat 실행
    // tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    //     dependsOn(tasks.matching { it.name == "ktlintFormat" })
    // }
}
