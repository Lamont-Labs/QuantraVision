buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.6.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.0.21")
    }
}

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
}

tasks.register("showRealErrors") {
    group = "help"
    description = "Disables KAPT to reveal actual compilation errors"
    
    doFirst {
        allprojects {
            tasks.matching { it.name.contains("kapt", ignoreCase = true) }.forEach { kaptTask ->
                kaptTask.enabled = false
                println("Disabled KAPT task: ${kaptTask.name}")
            }
        }
    }
}
