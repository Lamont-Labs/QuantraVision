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
        versionCode = 35
        versionName = "3.1.0"
        vectorDrawables.useSupportLibrary = true
        
        // Target arm64-v8a only for Samsung S23 FE (saves ~40MB)
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        
        // Build fingerprint for DevBot diagnostics (configuration-cache safe)
        val gitHash = runCatching {
            providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText.map { it.trim() }.orElse("no-git").get()
        }.getOrElse { "no-git" }
        
        val buildTimestamp = System.getenv("BUILD_TIMESTAMP") ?: System.currentTimeMillis().toString()
        val timestampShort = when {
            buildTimestamp.isBlank() -> "unknown"
            buildTimestamp.length >= 19 -> buildTimestamp.substring(0, 19)
            else -> buildTimestamp
        }
        val buildId = "$timestampShort-$gitHash"
        
        buildConfigField("String", "GIT_HASH", "\"$gitHash\"")
        buildConfigField("String", "BUILD_TIMESTAMP", "\"$buildTimestamp\"")
        buildConfigField("String", "BUILD_ID", "\"$buildId\"")
        buildConfigField("String", "BUILD_FINGERPRINT", "\"v${versionName}-${versionCode}-$gitHash\"")
        
        // App description metadata
        manifestPlaceholders["appDescription"] = "Offline AI pattern detection with predictive intelligence, gamification, and explainable AI"
        
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    // Code signing configuration for Codemagic CI/CD
    signingConfigs {
        create("release") {
            // Codemagic auto-populates these environment variables when keystore is uploaded
            if (System.getenv("CI") != null) {
                val keystorePath = System.getenv("CM_KEYSTORE_PATH")
                if (keystorePath != null && File(keystorePath).exists()) {
                    storeFile = file(keystorePath)
                    storePassword = System.getenv("CM_KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("CM_KEY_ALIAS")
                    keyPassword = System.getenv("CM_KEY_PASSWORD")
                }
            }
            // For local signing: Configure in gradle.properties or local.properties
            // DO NOT commit keystore passwords to version control
        }
    }

    buildTypes {
        release {
            // Use signing config if available (Codemagic CI or local keystore)
            if (signingConfigs.findByName("release")?.storeFile?.exists() == true) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            // Aggressive APK optimization
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isMinifyEnabled = false
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module"
            )
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
    
    // APK splits for different architectures (reduces APK size)
    // DISABLED: Building single arm64-v8a APK via ndk.abiFilters instead
    // Universal APK was causing 250MB bloat by including all architectures
    splits {
        abi {
            isEnable = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = false
    }

    kotlinOptions {
        jvmTarget = "17"
        
        // Kotlin compiler optimizations
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xjvm-default=all",
            "-Xbackend-threads=0"
        )
    }
    
    // Build features configuration
    buildFeatures {
        compose = true
        buildConfig = true
        aidl = false
        renderScript = false
        shaders = false
        resValues = false
    }
}

// Force dependency versions to stay compatible with SDK 35
configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.15.0")
        force("androidx.core:core-ktx:1.15.0")
        force("androidx.activity:activity:1.9.3")
        force("androidx.activity:activity-ktx:1.9.3")
        force("androidx.activity:activity-compose:1.9.3")
    }
}

dependencies {
    // Core Android - Pinned to SDK 35 compatible versions
    implementation("androidx.core:core-ktx:1.15.0") {
        // Prevent auto-update to 1.17.0 which requires SDK 36
        version { strictly("1.15.0") }
    }
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Room Database - Latest with KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Jetpack Compose - Pinned to SDK 35 compatible versions
    implementation("androidx.activity:activity-compose:1.9.3") {
        // Prevent auto-update to 1.12.0 which requires SDK 36
        version { strictly("1.9.3") }
    }
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.5")
    
    // Navigation Compose - Latest stable
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Billing & Security - Kotlin 1.9.25 compatible versions
    implementation("com.android.billingclient:billing-ktx:7.1.1")  // 8.0.0 requires Kotlin 2.0+
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Coroutines - Kotlin 1.9.25 compatible version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")  // 1.10.1 requires Kotlin 2.0+
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")  // For Google Play Services Task<T>.await()

    // WorkManager - For background learning tasks
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // TensorFlow Lite - Latest stable versions
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-support-api")
        exclude(group = "com.google.ai.edge.litert", module = "litert-support")
        exclude(group = "com.google.ai.edge.litert", module = "litert-support-api")
    }
    implementation("org.tensorflow:tensorflow-lite-task-text:0.4.4") {
        exclude(group = "org.tensorflow", module = "tensorflow-lite")
        exclude(group = "org.tensorflow", module = "tensorflow-lite-api")
        exclude(group = "com.google.ai.edge.litert", module = "litert")
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    // GPU delegate disabled due to compatibility issues in 2.17.0
    // implementation("org.tensorflow:tensorflow-lite-gpu:2.17.0")
    
    // MediaPipe LLM Inference - REMOVED (saved 12MB)
    // Was only used by old GemmaEngine.kt (555MB model)
    // Replaced with TFLite ensemble models (47MB total, bundled in APK)
    // implementation("com.google.mediapipe:tasks-genai:0.10.27")

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
    
    // PDF generation for trading notebooks
    implementation("com.itextpdf:itextpdf:5.5.13.3")
    
    // QR Code generation - ZXing
    implementation("com.google.zxing:core:3.5.3")
    
    // Material Icons Extended - For additional icons
    implementation("androidx.compose.material:material-icons-extended:1.7.5")

    // Testing - Latest stable
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
