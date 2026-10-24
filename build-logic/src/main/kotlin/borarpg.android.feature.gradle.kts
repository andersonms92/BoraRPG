plugins {
    id("borarpg.android.library")
    id("borarpg.android.compose")
    id("borarpg.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", project(":core:domain"))
    add("implementation", project(":core:designsystem"))
    add("implementation", catalog.findLibrary("hilt-compose").get())
}
