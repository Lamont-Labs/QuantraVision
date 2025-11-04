package com.lamontlabs.quantravision

import android.app.Application
import android.util.Log

/**
 * CRITICAL: Application class initialization
 * 
 * MUST NOT throw exceptions or the app will crash before any Activity starts.
 * ProGuard configuration ensures Log.e() calls are NEVER stripped.
 */
class App : Application() {
    
    companion object {
        @Volatile
        var openCVInitialized: Boolean = false
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        openCVInitialized = false  // Explicit initialization before try block
        
        try {
            try {
                System.loadLibrary("opencv_java4")
                openCVInitialized = true
                Log.e("QuantraVision-App", "✓ OpenCV native library loaded")
            } catch (e: UnsatisfiedLinkError) {
                openCVInitialized = false
                Log.e("QuantraVision-App", "⚠ OpenCV not available - using ML-only mode", e)
            } catch (e: SecurityException) {
                openCVInitialized = false
                Log.e("QuantraVision-App", "⚠ Security exception loading OpenCV", e)
            }
        } catch (t: Throwable) {
            openCVInitialized = false
            Log.e("QuantraVision-App", "⚠ Unexpected error in App.onCreate", t)
        }
        
        Log.e("QuantraVision-App", "✓ Application initialized (OpenCV: $openCVInitialized)")
    }
}
