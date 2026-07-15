enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        if (System.getenv("CI") != "true") {
            mavenLocal()
        }
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (System.getenv("CI") != "true") {
            mavenLocal()
        }
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
rootProject.name = "JL-Mod-Plus"
include(":app", ":dexlib")
