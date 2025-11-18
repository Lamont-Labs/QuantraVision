package com.lamontlabs.quantravision.overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.ml.HybridDetectorBridge
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.licensing.ProFeatureGate
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
    private lateinit var patternNotificationManager: PatternNotificationManager
    private var floatingLogo: FloatingLogoButton? = null
    private var scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var behavioralGuardrails: BehavioralGuardrails? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionCallback: MediaProjection.Callback? = null
    
    // Android 14 fix: Create VirtualDisplay ONCE and reuse for all scans
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var imageReader: android.media.ImageReader? = null
    
    // Track current scan job for cancellation
    private var currentScanJob: kotlinx.coroutines.Job? = null
    
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

        patternNotificationManager = PatternNotificationManager(this)
        Log.i(TAG, "✓ PatternNotificationManager initialized")

        resultController = PatternResultController(scope, autoClearTimeoutMs = 10_000L)
        resultController.onResultsCleared = {
            scope.launch(Dispatchers.Main.immediate) {
                patternNotificationManager.dismiss()
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                floatingLogo?.updatePatternCount(0)
                stateMachine.transitionToIdle()
            }
        }
        
        if (ProFeatureGate.isActive(this)) {
            behavioralGuardrails = BehavioralGuardrails(this)
            Log.i(TAG, "✓ Behavioral guardrails initialized")
        }
        
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
    
    
    private fun handleTap() {
        Log.i(TAG, "🎯 TAP DETECTED in OverlayService.handleTap()")
        val currentState = stateMachine.getCurrentState()
        Log.i(TAG, "Current state: $currentState")
        
        when (currentState) {
            is OverlayState.Idle -> {
                Log.i(TAG, "✅ Idle state → Triggering capture")
                triggerCapture()
            }
            is OverlayState.ShowingResult -> {
                Log.i(TAG, "📋 ShowingResult state → Clearing highlights")
                resultController.manualClear()
            }
            is OverlayState.Capturing -> {
                Log.i(TAG, "🛑 Capturing state → Canceling scan")
                cancelScan()
            }
        }
    }
    
    private fun handleLongPress() {
        Log.i(TAG, "🔴 LONG PRESS DETECTED in OverlayService.handleLongPress()")
        Log.i(TAG, "Returning to main UI...")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        Log.i(TAG, "✅ MainActivity started")
    }
    
    private fun cancelScan() {
        Log.i(TAG, "Canceling current scan...")
        currentScanJob?.cancel()
        currentScanJob = null
        
        scope.launch(Dispatchers.Main) {
            floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            Toast.makeText(
                applicationContext,
                "Scan canceled",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        stateMachine.transitionToIdle()
        Log.i(TAG, "✓ Scan canceled successfully")
    }
    
    private fun triggerCapture() {
        currentScanJob = scope.launch {
            val transitioned = stateMachine.transitionToCapturing()
            if (!transitioned) {
                Timber.w("Failed to transition to Capturing state")
                return@launch
            }
            
            withContext(Dispatchers.Main) {
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
            }
            
            try {
                val reader = imageReader
                if (reader == null) {
                    Timber.e("ImageReader is null, cannot capture")
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
                
                Timber.d("Capturing single frame from persistent VirtualDisplay...")
                val bitmap = singleFrameCapture.captureFrame(reader)
                
                Timber.d("Frame captured, processing patterns...")
                processFrameForPatterns(bitmap)
                
                bitmap.recycle()
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Scan was canceled by user - already showed toast in cancelScan()
                Timber.d("Scan canceled by user")
                throw e // Re-throw to properly cancel coroutine
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
            } finally {
                currentScanJob = null
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
                // Always call showPatterns - it handles empty list by showing "no patterns" notification
                patternNotificationManager.showPatterns(allDetectedPatterns)
                
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
            
            // Android 14 fix: Create VirtualDisplay ONCE here, reuse for all scans
            createPersistentVirtualDisplay()
            
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
    
    private fun createPersistentVirtualDisplay() {
        try {
            val displayMetrics = resources.displayMetrics
            val width = 720
            val height = 1280
            val densityDpi = displayMetrics.densityDpi
            
            // Create ImageReader that will be reused for all frame captures
            imageReader = android.media.ImageReader.newInstance(
                width,
                height,
                android.graphics.PixelFormat.RGBA_8888,
                2
            )
            
            // Create VirtualDisplay ONCE - Android 14 requirement
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "QuantraVision_Persistent",
                width,
                height,
                densityDpi,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null
            )
            
            if (virtualDisplay == null) {
                Log.e(TAG, "Failed to create VirtualDisplay")
                Toast.makeText(this, "Failed to create screen capture display", Toast.LENGTH_SHORT).show()
                stopSelf()
            } else {
                Log.i(TAG, "✓ VirtualDisplay created successfully - will be reused for all scans")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create VirtualDisplay", e)
            Toast.makeText(this, "Failed to setup screen capture: ${e.message}", Toast.LENGTH_SHORT).show()
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
        // Clean up VirtualDisplay and ImageReader first
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            Log.d(TAG, "VirtualDisplay released")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing VirtualDisplay", e)
        }
        
        try {
            imageReader?.close()
            imageReader = null
            Log.d(TAG, "ImageReader closed")
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ImageReader", e)
        }
        
        // Then clean up MediaProjection
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
        try {
            patternNotificationManager.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up pattern notification manager", e)
        }
        
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
            scope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling coroutine scope on destroy", e)
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
