plugins {
    alias(libs.plugins.android.application)
    id("borarpg.android.compose")
    id("borarpg.android.hilt")
}

android {
    namespace = "com.borarpg"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.borarpg"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":feature:journey"))
    implementation(project(":feature:activity"))
    implementation(project(":feature:backpack"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:missions"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:tracking"))
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.junit)
    implementation(libs.activity.compose)
    implementation(libs.hilt.compose)
}
