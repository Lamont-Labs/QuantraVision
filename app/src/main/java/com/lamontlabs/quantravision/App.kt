package com.lamontlabs.quantravision

import android.app.Application
import android.util.Log
import org.opencv.android.OpenCVLoader

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize OpenCV with proper error handling
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                Log.i("QuantraVision", "OpenCV loaded successfully")
            } else {
                Log.e("QuantraVision", "OpenCV initialization failed")
                // App can still function with limited capabilities
            }
        } catch (e: Exception) {
            Log.e("QuantraVision", "OpenCV initialization exception: ${e.message}", e)
            // Non-fatal - app will work without OpenCV features
        }
    }
}
