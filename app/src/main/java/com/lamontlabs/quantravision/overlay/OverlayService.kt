package com.lamontlabs.quantravision.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
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
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.ui.EnhancedOverlayView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.TradeScenarioInfo
import com.lamontlabs.quantravision.planner.PatternToPlanEngine
import com.lamontlabs.quantravision.capture.LiveOverlayController
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
    private var liveOverlayController: LiveOverlayController? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionCallback: MediaProjection.Callback? = null
    private var isLiveCaptureActive = false
    private var detectionLoopJob: Job? = null
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
        
        if (ProFeatureGate.isActive(this)) {
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
        startPowerPolicyApplicator()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        if (intent?.action == "ACTION_START_WITH_PROJECTION") {
            val resultCode = intent.getIntExtra("resultCode", -1)
            val data = intent.getParcelableExtra<Intent>("data")
            
            if (resultCode != -1 && data != null) {
                initializeMediaProjection(resultCode, data)
            } else {
                Log.e(TAG, "Invalid MediaProjection data received")
                Toast.makeText(this, "Failed to start screen capture", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        } else {
            startDetectionLoop()
        }
        
        return START_STICKY
    }
    
    private fun initializeMediaProjection(resultCode: Int, data: Intent) {
        scope.launch {
            detectionLoopJob?.cancelAndJoin()
            detectionLoopJob = null
            
            cleanupMediaProjectionResources()
            delay(100)
            
            initializeProjectionInternal(resultCode, data)
        }
    }
    
    private fun initializeProjectionInternal(resultCode: Int, data: Intent) {
        try {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            
            if (mediaProjection == null) {
                Log.e(TAG, "Failed to get MediaProjection")
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
                stopSelf()
                return
            }
            
            val callback = object : MediaProjection.Callback() {
                override fun onStop() {
                    super.onStop()
                    Log.i(TAG, "MediaProjection stopped by system or user")
                    cleanupMediaProjectionResources()
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "Screen capture stopped", Toast.LENGTH_SHORT).show()
                        stopSelf()
                    }
                }
                
                override fun onCapturedContentResize(width: Int, height: Int) {
                    super.onCapturedContentResize(width, height)
                    Log.d(TAG, "Captured content resized to ${width}x${height}")
                }
                
                override fun onCapturedContentVisibilityChanged(isVisible: Boolean) {
                    super.onCapturedContentVisibilityChanged(isVisible)
                    Log.d(TAG, "Captured content visibility changed: $isVisible")
                }
            }
            mediaProjectionCallback = callback
            
            mediaProjection?.registerCallback(callback, null)
            
            startLiveCapture()
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException during MediaProjection initialization", e)
            Toast.makeText(this, "Permission denied. Please grant screen capture permission.", Toast.LENGTH_LONG).show()
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaProjection", e)
            Toast.makeText(this, "Failed to start screen capture: ${e.message}", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }
    
    private fun startLiveCapture() {
        val displayMetrics = resources.displayMetrics
        val width = 720
        val height = 1280
        val densityDpi = displayMetrics.densityDpi
        
        liveOverlayController = LiveOverlayController(
            scope = scope,
            onFrame = { bitmap ->
                processFrame(bitmap)
            },
            targetFps = 12
        )
        
        try {
            mediaProjection?.let { projection ->
                liveOverlayController?.start(projection, width, height, densityDpi)
                isLiveCaptureActive = true
                Log.i(TAG, "Live screen capture started at ${width}x${height}")
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException during virtual display creation", e)
            Toast.makeText(this, "Failed to create virtual display", Toast.LENGTH_SHORT).show()
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start live capture", e)
            Toast.makeText(this, "Failed to start screen capture", Toast.LENGTH_SHORT).show()
            stopSelf()
        }
    }
    
    private fun processFrame(bitmap: android.graphics.Bitmap) {
        scope.launch {
            try {
                val detectorBridge = HybridDetectorBridge(applicationContext)
                val patternToPlanEngine = PatternToPlanEngine(applicationContext)
                val isProActive = ProFeatureGate.isActive(applicationContext)
                
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
                
                val results = detectorBridge.detectPatternsOptimized(bitmap)
                val allDetectedPatterns = mutableListOf<PatternMatch>()
                
                for (pattern in results) {
                    val patternMatch = pattern.toPatternMatch()
                    
                    if (isProActive) {
                        try {
                            val mockPrice = 100.0
                            val scenario = patternToPlanEngine.generateScenario(
                                patternMatch = patternMatch,
                                currentPrice = mockPrice
                            )
                            
                            patternMatch.tradeScenario = TradeScenarioInfo(
                                entryPrice = scenario.entryPrice,
                                stopLoss = scenario.stopLoss,
                                takeProfit = scenario.takeProfit
                            )
                        } catch (e: Exception) {
                            timber.log.Timber.w(e, "Failed to generate trade scenario for ${patternMatch.patternName}")
                        }
                    }
                    
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
                                }
                            } catch (e: Exception) {
                                timber.log.Timber.e(e, "Error recording behavioral view")
                            }
                        }
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
                timber.log.Timber.e(e, "Error processing frame for pattern detection")
            }
        }
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
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            // CRITICAL: Notification creation can fail on custom ROMs (~0.1-0.5%)
            Log.e(TAG, "Failed to start foreground service (custom ROM or notification restrictions)", e)
            // Service will continue to run, just without foreground notification
            // This prevents crashes on incompatible devices
        }
    }

    private fun startDetectionLoop() {
        if (isLiveCaptureActive) {
            Log.i(TAG, "Skipping detection loop - live capture is active")
            return
        }
        
        detectionLoopJob = scope.launch {
            val detectorBridge = HybridDetectorBridge(applicationContext)
            val legacyDetector = PatternDetector(applicationContext)
            val patternToPlanEngine = PatternToPlanEngine(applicationContext)
            val isProActive = ProFeatureGate.isActive(applicationContext)
            
            while (isActive && !isLiveCaptureActive) {
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
                                        
                                        for (pattern in results) {
                                            val patternMatch = pattern.toPatternMatch()
                                            
                                            if (isProActive) {
                                                try {
                                                    val mockPrice = 100.0
                                                    val scenario = patternToPlanEngine.generateScenario(
                                                        patternMatch = patternMatch,
                                                        currentPrice = mockPrice
                                                    )
                                                    
                                                    patternMatch.tradeScenario = TradeScenarioInfo(
                                                        entryPrice = scenario.entryPrice,
                                                        stopLoss = scenario.stopLoss,
                                                        takeProfit = scenario.takeProfit
                                                    )
                                                    
                                                    timber.log.Timber.d("Generated trade scenario for ${patternMatch.patternName}: Entry=${scenario.entryPrice}, Stop=${scenario.stopLoss}, Target=${scenario.takeProfit}")
                                                } catch (e: Exception) {
                                                    timber.log.Timber.w(e, "Failed to generate trade scenario for ${patternMatch.patternName}, continuing without trade info")
                                                }
                                            }
                                            
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
    
    private fun cleanupMediaProjectionResources() {
        isLiveCaptureActive = false
        
        try {
            liveOverlayController?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping live overlay controller", e)
        }
        liveOverlayController = null
        
        try {
            val callback = mediaProjectionCallback
            if (callback != null) {
                mediaProjection?.unregisterCallback(callback)
            }
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "MediaProjection already dead, skipping unregisterCallback()")
        } catch (e: android.os.RemoteException) {
            Log.w(TAG, "RemoteException unregistering callback", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering MediaProjection callback", e)
        }
        mediaProjectionCallback = null
        
        try {
            mediaProjection?.stop()
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "MediaProjection already dead, skipping stop()")
        } catch (e: android.os.RemoteException) {
            Log.w(TAG, "RemoteException stopping MediaProjection", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaProjection", e)
        }
        mediaProjection = null
    }

    override fun onDestroy() {
        runBlocking {
            try {
                detectionLoopJob?.cancelAndJoin()
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling detection loop job", e)
            }
            detectionLoopJob = null
            
            cleanupMediaProjectionResources()
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
