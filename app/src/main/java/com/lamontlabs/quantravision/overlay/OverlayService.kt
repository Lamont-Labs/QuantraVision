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
        
        if (ProFeatureGate.isActive(this)) {
            behavioralGuardrails = BehavioralGuardrails(this)
            alertManager = AlertManager.getInstance(this)
        }
        
        floatingMenu = FloatingMenu(this, windowManager) {
            stopSelf()
        }
        
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
        
        val foregroundSuccess = startForegroundService()
        if (!foregroundSuccess) {
            Log.e(TAG, "Failed to start foreground service, stopping service")
            Toast.makeText(
                this,
                "Failed to start overlay service. Please try again.",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }
        
        startPowerPolicyApplicator()
        
        // Get screen dimensions for ImageReader
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
        
        try {
            val readyIntent = Intent(SERVICE_READY_ACTION)
            sendBroadcast(readyIntent)
            Log.i(TAG, "OverlayService ready broadcast sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send ready broadcast", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, -1) ?: -1
        val data = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
        
        if (resultCode == -1 || data == null) {
            Log.w(TAG, "MediaProjection extras not provided - falling back to demo mode")
            startDetectionLoop(useDemoMode = true)
        } else {
            try {
                initializeMediaProjection(resultCode, data)
                startDetectionLoop(useDemoMode = false)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaProjection", e)
                Toast.makeText(
                    this,
                    "Failed to start screen capture: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun initializeMediaProjection(resultCode: Int, data: Intent) {
        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        
        if (projectionManager == null) {
            Log.e(TAG, "MediaProjectionManager not available")
            throw IllegalStateException("MediaProjectionManager not available on this device")
        }
        
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        if (mediaProjection == null) {
            Log.e(TAG, "Failed to create MediaProjection")
            throw IllegalStateException("Failed to create MediaProjection")
        }
        
        // Register callback to handle when user stops sharing via system notification
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
        }, null)
        
        // Create ImageReader with RGBA_8888 format (hardware accelerated)
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2  // maxImages - double buffering
        )
        
        // Create VirtualDisplay that renders to ImageReader surface
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
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
                .build()
            startForeground(1, notification)
            Log.i(TAG, "Foreground service started successfully")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to start foreground service", e)
            return false
        }
    }

    private fun startDetectionLoop(useDemoMode: Boolean = false) {
        scope.launch {
            val detectorBridge = HybridDetectorBridge(applicationContext)
            val legacyDetector = PatternDetector(applicationContext)
            
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
                                            timber.log.Timber.i("DEMO: Processing ${imageFile.name}")
                                            val results = detectorBridge.detectPatternsOptimized(bitmap)
                                            results.forEach { pattern ->
                                                allDetectedPatterns.add(pattern.toPatternMatch())
                                            }
                                        } finally {
                                            bitmap.recycle()
                                        }
                                    }
                                } catch (e: Exception) {
                                    timber.log.Timber.e(e, "Error processing ${imageFile.name}")
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
                                timber.log.Timber.d("Captured screen: ${capturedBitmap.width}x${capturedBitmap.height}")
                                val results = detectorBridge.detectPatternsOptimized(capturedBitmap)
                                timber.log.Timber.d("Detected ${results.size} patterns on live screen")
                                
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
                                                    timber.log.Timber.w("BehavioralGuardrail: ${w.type.name}")
                                                }
                                            } catch (e: Exception) {
                                                timber.log.Timber.e(e, "Error recording behavioral view")
                                            }
                                        }
                                    }
                                }
                            } finally {
                                capturedBitmap.recycle()
                            }
                        } else {
                            timber.log.Timber.w("Screen capture returned null bitmap")
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
                    timber.log.Timber.e(e, "Detection loop error")
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
            timber.log.Timber.e(e, "Error capturing screen")
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
        try {
            virtualDisplay?.release()
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
            mediaProjection?.stop()
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
