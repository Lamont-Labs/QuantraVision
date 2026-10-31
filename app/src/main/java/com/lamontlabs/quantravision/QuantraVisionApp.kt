package com.lamontlabs.quantravision

import android.app.Application
import android.util.Log
import com.lamontlabs.quantravision.devbot.diagnostics.StartupDiagnosticCollector
import org.opencv.android.OpenCVLoader

class QuantraVisionApp : Application() {
    
    companion object {
        @Volatile
        var openCVInitialized: Boolean = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        StartupDiagnosticCollector.start()
        
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                openCVInitialized = true
                com.lamontlabs.quantravision.overlay.OverlayServiceGuard.enable(this)
                Log.i("QuantraVision", "OpenCV loaded successfully - full pattern detection available")
            } else {
                openCVInitialized = false
                Log.e("QuantraVision", "OpenCV initialization failed - scanner unavailable")
                
                android.widget.Toast.makeText(
                    this,
                    "Chart scanner unavailable: OpenCV could not initialize.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV initialization exception: ${e.message} - scanner unavailable", e)
            
            android.widget.Toast.makeText(
                this,
                "Chart scanner unavailable: OpenCV could not initialize.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        
        StartupDiagnosticCollector.completeStartup()
    }
}
