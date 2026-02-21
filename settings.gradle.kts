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

        // (اختیاری) اگر یه روزی پلاگین از jitpack خواستی
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ برای کتابخونه‌ها (Dependency ها)
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CompoundEffectV1_01"
include(":app")
 