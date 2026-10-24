plugins { id("org.jetbrains.kotlin.plugin.compose") }

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

plugins.withId("com.android.library") {
    extensions.configure<com.android.build.api.dsl.LibraryExtension> {
        buildFeatures { compose = true }
    }
}

plugins.withId("com.android.application") {
    extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
        buildFeatures { compose = true }
    }
}

dependencies {
    add("implementation", platform(catalog.findLibrary("compose-bom").get()))
    add("implementation", catalog.findLibrary("compose-material3").get())
    add("implementation", catalog.findLibrary("compose-icons").get())
    add("implementation", catalog.findLibrary("lifecycle-runtime-compose").get())
    add("implementation", catalog.findLibrary("lifecycle-viewmodel-compose").get())
}
