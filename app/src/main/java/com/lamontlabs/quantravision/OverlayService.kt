package com.lamontlabs.quantravision

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.ml.PowerPolicyApplicator
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var scope = CoroutineScope(Dispatchers.Default)
    private var policyApplicator: PowerPolicyApplicator? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
        startForegroundService()
        startDetectionLoop()
        startPowerPolicyApplicator()
    }

    private fun startForegroundService() {
        val channelId = "QuantraVisionOverlay"
        val channel = NotificationChannel(channelId, "Overlay", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("QuantraVision Overlay")
            .setContentText("Running detection service with AI optimizations")
            .setSmallIcon(R.drawable.ic_overlay_marker)
            .build()
        startForeground(1, notification)
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
        policyApplicator?.stop()
        windowManager.removeView(overlayView)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
