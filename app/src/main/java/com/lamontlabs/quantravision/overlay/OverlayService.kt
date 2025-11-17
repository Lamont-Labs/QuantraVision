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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.ui.EnhancedOverlayView
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.TradeScenarioInfo
import com.lamontlabs.quantravision.planner.PatternToPlanEngine
import kotlinx.coroutines.*
import timber.log.Timber

class OverlayService : Service() {

    companion object {
        private const val TAG = "OverlayService"
        private const val NO_RESULT = Int.MIN_VALUE  // Sentinel value distinct from Activity.RESULT_OK (-1)
        
        // Temporary storage for MediaProjection permission result
        // Must be accessed from main thread only to avoid race conditions
        @Volatile
        private var pendingResultCode: Int = NO_RESULT
        @Volatile
        private var pendingData: Intent? = null
        
        fun setMediaProjectionResult(resultCode: Int, data: Intent) {
            pendingResultCode = resultCode
            pendingData = data
        }
        
        private fun consumeMediaProjectionResult(): Pair<Int, Intent>? {
            val code = pendingResultCode
            val data = pendingData
            // Clear after reading
            pendingResultCode = NO_RESULT
            pendingData = null
            return if (code != NO_RESULT && data != null) Pair(code, data) else null
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var enhancedOverlayView: EnhancedOverlayView? = null
    private var floatingLogo: FloatingLogoButton? = null
    private var floatingMenu: FloatingMenu? = null
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var behavioralGuardrails: BehavioralGuardrails? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionCallback: MediaProjection.Callback? = null
    
    private val stateMachine = OverlayStateMachine()
    private val singleFrameCapture = SingleFrameCapture()
    private lateinit var resultController: PatternResultController

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "=== OverlayService.onCreate() START ===")
        
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
            } else {
                Log.i(TAG, "✓ Overlay permission granted")
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
        Log.i(TAG, "✓ WindowManager initialized")

        resultController = PatternResultController(scope, autoClearTimeoutMs = 10_000L)
        resultController.onResultsCleared = {
            scope.launch(Dispatchers.Main.immediate) {
                enhancedOverlayView?.clearAll()
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                floatingLogo?.updatePatternCount(0)
                stateMachine.transitionToIdle()
            }
        }

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        Log.i(TAG, "Creating full-screen overlay view...")
        try {
            windowManager.addView(view, params)
            overlayView = view
            Log.i(TAG, "✓ Full-screen overlay view added")
            
            enhancedOverlayView = view.findViewById(R.id.overlay_canvas)
            
            if (enhancedOverlayView == null) {
                Log.e(TAG, "CRITICAL: EnhancedOverlayView not found in overlay_layout")
            } else {
                setupEnhancedOverlayTouchHandling()
                Log.i(TAG, "✓ EnhancedOverlayView initialized successfully")
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
        
        if (ProFeatureGate.isActive(this)) {
            behavioralGuardrails = BehavioralGuardrails(this)
            Log.i(TAG, "✓ Behavioral guardrails initialized")
        }
        
        Log.i(TAG, "Creating FloatingMenu...")
        floatingMenu = FloatingMenu(this, windowManager) {
            stopSelf()
        }
        Log.i(TAG, "✓ FloatingMenu created")
        
        Log.i(TAG, "Creating FloatingLogoButton...")
        try {
            floatingLogo = FloatingLogoButton(this, windowManager).apply {
                Log.i(TAG, "Setting up FloatingLogoButton callbacks...")
                onClickListener = {
                    handleTap()
                }
                onLongPressListener = {
                    handleLongPress()
                }
                Log.i(TAG, "Calling FloatingLogoButton.show()...")
                show()
                Log.i(TAG, "✓ FloatingLogoButton.show() completed")
                setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            }
            Log.i(TAG, "✓ FloatingLogoButton fully initialized")
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to create FloatingLogoButton", e)
            Toast.makeText(this, "Failed to create overlay button", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }
        
        Log.i(TAG, "Starting foreground service...")
        startForegroundService()
        Log.i(TAG, "=== OverlayService.onCreate() COMPLETE ===")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "=== OverlayService.onStartCommand() START ===")
        Log.i(TAG, "Intent action: ${intent?.action}")
        
        if (intent?.action == "ACTION_START_WITH_PROJECTION") {
            Log.i(TAG, "Consuming MediaProjection result from companion object...")
            val result = consumeMediaProjectionResult()
            
            if (result != null) {
                val (resultCode, data) = result
                Log.i(TAG, "✓ MediaProjection result found (resultCode=$resultCode)")
                initializeMediaProjection(resultCode, data)
            } else {
                Log.e(TAG, "CRITICAL: MediaProjection data is NULL - permission result not stored")
                Toast.makeText(this, "Failed to start screen capture", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        }
        
        Log.i(TAG, "=== OverlayService.onStartCommand() COMPLETE ===")
        return START_STICKY
    }
    
    private fun setupEnhancedOverlayTouchHandling() {
        // EnhancedOverlayView only handles taps when showing results (for tap-to-clear)
        enhancedOverlayView?.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                if (stateMachine.getCurrentState() is OverlayState.ShowingResult) {
                    Timber.d("EnhancedOverlayView tapped → Clearing results")
                    resultController.manualClear()
                    return@setOnTouchListener true
                }
            }
            // Pass through touches when not showing results
            false
        }
    }
    
    private fun handleTap() {
        Timber.d("Tap detected, current state: ${stateMachine.getCurrentState()}")
        
        when (stateMachine.getCurrentState()) {
            is OverlayState.Idle -> {
                Timber.d("Idle → Triggering capture")
                triggerCapture()
            }
            is OverlayState.ShowingResult -> {
                Timber.d("ShowingResult → Manually clearing highlights")
                resultController.manualClear()
            }
            is OverlayState.Capturing -> {
                Timber.d("Already capturing, ignoring tap")
            }
        }
    }
    
    private fun handleLongPress() {
        Timber.d("Long press detected → Returning to main UI")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
    }
    
    private fun triggerCapture() {
        scope.launch {
            val transitioned = stateMachine.transitionToCapturing()
            if (!transitioned) {
                Timber.w("Failed to transition to Capturing state")
                return@launch
            }
            
            withContext(Dispatchers.Main) {
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
            }
            
            try {
                val projection = mediaProjection
                if (projection == null) {
                    Timber.e("MediaProjection is null, cannot capture")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "Screen capture not initialized",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    stateMachine.transitionToIdle()
                    return@launch
                }
                
                val displayMetrics = resources.displayMetrics
                val width = 720
                val height = 1280
                val densityDpi = displayMetrics.densityDpi
                
                Timber.d("Capturing single frame...")
                val bitmap = singleFrameCapture.captureFrame(projection, width, height, densityDpi)
                
                Timber.d("Frame captured, processing patterns...")
                processFrameForPatterns(bitmap)
                
                bitmap.recycle()
                
            } catch (e: Exception) {
                Timber.e(e, "Error during frame capture")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Capture failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                }
                stateMachine.transitionToIdle()
            }
        }
    }
    
    private suspend fun processFrameForPatterns(bitmap: android.graphics.Bitmap) {
        try {
            val detectorBridge = HybridDetectorBridge(applicationContext)
            val patternToPlanEngine = PatternToPlanEngine(applicationContext)
            val isProActive = ProFeatureGate.isActive(applicationContext)
            
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
                    } else {
                        floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.PATTERNS_FOUND)
                    }
                } else {
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                }
            }
            
            if (allDetectedPatterns.isNotEmpty()) {
                stateMachine.transitionToShowingResult(results)
                resultController.showResults(results)
            } else {
                stateMachine.transitionToIdle()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error processing frame for pattern detection")
            withContext(Dispatchers.Main) {
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            }
            stateMachine.transitionToIdle()
        }
    }
    
    private fun initializeMediaProjection(resultCode: Int, data: Intent) {
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
            
            Log.i(TAG, "MediaProjection initialized successfully - ready for tap-to-scan")
            
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

    private fun cleanupMediaProjectionResources() {
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
            cleanupMediaProjectionResources()
        }
        
        try {
            resultController.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up result controller", e)
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
