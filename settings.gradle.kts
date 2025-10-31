pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.google.devtools.ksp") {
        useModule("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${requested.version}")
      }
    }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "QuantraVision"
include(":app")
