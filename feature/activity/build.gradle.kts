plugins { id("borarpg.android.feature") }

dependencies {
    implementation(project(":feature:missions"))
    implementation(libs.maplibre)
    implementation(libs.activity.compose)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
