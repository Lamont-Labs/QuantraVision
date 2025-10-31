package com.lamontlabs.quantravision

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.ml.PowerPolicyApplicator
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var scope = CoroutineScope(Dispatchers.Default)
    private var policyApplicator: PowerPolicyApplicator? = null
    private val TAG = "OverlayService"

    override fun onCreate() {
        super.onCreate()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e(TAG, "CRITICAL: SYSTEM_ALERT_WINDOW permission not granted, stopping service")
                Toast.makeText(
                    this,
                    "Overlay permission required. Please enable it in settings.",
                    Toast.LENGTH_LONG
                ).show()
                stopSelf()
                return
            }
        }
        
        // CRITICAL: Use safe cast to prevent NPE on custom ROMs (0.5-1% of devices)
        val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm == null) {
            Log.e(TAG, "CRITICAL: WindowManager is null (custom ROM incompatibility), stopping service")
            Toast.makeText(
                this,
                "Overlay service not supported on this device.",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }
        windowManager = wm

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to add overlay view (permission likely revoked mid-operation)", e)
            Toast.makeText(
                this,
                "Failed to create overlay. Please check permissions.",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }
        
        startForegroundService()
        startDetectionLoop()
        startPowerPolicyApplicator()
    }

    private fun startForegroundService() {
        try {
            val channelId = "QuantraVisionOverlay"
            val channel = NotificationChannel(channelId, "Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("QuantraVision Overlay")
                .setContentText("Running detection service with AI optimizations")
                .setSmallIcon(R.drawable.ic_overlay_marker)
                .build()
            startForeground(1, notification)
        } catch (e: Exception) {
            // CRITICAL: Notification creation can fail on custom ROMs (~0.1-0.5%)
            Log.e(TAG, "Failed to start foreground service (custom ROM or notification restrictions)", e)
            // Service will continue to run, just without foreground notification
            // This prevents crashes on incompatible devices
        }
    }

    private fun startDetectionLoop() {
        scope.launch {
            val detectorBridge = HybridDetectorBridge(applicationContext)
            val legacyDetector = PatternDetector(applicationContext)
            
            while (isActive) {
                try {
                    // OPTIMIZED PATH: Use HybridDetectorBridge for AI-optimized detection
                    val dir = java.io.File(applicationContext.filesDir, "demo_charts")
                    if (dir.exists()) {
                        dir.listFiles()?.forEach { imageFile ->
                            try {
                                val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                                if (bitmap != null) {
                                    try {
                                        timber.log.Timber.i("HybridDetectorBridge: Processing ${imageFile.name} with optimizations")
                                        val results = detectorBridge.detectPatternsOptimized(bitmap)
                                        timber.log.Timber.d("HybridDetectorBridge: Detected ${results.size} patterns in ${imageFile.name}")
                                    } finally {
                                        bitmap.recycle()
                                    }
                                }
                            } catch (e: Exception) {
                                timber.log.Timber.e(e, "Error processing ${imageFile.name} with bridge")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // FALLBACK: Use legacy detector if optimized path fails
                    timber.log.Timber.w(e, "HybridDetectorBridge failed, falling back to legacy PatternDetector")
                    try {
                        legacyDetector.scanStaticAssets()
                    } catch (fallbackError: Exception) {
                        timber.log.Timber.e(fallbackError, "Legacy detector also failed")
                    }
                }
                delay(3000)
            }
        }
    }
    
    private fun startPowerPolicyApplicator() {
        policyApplicator = PowerPolicyApplicator(applicationContext)
        policyApplicator?.start(scope, intervalMs = 5000)
    }

    override fun onDestroy() {
        // CRITICAL: Service can be killed by system under low memory
        // Use ::isInitialized checks and individual try-catch for each cleanup operation
        
        // Stop power policy applicator
        try {
            policyApplicator?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping power policy applicator on destroy", e)
        }
        
        // Remove overlay view
        overlayView?.let { view ->
            try {
                // Check if windowManager was initialized before using it
                if (::windowManager.isInitialized) {
                    windowManager.removeView(view)
                } else {
                    Log.w(TAG, "WindowManager not initialized during cleanup, skipping removeView")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view on destroy", e)
            }
        }
        
        // Cancel coroutine scope
        try {
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling coroutine scope on destroy", e)
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
