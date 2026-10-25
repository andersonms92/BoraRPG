plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}

apply(from = "gradle/architecture.gradle.kts")
