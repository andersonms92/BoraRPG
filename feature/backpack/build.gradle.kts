plugins { id("borarpg.android.feature") }

dependencies {
    implementation(project(":feature:missions"))
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
