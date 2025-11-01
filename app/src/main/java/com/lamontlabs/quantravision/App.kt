package com.lamontlabs.quantravision

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

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
        
        // Initialize OpenCV with proper error handling and user notification
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                openCVInitialized = true
                Log.i("QuantraVision", "OpenCV loaded successfully - full pattern detection available")
            } else {
                openCVInitialized = false
                Log.e("QuantraVision", "OpenCV initialization failed - pattern detection limited to ML-only mode")
                
                // Notify user that pattern detection is running in limited mode
                android.widget.Toast.makeText(
                    this,
                    "⚠️ Limited Mode: Advanced pattern detection unavailable. App will detect only 6 core patterns via ML.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
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
