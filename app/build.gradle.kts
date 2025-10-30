plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.lamontlabs.quantravision"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lamontlabs.quantravision"
        minSdk = 26
        targetSdk = 35
        versionCode = 21
        versionName = "2.1"
        vectorDrawables.useSupportLibrary = true
        
        // App description metadata
        manifestPlaceholders["appDescription"] = "World's best offline AI pattern detection with predictive intelligence, gamification, and explainable AI"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += setOf("META-INF/DEPENDENCIES", "META-INF/INDEX.LIST")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android - Updated to latest stable versions (Oct 2025)
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")

    // Room Database - Updated to 2.8.3 (Oct 2025)
    implementation("androidx.room:room-runtime:2.8.3")
    implementation("androidx.room:room-ktx:2.8.3")
    kapt("androidx.room:room-compiler:2.8.3")

    // Jetpack Compose - Updated to latest stable (Oct 2025)
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.compose.ui:ui:1.9.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.9.4")
    
    // Navigation Compose - Updated to 2.9.5 (Sep 2025)
    implementation("androidx.navigation:navigation-compose:2.9.5")

    // Billing & Security - Updated Billing to 8.0.0 (Oct 2025)
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coroutines - Updated to 1.10.2 (Oct 2025)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    // TensorFlow Lite - Updated to 2.17.0 (2025)
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")

    // CameraX - Updated to 1.5.0 stable (Oct 2025)
    implementation("androidx.camera:camera-core:1.5.0")
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-view:1.5.0")
    
    // OpenCV - Upgraded to 4.10.0 for official AAR support
    implementation("org.opencv:opencv:4.10.0")
    
    // Logging - Already latest
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // JSON & YAML parsing - Updated to latest (2025)
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.yaml:snakeyaml:2.3")

    // Testing - Updated to latest
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Spotless removed - causing build issues
