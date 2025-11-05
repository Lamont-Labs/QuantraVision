package com.lamontlabs.quantravision.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.ml.PowerPolicyApplicator
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.ui.EnhancedOverlayView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.PatternMatch
import kotlinx.coroutines.*
import java.nio.ByteBuffer

/**
 * Safe Timber logging wrapper for Service context
 * Falls back to Log.* if Timber isn't initialized (common in Services)
 */
private object SafeLog {
    fun i(tag: String, message: String) {
        try {
            SafeLog.i(TAG, message)
        } catch (e: Throwable) {
            Log.i(tag, message)
        }
    }
    
    fun d(tag: String, message: String) {
        try {
            SafeLog.d(TAG, message)
        } catch (e: Throwable) {
            Log.d(tag, message)
        }
    }
    
    fun w(tag: String, message: String) {
        try {
            SafeLog.w(TAG, message)
        } catch (e: Throwable) {
            Log.w(tag, message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                SafeLog.e(TAG, message, throwable)
            } else {
                timber.log.Timber.e(message)
            }
        } catch (e: Throwable) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
}

class OverlayService : Service() {

    companion object {
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        private const val VIRTUAL_DISPLAY_NAME = "QuantraVisionCapture"
        private const val SERVICE_READY_ACTION = "com.lamontlabs.quantravision.OVERLAY_SERVICE_READY"
    }

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
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensity: Int = 0

    override fun onCreate() {
        super.onCreate()
        
        Log.i(TAG, "OverlayService onCreate() - Android ${Build.VERSION.SDK_INT}")
        
        // ANDROID 14+ FIX: Start foreground service FIRST before anything else
        // Research shows this is the #1 crash cause for MediaProjection + Overlay apps
        val foregroundSuccess = startForegroundService()
        if (!foregroundSuccess) {
            Log.e(TAG, "CRITICAL: Failed to start foreground service, stopping")
            Toast.makeText(
                this,
                "Failed to start overlay service. Please try again.",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }
        
        // Verify overlay permission (secondary check after foreground service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e(TAG, "CRITICAL: SYSTEM_ALERT_WINDOW permission not granted")
                Toast.makeText(
                    this,
                    "Overlay permission required. Please enable it in settings.",
                    Toast.LENGTH_LONG
                ).show()
                stopSelf()
                return
            }
        }
        
