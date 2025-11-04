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
        // ProGuard NOTE: We keep Log.e/w/i to ensure catch blocks have side effects and aren't stripped
        try {
            System.loadLibrary("opencv_java4")
            openCVInitialized = true
            Log.i("QuantraVision", "OpenCV loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            // CRITICAL: This catch block MUST have code that ProGuard won't strip
            // Otherwise the exception will crash the app
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV library not found - ML mode only")
        } catch (e: Exception) {
            // CRITICAL: Keep this catch block with unstripped code
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV init failed - limited mode")
        }
    }
}
