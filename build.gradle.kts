// Project-level Gradle
plugins {
  id("com.android.application") version "8.5.0" apply false
  id("org.jetbrains.kotlin.android") version "1.9.24" apply false
}
tasks.register<Delete>("clean") { delete(rootProject.buildDir) }
