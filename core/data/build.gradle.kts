plugins {
    id("borarpg.android.library")
    id("borarpg.android.hilt")
}

extensions.configure<com.android.build.api.dsl.LibraryExtension> {
    sourceSets.named("androidTest") { assets.directories.add("$projectDir/schemas") }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore)
    implementation(libs.coroutines)
    androidTestImplementation(libs.android.test.runner)
    androidTestImplementation(libs.android.test.junit)
}
