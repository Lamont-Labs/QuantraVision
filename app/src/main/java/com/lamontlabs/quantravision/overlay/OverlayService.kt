package com.lamontlabs.quantravision.overlay

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
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.detection.ProFeatureGate
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.ui.EnhancedOverlayView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.PatternMatch
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var enhancedOverlayView: EnhancedOverlayView? = null
    private var floatingLogo: FloatingLogoButton? = null
    private var floatingMenu: FloatingMenu? = null
    private var scope = CoroutineScope(Dispatchers.Default)
    private var policyApplicator: PowerPolicyApplicator? = null
    private var behavioralGuardrails: BehavioralGuardrails? = null
    private var alertManager: AlertManager? = null
    private var glowingBorderView: GlowingBorderView? = null
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
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(view, params)
            overlayView = view
            
            enhancedOverlayView = view.findViewById(R.id.overlay_canvas)
            if (enhancedOverlayView == null) {
                Log.e(TAG, "CRITICAL: EnhancedOverlayView not found in overlay_layout")
            } else {
                Log.i(TAG, "EnhancedOverlayView initialized successfully")
            }
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
        
        // Add glowing border overlay
        val glowingBorder = GlowingBorderView(this)
        val borderParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(glowingBorder, borderParams)
            glowingBorderView = glowingBorder
            Log.i(TAG, "Glowing border overlay added successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add glowing border view", e)
            // Non-critical - service continues without border
        }
        
        if (ProFeatureGate.hasAccess(this)) {
            behavioralGuardrails = BehavioralGuardrails(this)
            alertManager = AlertManager.getInstance(this)
        }
        
        floatingMenu = FloatingMenu(this, windowManager) {
            stopSelf()
        }
        
        floatingLogo = FloatingLogoButton(this, windowManager).apply {
            onClickListener = {
                openMainApp()
            }
            onLongPressListener = {
                floatingMenu?.show()
            }
            show()
            setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
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
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
                    
                    val dir = java.io.File(applicationContext.filesDir, "demo_charts")
                    if (dir.exists()) {
                        val allDetectedPatterns = mutableListOf<PatternMatch>()
                        
                        dir.listFiles()?.forEach { imageFile ->
                            try {
                                val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                                if (bitmap != null) {
                                    try {
                                        timber.log.Timber.i("HybridDetectorBridge: Processing ${imageFile.name} with optimizations")
                                        val results = detectorBridge.detectPatternsOptimized(bitmap)
                                        timber.log.Timber.d("HybridDetectorBridge: Detected ${results.size} patterns in ${imageFile.name}")
                                        
                                        results.forEach { pattern ->
                                            allDetectedPatterns.add(pattern.toPatternMatch())
                                            
                                            behavioralGuardrails?.let { guardrails ->
                                                scope.launch {
                                                    try {
                                                        val warning = guardrails.recordView(pattern)
                                                        warning?.let { w ->
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(
                                                                    applicationContext,
                                                                    w.message,
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                            
                                                            if (alertManager?.isVoiceEnabled() == true) {
                                                                alertManager?.announceWarning(w.voiceMessage)
                                                            }
                                                            
                                                            timber.log.Timber.w("BehavioralGuardrail triggered: ${w.type.name} - ${w.message}")
                                                        }
                                                    } catch (e: Exception) {
                                                        timber.log.Timber.e(e, "Error recording behavioral view")
                                                    }
                                                }
                                            }
                                        }
                                    } finally {
                                        bitmap.recycle()
                                    }
                                }
                            } catch (e: Exception) {
                                timber.log.Timber.e(e, "Error processing ${imageFile.name} with bridge")
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            enhancedOverlayView?.updateMatches(allDetectedPatterns)
                            floatingLogo?.updatePatternCount(allDetectedPatterns.size)
                            
                            if (allDetectedPatterns.isNotEmpty()) {
                                val highConfidencePattern = allDetectedPatterns.any { it.confidence > 0.85f }
                                if (highConfidencePattern) {
                                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.HIGH_CONFIDENCE)
                                    glowingBorderView?.setPulsing(true)
                                } else {
                                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.PATTERNS_FOUND)
                                    glowingBorderView?.setPulsing(true)
                                }
                            } else {
                                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                                glowingBorderView?.setPulsing(false)
                            }
                        }
                    }
                } catch (e: Exception) {
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
    
    private fun com.lamontlabs.quantravision.detection.DetectionResult.toPatternMatch(): PatternMatch {
        return PatternMatch(
            patternName = this.patternName,
            boundingBox = android.graphics.RectF(
                this.x.toFloat(),
                this.y.toFloat(),
                this.x.toFloat() + this.width.toFloat(),
                this.y.toFloat() + this.height.toFloat()
            ),
            confidence = this.confidence,
            timestamp = this.timestamp
        )
    }
    
    private fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }
    
    private fun startPowerPolicyApplicator() {
        policyApplicator = PowerPolicyApplicator(applicationContext)
        policyApplicator?.start(scope, intervalMs = 5000)
    }

    override fun onDestroy() {
        try {
            policyApplicator?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping power policy applicator on destroy", e)
        }
        
        try {
            floatingLogo?.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up floating logo", e)
        }
        
        try {
            floatingMenu?.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up floating menu", e)
        }
        
        try {
            glowingBorderView?.let { border ->
                if (::windowManager.isInitialized) {
                    windowManager.removeView(border)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error removing glowing border view", e)
        }
        glowingBorderView = null
        
        overlayView?.let { view ->
            try {
                if (::windowManager.isInitialized) {
                    windowManager.removeView(view)
                } else {
                    Log.w(TAG, "WindowManager not initialized during cleanup, skipping removeView")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay view on destroy", e)
            }
        }
        
        try {
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling coroutine scope on destroy", e)
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
