plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.lamontlabs.quantravision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lamontlabs.quantravision"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0-seed"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // OpenCV (SDK AAR)
    implementation(files("libs/opencv-4.8.0.aar"))

    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // Room (local provenance DB)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Security (for hashing)
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
}
