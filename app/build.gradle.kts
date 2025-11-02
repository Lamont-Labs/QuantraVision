plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
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
        manifestPlaceholders["appDescription"] = "Offline AI pattern detection with predictive intelligence, gamification, and explainable AI"
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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
    // Core Android - Latest stable versions
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Room Database - Latest with KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Jetpack Compose - Latest stable versions
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.5")
    
    // Navigation Compose - Latest stable
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Billing & Security - Latest stable
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coroutines - Latest stable
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")

    // TensorFlow Lite - Latest stable versions
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
    }
    implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")

    // CameraX - Latest stable versions
    implementation("androidx.camera:camera-core:1.5.0")
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")
    implementation("androidx.camera:camera-view:1.5.0")
    
    // OpenCV - Latest official Maven Central release
    implementation("org.opencv:opencv:4.10.0")
    
    // ML Kit Text Recognition - Latest stable
    implementation("com.google.mlkit:text-recognition:16.0.1")
    
    // Logging - Latest stable
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // JSON & YAML parsing - Latest stable
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.yaml:snakeyaml:2.3")

    // Testing - Latest stable
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
