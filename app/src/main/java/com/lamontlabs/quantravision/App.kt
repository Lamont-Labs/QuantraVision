package com.lamontlabs.quantravision

import android.app.Application
import android.util.Log

class App : Application() {
    
    companion object {
        /**
         * Global flag indicating whether OpenCV was successfully initialized.
         * Downstream code MUST check this before using OpenCV features.
         * @Volatile ensures visibility across threads
         */
        @Volatile
        var openCVInitialized: Boolean = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize OpenCV native library for Maven Central distribution
        // CRITICAL: Use System.loadLibrary() for org.opencv:opencv:4.10.0
        // OpenCVLoader.initDebug() is ONLY for OpenCV Android SDK development and will CRASH in release builds
        try {
            System.loadLibrary("opencv_java4")
            openCVInitialized = true
            Log.i("QuantraVision", "OpenCV loaded successfully - full pattern detection available")
        } catch (e: UnsatisfiedLinkError) {
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV native library not found: ${e.message} - pattern detection limited to ML-only mode", e)
            
            // Notify user that pattern detection is running in limited mode
            android.widget.Toast.makeText(
                this,
                "⚠️ Limited Mode: Advanced pattern detection unavailable. App will detect only 6 core patterns via ML.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV initialization exception: ${e.message} - limited mode enabled", e)
            
            // Notify user about limited functionality
            android.widget.Toast.makeText(
                this,
                "⚠️ Limited Mode: Template matching disabled. Only 6 ML patterns available.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
}
