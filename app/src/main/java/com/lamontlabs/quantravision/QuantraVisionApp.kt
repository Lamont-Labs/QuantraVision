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
        
        val modelManager = com.lamontlabs.quantravision.intelligence.llm.ModelManager(this)
        if (modelManager.getModelState() != com.lamontlabs.quantravision.intelligence.llm.ModelState.Downloaded) {
            Log.i("QuantraVision", "Model not imported - disabling OverlayService at OS level")
            com.lamontlabs.quantravision.overlay.OverlayServiceGuard.disable(this)
        }
        
        try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                openCVInitialized = true
                Log.i("QuantraVision", "OpenCV loaded successfully - full pattern detection available")
            } else {
                openCVInitialized = false
                Log.e("QuantraVision", "OpenCV initialization failed - pattern detection limited to ML-only mode")
                
                android.widget.Toast.makeText(
                    this,
                    "Limited Mode: Advanced pattern detection unavailable. App will detect only 6 core patterns via ML.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            openCVInitialized = false
            Log.e("QuantraVision", "OpenCV initialization exception: ${e.message} - limited mode enabled", e)
            
            android.widget.Toast.makeText(
                this,
                "Limited Mode: Template matching disabled. Only 6 ML patterns available.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
        
        StartupDiagnosticCollector.completeStartup()
    }
}
