plugins {
    alias(libs.plugins.kotlin.jvm)
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin { compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }

dependencies {
    api(libs.coroutines.core)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
