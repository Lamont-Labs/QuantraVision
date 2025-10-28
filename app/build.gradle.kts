plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt") // must match above
}

android {
    namespace = "com.lamontlabs.quantravision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lamontlabs.quantravision"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "1.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
// ADD the Billing dependency (keep the rest of your file unchanged)
dependencies {
    // ...
    implementation("com.android.billingclient:billing-ktx:6.1.0")
}
// app/build.gradle.kts (module)
plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.lamontlabs.quantravision"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.lamontlabs.quantravision"
    minSdk = 26
    targetSdk = 34
    versionCode = 13
    versionName = "1.3"
    vectorDrawables.useSupportLibrary = true
  }

  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
  packaging { resources { excludes += ["META-INF/DEPENDENCIES","META-INF/INDEX.LIST"] } }
}

dependencies {
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("com.google.android.material:material:1.12.0")

  // Compose
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui:1.7.2")
  implementation("androidx.compose.material3:material3:1.3.0")
  implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
  debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")

  // Billing
  implementation("com.android.billingclient:billing-ktx:6.2.1")
}

apply(plugin = "com.diffplug.spotless")
spotless {
  kotlin { target("**/*.kt"); ktlint("1.2.1").editorConfigOverride(mapOf("indent_size" to "2","max_line_length" to "140")) }
  format("xml") { target("**/*.xml"); trimTrailingWhitespace(); endWithNewline() }
}
tasks.named("preBuild").configure { dependsOn("spotlessApply") }
