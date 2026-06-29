pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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

rootProject.name = "MarketBasketAndroid"
include(":app")
include(":core:domain")
include(":core:designsystem")
include(":core:data")
include(":feature:list")
include(":feature:deals")
include(":feature:today")
include(":feature:onboarding")
include(":feature:chat")
include(":feature:ideas")
