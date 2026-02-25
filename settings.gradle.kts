pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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
 