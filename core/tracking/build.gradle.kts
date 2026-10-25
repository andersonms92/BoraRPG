plugins {
    id("borarpg.android.library")
    id("borarpg.android.hilt")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.coroutines)
}
