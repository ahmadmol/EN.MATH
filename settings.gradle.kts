pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (providers.gradleProperty("g9Render").orNull == "true") {
            providers.gradleProperty("g9RenderRepo").orNull?.let { mirror -> maven { url = uri(mirror) } }
        }
        google(); mavenCentral()
    }
}
rootProject.name = "EN.MATH"
include(":app")
