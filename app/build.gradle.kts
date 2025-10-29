plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.lamontlabs.quantravision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lamontlabs.quantravision"
        minSdk = 26
        targetSdk = 34
        versionCode = 20
        versionName = "2.0"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += setOf("META-INF/DEPENDENCIES", "META-INF/INDEX.LIST")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Jetpack Compose
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.2")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.2")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.2")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Billing & Security
    implementation("com.android.billingclient:billing-ktx:6.2.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.13.0")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    
    // OpenCV (using Maven Central distribution)
    // Note: If this fails, download opencv-4.8.0-android-sdk.zip from https://opencv.org/releases/
    // and place the .aar file in app/libs/, then uncomment the line below:
    // implementation(files("libs/opencv-4.8.0.aar"))
    implementation("org.opencv:opencv-android:4.8.0") {
        isTransitive = false
    }
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // JSON & YAML parsing
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.yaml:snakeyaml:2.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Spotless removed - causing build issues
