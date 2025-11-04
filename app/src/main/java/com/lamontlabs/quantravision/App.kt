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
        
        // CRITICAL: Outer try-catch to prevent ANY crash during app initialization
        // This catch block will NEVER be stripped by ProGuard because it catches Throwable
        // and has explicit field assignment (side effect)
        try {
            initializeOpenCV()
        } catch (t: Throwable) {
            // Catch ANY error including OutOfMemoryError, StackOverflowError, etc.
            openCVInitialized = false
            // Log.e is never stripped by ProGuard
            Log.e("QuantraVision", "Fatal error during initialization - app will run in limited mode", t)
        }
    }
    
    private fun initializeOpenCV() {
        // Initialize OpenCV native library for Maven Central distribution
        // CRITICAL: Use System.loadLibrary() for org.opencv:opencv:4.10.0
        // OpenCVLoader.initDebug() is ONLY for OpenCV Android SDK development and will CRASH in release builds
        try {
            System.loadLibrary("opencv_java4")
            openCVInitialized = true
            Log.e("QuantraVision", "OpenCV loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            // OpenCV native library not available
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV library not found - ML mode only", e)
        } catch (e: Exception) {
            // Other initialization errors
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV init failed - limited mode", e)
        }
    }
}
