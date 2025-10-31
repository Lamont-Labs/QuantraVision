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
        
        // Initialize OpenCV with proper error handling
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                openCVInitialized = true
                Log.i("QuantraVision", "OpenCV loaded successfully - flag set to true")
            } else {
                openCVInitialized = false
                Log.e("QuantraVision", "OpenCV initialization failed - flag remains false")
                // App can still function with limited capabilities
            }
        } catch (e: Exception) {
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV initialization exception: ${e.message} - flag set to false", e)
            // Non-fatal - app will work without OpenCV features
        }
    }
}
