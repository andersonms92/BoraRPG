plugins { id("borarpg.android.feature") }

dependencies {
    implementation(project(":core:data"))
    implementation(project(":feature:missions"))
    implementation(project(":core:tracking"))
    implementation(libs.maplibre)
    implementation(libs.activity.compose)
}
