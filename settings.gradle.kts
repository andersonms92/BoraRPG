pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BoraRPG"

include(":app")

include(":core:domain", ":core:data", ":core:designsystem", ":core:tracking")

include(
    ":feature:journey",
    ":feature:activity",
    ":feature:backpack",
    ":feature:profile",
    ":feature:missions",
)
