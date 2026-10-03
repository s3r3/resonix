import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
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
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Resonix"

include(
    ":app",
    ":core:common",
    ":core:data",
    ":core:database",
    ":core:datastore",
    ":core:domain",
    ":core:media",
    ":core:model",
    ":core:ui",
    ":feature:media",
    ":feature:network",
    ":feature:player",
    ":feature:playlist",
    ":feature:settings",
)