        // Initialize WindowManager (verified after foreground service started)
        val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        if (wm == null) {
            Log.e(TAG, "CRITICAL: WindowManager is null (custom ROM incompatibility)")
            Toast.makeText(
                this,
                "Overlay service not supported on this device.",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }
        windowManager = wm
        
        // Get screen dimensions early
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
        
        Log.i(TAG, "OverlayService onCreate() completed - screen: ${screenWidth}x${screenHeight}@${screenDensity}dpi")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand() called - foreground service already running")
        
        // ANDROID 14+ FIX: Create overlay views AFTER foreground service is running
        // This prevents BadTokenException and SecurityException crashes
        if (overlayView == null) {
            if (!createOverlayViews()) {
                Log.e(TAG, "Failed to create overlay views, stopping service")
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        // Initialize MediaProjection AFTER overlay views are created
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        
        if (resultCode == -1 || data == null) {
            Log.w(TAG, "MediaProjection extras not provided - falling back to demo mode")
            startDetectionLoop(useDemoMode = true)
        } else {
            try {
                Log.i(TAG, "Initializing MediaProjection with resultCode=$resultCode")
                initializeMediaProjection(resultCode, data)
                startDetectionLoop(useDemoMode = false)
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL: Failed to initialize MediaProjection", e)
                Toast.makeText(
                    this,
                    "Failed to start screen capture: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        // Send ready broadcast
        try {
            val readyIntent = Intent(SERVICE_READY_ACTION)
            sendBroadcast(readyIntent)
            Log.i(TAG, "OverlayService ready broadcast sent")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ready broadcast", e)
        }
        
        return START_NOT_STICKY
    }
    
    private fun createOverlayViews(): Boolean {
        try {
            Log.i(TAG, "Creating overlay views...")
            
            // Create main overlay view
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

            windowManager.addView(view, params)
            overlayView = view
            
            enhancedOverlayView = view.findViewById(R.id.overlay_canvas)
            if (enhancedOverlayView == null) {
                Log.e(TAG, "EnhancedOverlayView not found in overlay_layout")
            } else {
                Log.i(TAG, "EnhancedOverlayView initialized successfully")
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

            windowManager.addView(glowingBorder, borderParams)
            glowingBorderView = glowingBorder
            Log.i(TAG, "Glowing border overlay added successfully")
            
            // Initialize Pro features if licensed
            if (ProFeatureGate.isActive(this)) {
                behavioralGuardrails = BehavioralGuardrails(this)
                alertManager = AlertManager.getInstance(this)
            }
            
            // Create floating menu
            floatingMenu = FloatingMenu(this, windowManager) {
                stopSelf()
            }
            
            // Create floating logo button
            floatingLogo = FloatingLogoButton(this, windowManager).apply {
                onClickListener = {
                    val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        putExtra("opened_from_overlay", true)
                    }
                    startActivity(intent)
                }
                onLongPressListener = {
                    floatingMenu?.show()
                }
                show()
                setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            }
            
            // Start power policy applicator
            startPowerPolicyApplicator()
            
            Log.i(TAG, "All overlay views created successfully")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to create overlay views", e)
            Toast.makeText(
                this,
                "Failed to create overlay: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }
    
    private fun initializeMediaProjection(resultCode: Int, data: Intent) {
        Log.i(TAG, "Initializing MediaProjection - Android ${Build.VERSION.SDK_INT}, Manufacturer: ${Build.MANUFACTURER}")
        
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        
        if (projectionManager == null) {
            Log.e(TAG, "CRITICAL: MediaProjectionManager not available on ${Build.MANUFACTURER} ${Build.MODEL}")
            throw IllegalStateException("MediaProjectionManager not available on this device")
        }
        
        try {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        } catch (e: SecurityException) {
            Log.e(TAG, "CRITICAL: SecurityException creating MediaProjection - likely permission issue on Android 14+", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Unexpected exception creating MediaProjection", e)
            throw e
        }
        
        if (mediaProjection == null) {
            Log.e(TAG, "Failed to create MediaProjection")
            throw IllegalStateException("Failed to create MediaProjection")
        }
        
        // Register callback to handle when user stops sharing via system notification
        // ANDROID 14+ FIX: Must specify Handler(Looper.getMainLooper()) instead of null
        // Research shows null handler causes crashes on Samsung devices
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() {
                Log.i(TAG, "MediaProjection stopped by user - cleaning up service")
                cleanupMediaProjection()
                Toast.makeText(
                    applicationContext,
                    "Screen sharing stopped",
                    Toast.LENGTH_SHORT
                ).show()
                stopSelf()
            }
        }, android.os.Handler(android.os.Looper.getMainLooper()))
        
        // Create ImageReader with RGBA_8888 format (hardware accelerated)
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2  // maxImages - double buffering
        )
        
        // CRITICAL MEMORY LEAK FIX: Must provide VirtualDisplay.Callback (not null!)
        // Research shows virtualDisplay.release() does NOTHING if callback is null
        val virtualDisplayCallback = object : VirtualDisplay.Callback() {
            override fun onPaused() {
                Log.d(TAG, "VirtualDisplay paused")
            }
            override fun onResumed() {
                Log.d(TAG, "VirtualDisplay resumed")
            }
            override fun onStopped() {
                Log.i(TAG, "VirtualDisplay stopped")
            }
        }
        
        // Create VirtualDisplay that renders to ImageReader surface
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            virtualDisplayCallback,  // CRITICAL: Not null!
            android.os.Handler(android.os.Looper.getMainLooper())
        )
        
        if (virtualDisplay == null) {
            Log.e(TAG, "Failed to create VirtualDisplay")
            cleanupMediaProjection()
            throw IllegalStateException("Failed to create VirtualDisplay")
        }
        
        Log.i(TAG, "MediaProjection initialized successfully: ${screenWidth}x${screenHeight}@${screenDensity}dpi")
    }

    private fun startForegroundService(): Boolean {
        try {
            val channelId = "QuantraVisionOverlay"
            val channel = NotificationChannel(channelId, "Overlay", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("QuantraVision Overlay")
                .setContentText("Running detection service with AI optimizations")
                .setSmallIcon(R.drawable.ic_overlay_marker)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true)
                .build()
            
            // ANDROID 14+ FIX: Must specify FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            // Research shows this is mandatory for MediaProjection services on API 29+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    1, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                )
                Log.i(TAG, "Foreground service started with MEDIA_PROJECTION type (Android ${Build.VERSION.SDK_INT})")
            } else {
                startForeground(1, notification)
                Log.i(TAG, "Foreground service started (Android ${Build.VERSION.SDK_INT})")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to start foreground service", e)
            return false
        }
    }

    private fun startDetectionLoop(useDemoMode: Boolean = false) {
        scope.launch {
            Log.i(TAG, "Starting detection loop - demoMode=$useDemoMode, OpenCV=${com.lamontlabs.quantravision.App.openCVInitialized}")
            
            val detectorBridge = try {
                HybridDetectorBridge(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "CRITICAL: Failed to initialize HybridDetectorBridge", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Pattern detector initialization failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return@launch
            }
            
            val legacyDetector = try {
                PatternDetector(applicationContext)
            } catch (e: Exception) {
                Log.e(TAG, "WARNING: Failed to initialize legacy PatternDetector, continuing with hybrid only", e)
                null
            }
            
            while (isActive) {
                try {
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
                    
                    val allDetectedPatterns = mutableListOf<PatternMatch>()
                    
                    if (useDemoMode) {
                        // ========================================
                        // DEMO MODE: Fallback for testing without MediaProjection
                        // ========================================
                        val dir = java.io.File(applicationContext.filesDir, "demo_charts")
                        if (dir.exists()) {
                            dir.listFiles()?.forEach { imageFile ->
                                try {
                                    val bitmap = android.graphics.BitmapFactory.decodeFile(imageFile.absolutePath)
                                    if (bitmap != null) {
                                        try {
                                            SafeLog.i(TAG, "DEMO: Processing ${imageFile.name}")
                                            val results = detectorBridge.detectPatternsOptimized(bitmap)
                                            results.forEach { pattern ->
                                                allDetectedPatterns.add(pattern.toPatternMatch())
                                            }
                                        } finally {
                                            bitmap.recycle()
                                        }
                                    }
                                } catch (e: Exception) {
                                    SafeLog.e(TAG, "Error processing ${imageFile.name}", e)
                                }
                            }
                        }
                    } else {
                        // ========================================
                        // REAL-TIME SCREEN CAPTURE MODE
                        // ========================================
                        val capturedBitmap = captureScreen()
                        
                        if (capturedBitmap != null) {
                            try {
                                SafeLog.d(TAG, "Captured screen: ${capturedBitmap.width}x${capturedBitmap.height}")
                                val results = detectorBridge.detectPatternsOptimized(capturedBitmap)
                                SafeLog.d(TAG, "Detected ${results.size} patterns on live screen")
                                
                                results.forEach { pattern ->
                                    val patternMatch = pattern.toPatternMatch()
                                    allDetectedPatterns.add(patternMatch)
                                    
                                    behavioralGuardrails?.let { guardrails ->
                                        scope.launch {
                                            try {
                                                val warning = guardrails.recordView(patternMatch)
                                                warning?.let { w ->
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(
                                                            applicationContext,
                                                            w.message,
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                    SafeLog.w(TAG, "BehavioralGuardrail: ${w.type.name}")
                                                }
                                            } catch (e: Exception) {
                                                SafeLog.e(TAG, "Error recording behavioral view", e)
                                            }
                                        }
                                    }
                                }
                            } finally {
                                capturedBitmap.recycle()
                            }
                        } else {
                            SafeLog.w(TAG, "Screen capture returned null bitmap")
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
                } catch (e: Exception) {
                    SafeLog.e(TAG, "Detection loop error", e)
                    withContext(Dispatchers.Main) {
                        floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                    }
                }
                
                // Throttle to 2-3 fps to avoid battery drain
                delay(400)  // ~2.5 fps
            }
        }
    }
    
    private fun captureScreen(): Bitmap? {
        val reader = imageReader ?: return null
        
        try {
            val image = reader.acquireLatestImage() ?: return null
            
            try {
                return imageToBitmap(image)
            } finally {
                image.close()
            }
        } catch (e: Exception) {
            SafeLog.e(TAG, "Error capturing screen", e)
            return null
        }
    }
    
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * image.width
        
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        
        bitmap.copyPixelsFromBuffer(buffer)
        
        return if (rowPadding == 0) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
        }
    }
    
    private fun cleanupMediaProjection() {
        // CRITICAL: Follow exact cleanup order to avoid RemoteException/DeadObjectException
        // Research: VirtualDisplay → ImageReader → MediaProjection
        
        try {
            virtualDisplay?.release()
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "VirtualDisplay already dead (process terminated)")
        } catch (e: android.os.RemoteException) {
            Log.w(TAG, "RemoteException releasing VirtualDisplay (service died)", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing VirtualDisplay", e)
        }
        virtualDisplay = null
        
        try {
            imageReader?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing ImageReader", e)
        }
        imageReader = null
        
        try {
            mediaProjection?.unregisterCallback(null)
        } catch (e: Exception) {
            // Callback might already be unregistered
        }
        
        try {
            mediaProjection?.stop()
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "MediaProjection already dead (process terminated)")
        } catch (e: android.os.RemoteException) {
            Log.w(TAG, "RemoteException stopping MediaProjection (service died)", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaProjection", e)
        }
        mediaProjection = null
        
        Log.i(TAG, "MediaProjection resources cleaned up")
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
            cleanupMediaProjection()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up MediaProjection on destroy", e)
        }
        
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
