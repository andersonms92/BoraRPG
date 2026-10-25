plugins {
    id("borarpg.android.library")
    id("borarpg.android.hilt")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coroutines)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.junit)
}
