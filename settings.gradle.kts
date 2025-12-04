pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Add JetBrains Compose repository
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JetBrains Compose repository here as well
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
rootProject.name = "inventra"
include(":app", ":shared")