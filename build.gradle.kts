plugins { id("com.diffplug.spotless") version "6.25.0" apply false }
plugins {
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.24" apply false
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
