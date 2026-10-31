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
import com.lamontlabs.quantravision.detection.VisualTemplateScanner
import com.lamontlabs.quantravision.detection.DetectionResult
import com.lamontlabs.quantravision.psychology.BehavioralGuardrails
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.pipeline.QuantraPipelineCoordinator
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
    private val scanSession = SingleScanSession(scope)
    
    // Track if MediaProjection is fully initialized and ready
    @Volatile
    private var isMediaProjectionReady: Boolean = false
    
    // Overlay-only mode: When true, only the floating Q is visible (chart overlay mode)
    @Volatile
    private var overlayOnlyMode: Boolean = false
    
    // CRITICAL: Reuse detector bridge to avoid repeated template loading
    // Templates are cached inside detector, loading once saves ~200ms and memory on each scan
    private var patternDetector: VisualTemplateScanner? = null
    
    // v2.0 Pipeline Coordinator for enhanced scan flow
    private var pipelineCoordinator: QuantraPipelineCoordinator? = null
    
    // Cache template count to avoid repeated verification
    private var templateCount: Int = 0
    
    private val stateMachine = OverlayStateMachine()
    private val singleFrameCapture = SingleFrameCapture()
    private lateinit var resultController: PatternResultController

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "=== OverlayService.onCreate() START ===")
        startForegroundService()
        
        // CRITICAL: Defense-in-depth - Check if service should be disabled
        // This should NEVER happen (OverlayServiceGuard disables component before import)
        // But if it does, we fail gracefully instead of crashing
        if (!com.lamontlabs.quantravision.overlay.OverlayServiceGuard.isEnabled(this)) {
            Log.e(TAG, "CRITICAL: Service started while disabled! This should never happen.")
            android.widget.Toast.makeText(
                this,
                "Scanner is temporarily disabled during model import",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            stopSelf()
            return
        }
        
        if (!com.lamontlabs.quantravision.QuantraVisionApp.openCVInitialized) {
            Toast.makeText(this, "Chart scanner unavailable: OpenCV could not initialize.", Toast.LENGTH_LONG).show()
            stopForeground(true)
            stopSelf()
            return
        }

        // Initialize v2.0 Pipeline Coordinator
        pipelineCoordinator = QuantraPipelineCoordinator(this, detectPatterns = { bitmap ->
            val detector = patternDetector ?: error("Pattern templates are not ready")
            detector.scan(bitmap)
        })
        Log.i(TAG, "✓ Pipeline coordinator initialized")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.e(TAG, "CRITICAL: SYSTEM_ALERT_WINDOW permission not granted, stopping service")
                Toast.makeText(
                    this,
                    "Overlay permission required. Tap notification to open settings.",
                    Toast.LENGTH_LONG
                ).show()
                
                try {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:$packageName")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(settingsIntent)
                    Log.i(TAG, "Opened overlay permission settings for user")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open overlay settings: ${e.message}", e)
                    Toast.makeText(
                        this,
                        "Please enable 'Draw over other apps' in Settings > Apps > QuantraVision",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                stopForeground(true)
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
            // Properly stop foreground service to avoid crash
            stopForeground(true)
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
                onDoubleTapListener = {
                    handleDoubleTap()
                }
                onLongPressListener = {
                    handleLongPress()
                }
                Log.i(TAG, "Calling FloatingLogoButton.show()...")
                show()
                Log.i(TAG, "✓ FloatingLogoButton.show() completed")
                // Start with disabled status until MediaProjection is ready
                setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                setEnabled(false)  // Disable until MediaProjection is initialized
            }
            Log.i(TAG, "✓ FloatingLogoButton fully initialized (disabled until MediaProjection ready)")
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Failed to create FloatingLogoButton", e)
            Toast.makeText(this, "Failed to create overlay button", Toast.LENGTH_LONG).show()
            // Properly stop foreground service to avoid crash
            stopForeground(true)
            stopSelf()
            return
        }
        
        // Note: startForegroundService() already called at beginning of onCreate()
        Log.i(TAG, "=== OverlayService.onCreate() COMPLETE ===")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.i(TAG, "=== OverlayService.onStartCommand() START ===")
        
        // Double-check model state in case service was restarted
        val modelManager = com.lamontlabs.quantravision.intelligence.llm.ModelManager(this)
        val modelState = modelManager.getModelState()
        if (modelState != com.lamontlabs.quantravision.intelligence.llm.ModelState.Downloaded) {
            Log.w(TAG, "AI model not imported (state=$modelState) in onStartCommand, stopping service")
            // Properly stop foreground service to avoid crash
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY // Don't restart service
        }
        
        Log.i(TAG, "Intent action: ${intent?.action}")
        Log.i(TAG, "Current state - MediaProjection: ${if (mediaProjection != null) "✓ EXISTS" else "❌ NULL"}, ImageReader: ${if (imageReader != null) "✓ EXISTS" else "❌ NULL"}")
        
        if (intent?.action == "ACTION_START_WITH_PROJECTION") {
            // Only initialize if MediaProjection doesn't already exist
            if (mediaProjection != null) {
                Log.w(TAG, "MediaProjection already initialized, skipping re-initialization")
                return START_STICKY
            }
            
            Log.i(TAG, "Consuming MediaProjection result from companion object...")
            val result = consumeMediaProjectionResult()
            
            if (result != null) {
                val (resultCode, data) = result
                Log.i(TAG, "✓ MediaProjection result found (resultCode=$resultCode)")
                initializeMediaProjection(resultCode, data)
            } else {
                Log.e(TAG, "CRITICAL: MediaProjection data is NULL - permission result not stored or already consumed")
                Toast.makeText(this, "Failed to start screen capture. Please restart scanner.", Toast.LENGTH_SHORT).show()
                stopForeground(true)
                stopSelf()
            }
        } else if (intent == null) {
            Log.w(TAG, "Service restarted by system (START_STICKY) but no MediaProjection data available")
            Log.w(TAG, "Current MediaProjection state: ${if (mediaProjection != null) "exists (will continue)" else "null (stopping service)"}")
            if (mediaProjection == null) {
                Toast.makeText(this, "Screen capture stopped. Please restart scanner.", Toast.LENGTH_SHORT).show()
                stopForeground(true)
                stopSelf()
            }
        }
        
        Log.i(TAG, "=== OverlayService.onStartCommand() COMPLETE ===")
        return START_STICKY
    }
    
    
    private fun handleTap() {
        Log.i(TAG, "🎯 TAP DETECTED in OverlayService.handleTap()")
        
        // Check if MediaProjection is ready
        if (!isMediaProjectionReady) {
            Log.w(TAG, "❌ MediaProjection not ready yet, ignoring tap")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(
                    applicationContext,
                    "Initializing screen capture... Please wait.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            return
        }
        
        val currentState = stateMachine.getCurrentState()
        Log.i(TAG, "Current state: $currentState")
        
        when (currentState) {
            is OverlayState.Idle -> {
                if (ScanThrottler.shouldScan()) {
                    Log.i(TAG, "✅ Idle state → Triggering capture (throttle passed)")
                    triggerCapture()
                } else {
                    Log.i(TAG, "⏱️ Scan throttled - too soon after last scan")
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "Please wait before scanning again...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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
    
    private fun handleDoubleTap() {
        Log.i(TAG, "👆👆 DOUBLE TAP DETECTED in OverlayService.handleDoubleTap()")
        overlayOnlyMode = !overlayOnlyMode
        Log.i(TAG, "Overlay-only mode toggled to: $overlayOnlyMode")
        
        scope.launch(Dispatchers.Main) {
            if (overlayOnlyMode) {
                // Enter overlay-only mode - show toast feedback
                Toast.makeText(
                    applicationContext,
                    "📊 Overlay mode ON - Double-tap to exit, long-press for full UI",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(TAG, "✅ Entered overlay-only mode")
            } else {
                // Exit overlay-only mode
                Toast.makeText(
                    applicationContext,
                    "🔙 Overlay mode OFF - Normal mode restored",
                    Toast.LENGTH_SHORT
                ).show()
                Log.i(TAG, "✅ Exited overlay-only mode")
            }
        }
    }
    
    private fun handleLongPress() {
        Log.i(TAG, "🔴 LONG PRESS DETECTED in OverlayService.handleLongPress()")
        
        // Long press always exits overlay-only mode and returns to main UI
        if (overlayOnlyMode) {
            overlayOnlyMode = false
            Log.i(TAG, "Force-exiting overlay-only mode via long press")
        }
        
        Log.i(TAG, "Returning to main UI...")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }
        startActivity(intent)
        Log.i(TAG, "✅ MainActivity started")
    }
    
    private fun cancelScan() {
        Log.i(TAG, "Canceling current scan...")
        scanSession.cancel()
        
        scope.launch(Dispatchers.Main) {
            floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            Toast.makeText(
                applicationContext,
                "Scan canceled",
                Toast.LENGTH_SHORT
            ).show()
            stateMachine.transitionToIdle()
        }
        
        Log.i(TAG, "✓ Scan canceled successfully")
    }
    
    private fun triggerCapture() {
        scanSession.start {
            var capturedBitmap: android.graphics.Bitmap? = null
            val scanStartTime = System.currentTimeMillis()
            
            val transitioned = stateMachine.transitionToCapturing()
            if (!transitioned) {
                Timber.w("Failed to transition to Capturing state")
                return@start
            }
            
            withContext(Dispatchers.Main) {
                floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
                Toast.makeText(
                    applicationContext,
                    "🔍 Scanning chart...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            try {
                val reader = imageReader
                if (reader == null) {
                    Timber.e("❌ CRITICAL: ImageReader is null, cannot capture")
                    Timber.e("MediaProjection state: ${if (mediaProjection != null) "exists" else "null"}")
                    Timber.e("VirtualDisplay state: ${if (virtualDisplay != null) "exists" else "null"}")
                    Timber.e("This indicates MediaProjection was not initialized properly")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "❌ Screen capture not initialized. Please restart scanner.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    stateMachine.transitionToIdle()
                    return@start
                }
                
                Timber.i("📸 Starting frame capture from VirtualDisplay...")
                val captureStartTime = System.currentTimeMillis()
                
                val bitmap = try {
                    singleFrameCapture.captureFrame(reader)
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Timber.e("❌ TIMEOUT: No frame received from VirtualDisplay after 2.5s")
                    Timber.e("This is a Samsung One UI quirk - VirtualDisplay not delivering frames")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "❌ Screen capture timed out. Please restart scanner.\n(Samsung One UI bug - try restarting phone if this persists)",
                            Toast.LENGTH_LONG
                        ).show()
                        floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                    }
                    stateMachine.transitionToIdle()
                    return@start
                }
                capturedBitmap = bitmap
                
                val captureTime = System.currentTimeMillis() - captureStartTime
                Timber.i("✅ Frame captured in ${captureTime}ms - size: ${bitmap.width}x${bitmap.height}")
                
                // Update UI: capture complete, starting detection
                withContext(Dispatchers.Main) {
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.SCANNING)
                }
                
                Timber.i("🔬 Starting pattern detection (5s timeout)...")
                val detectionStartTime = System.currentTimeMillis()
                
                try {
                    // CRITICAL: Separate timeout for detection phase (5s)
                    // Frame capture has 2.5s timeout, detection needs more time for 109 patterns
                    withTimeout(5000L) {
                        processFrameForPatterns(bitmap)
                    }
                } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                    Timber.e("❌ TIMEOUT: Pattern detection took longer than 5s")
                    Timber.e("This may indicate a performance issue or template loading problem")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            applicationContext,
                            "❌ Pattern detection timed out after 5 seconds.\nPlease try again or restart the scanner.",
                            Toast.LENGTH_LONG
                        ).show()
                        floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                    }
                    stateMachine.transitionToIdle()
                    return@start
                }
                
                val detectionTime = System.currentTimeMillis() - detectionStartTime
                
                val totalTime = System.currentTimeMillis() - scanStartTime
                Timber.i("✅ Scan complete in ${totalTime}ms (capture: ${captureTime}ms, detection: ${detectionTime}ms)")
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Scan was canceled by user - already showed toast in cancelScan()
                Timber.d("Scan canceled by user")
                throw e // Re-throw to properly cancel coroutine
            } catch (e: Exception) {
                Timber.e(e, "❌ Error during scan")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "❌ Scan failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
                }
                stateMachine.transitionToIdle()
            } finally {
                capturedBitmap?.let { if (!it.isRecycled) it.recycle() }
            }
        }
    }
    
    private suspend fun processFrameForPatterns(bitmap: android.graphics.Bitmap) {
        try {
            val coordinator = pipelineCoordinator ?: error("Scanner is not ready")
            when (val result = coordinator.executePipeline(bitmap)) {
                is QuantraPipelineCoordinator.PipelineResult.FailClosed -> {
                    Timber.w("Scan rejected at ${result.stage}: ${result.reason} (${result.proofHash})")
                    clearRejectedScan(result.reason)
                }
                is QuantraPipelineCoordinator.PipelineResult.QuotaExhausted ->
                    clearRejectedScan("Scan allowance reached or quota storage unavailable")
                is QuantraPipelineCoordinator.PipelineResult.Success -> {
                    val results = result.patterns
                    // Persist only measurements that passed the authoritative scan path.
                    try {
                        val dao = com.lamontlabs.quantravision.PatternDatabase.getInstance(applicationContext).patternDao()
                        results.forEach { dao.insert(it.toPatternMatch()) }
                    } catch (error: Exception) { Timber.w(error, "Could not save scan history") }
                    withContext(Dispatchers.Main) {
                        patternNotificationManager.showPatterns(results.map { it.toPatternMatch() })
                        floatingLogo?.updatePatternCount(results.size)
                        floatingLogo?.setDetectionStatus(if (results.isEmpty()) LogoBadge.DetectionStatus.IDLE
                            else LogoBadge.DetectionStatus.PATTERNS_FOUND)
                        Toast.makeText(applicationContext, if (results.isEmpty()) "No visual matches found"
                            else "${results.size} visual matches · price levels unavailable", Toast.LENGTH_SHORT).show()
                    }
                    if (results.isEmpty()) {
                        resultController.manualClear()
                        stateMachine.transitionToIdle()
                    } else {
                        stateMachine.transitionToShowingResult(results)
                        resultController.showResults(results)
                    }
                }
            }
        } catch (cancel: CancellationException) {
            clearRejectedScan("Scan canceled")
            throw cancel
        } catch (error: Exception) {
            Timber.e(error, "Scan failed")
            clearRejectedScan("Scan could not complete. Please try again.")
        }
    }

    private suspend fun clearRejectedScan(message: String) = withContext(NonCancellable) {
        withContext(Dispatchers.Main) {
            resultController.manualClear()
            patternNotificationManager.dismiss()
            floatingLogo?.updatePatternCount(0)
            floatingLogo?.setDetectionStatus(LogoBadge.DetectionStatus.IDLE)
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
        stateMachine.transitionToIdle()
    }

    private fun initializeMediaProjection(resultCode: Int, data: Intent) {
        try {
            val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            
            if (mediaProjection == null) {
                Log.e(TAG, "Failed to get MediaProjection - user denied permission or system error")
                Toast.makeText(
                    this, 
                    "❌ Screen capture permission denied.\n\nTo use the scanner:\n1. Tap the floating button again\n2. Grant screen capture permission when prompted\n\nNote: This is required for chart pattern detection.",
                    Toast.LENGTH_LONG
                ).show()
                stopForeground(true)
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
                        stopForeground(true)
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
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaProjection", e)
            Toast.makeText(this, "Failed to start screen capture: ${e.message}", Toast.LENGTH_SHORT).show()
            stopForeground(true)
            stopSelf()
        }
    }
    
    private fun createPersistentVirtualDisplay() {
        try {
            val displayMetrics = resources.displayMetrics
            val scale = (1280f / maxOf(displayMetrics.widthPixels, displayMetrics.heightPixels)).coerceAtMost(1f)
            val width = (displayMetrics.widthPixels * scale).toInt().coerceAtLeast(2)
            val height = (displayMetrics.heightPixels * scale).toInt().coerceAtLeast(2)
            val densityDpi = displayMetrics.densityDpi
            
            // Create ImageReader that will be reused for all frame captures
            imageReader = try {
                android.media.ImageReader.newInstance(
                    width,
                    height,
                    android.graphics.PixelFormat.RGBA_8888,
                    2
                )
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "❌ Invalid ImageReader parameters: ${width}x${height}", e)
                throw RuntimeException("Invalid screen capture dimensions", e)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to create ImageReader", e)
                throw RuntimeException("Failed to create ImageReader", e)
            }
            
            // Create VirtualDisplay ONCE - Android 14 requirement
            virtualDisplay = try {
                mediaProjection?.createVirtualDisplay(
                    "QuantraVision_Persistent",
                    width,
                    height,
                    densityDpi,
                    android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface,
                    null,
                    null
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "❌ SecurityException creating VirtualDisplay - permission denied", e)
                throw RuntimeException("Screen capture permission denied", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "❌ IllegalStateException - MediaProjection stopped or in invalid state", e)
                throw RuntimeException("MediaProjection stopped", e)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to create VirtualDisplay", e)
                throw RuntimeException("Failed to create VirtualDisplay", e)
            }
            
            if (virtualDisplay == null) {
                Log.e(TAG, "❌ Failed to create VirtualDisplay")
                Log.e(TAG, "MediaProjection state: ${if (mediaProjection != null) "exists" else "null"}")
                Log.e(TAG, "ImageReader state: ${if (imageReader != null) "exists" else "null"}")
                isMediaProjectionReady = false
                Toast.makeText(this, "Failed to create screen capture display", Toast.LENGTH_SHORT).show()
                stopForeground(true)
                stopSelf()
            } else {
                Log.i(TAG, "✅ VirtualDisplay created successfully - will be reused for all scans")
                Log.i(TAG, "✅ ImageReader ready: ${imageReader != null}")
                
                // Mark as ready and enable the floating button
                isMediaProjectionReady = true
                
                // CRITICAL: Initialize detector bridge ONCE and verify templates loaded
                // This caches templates for all future scans, avoiding repeated loading
                scope.launch {
                    try {
                        Timber.i("🔍 Initializing pattern detector and loading templates...")
                        patternDetector = VisualTemplateScanner(applicationContext)
                        templateCount = patternDetector?.getTemplateCount() ?: 0
                        
                        if (templateCount == 0) {
                            throw Exception("No templates loaded - pattern detection will not work")
                        }
                        
                        Timber.i("✅ Pattern detector ready: $templateCount templates loaded and cached")
                        
                        withContext(Dispatchers.Main) {
                            floatingLogo?.setEnabled(true)
                            Toast.makeText(
                                applicationContext,
                                "✓ Ready! Loaded $templateCount patterns. Tap Q to scan charts.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ CRITICAL: Failed to initialize pattern detector")
                        patternDetector = null
                        templateCount = 0
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                applicationContext,
                                "❌ Pattern detector failed to initialize.\nPlease restart the scanner.\n\nError: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            floatingLogo?.setEnabled(false)
                        }
                        isMediaProjectionReady = false
                        // Stop service on initialization failure - forces user to restart
                        stopForeground(true)
                        stopSelf()
                    }
                }
                Log.i(TAG, "✅ MediaProjection fully initialized and ready for scans")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create VirtualDisplay", e)
            isMediaProjectionReady = false
            Toast.makeText(this, "Failed to setup screen capture: ${e.message}", Toast.LENGTH_SHORT).show()
            stopForeground(true)
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
        Log.i(TAG, "Cleaning up MediaProjection resources...")
        
        // Mark as not ready during cleanup
        isMediaProjectionReady = false
        try {
            scope.launch(Dispatchers.Main) {
                floatingLogo?.setEnabled(false)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error disabling floating logo during cleanup", e)
        }
        
        // Reset scan throttler
        try {
            ScanThrottler.reset()
            Log.d(TAG, "ScanThrottler reset")
        } catch (e: Exception) {
            Log.w(TAG, "Error resetting ScanThrottler", e)
        }
        
        // Release detector bridge (frees cached templates)
        try {
            patternDetector = null
            templateCount = 0
            Log.d(TAG, "Detector bridge released")
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing detector bridge", e)
        }
        
        // Clean up VirtualDisplay and ImageReader first
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            Log.d(TAG, "VirtualDisplay released")
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "VirtualDisplay already dead")
            virtualDisplay = null
        } catch (e: IllegalStateException) {
            Log.w(TAG, "VirtualDisplay in illegal state during release")
            virtualDisplay = null
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing VirtualDisplay", e)
            virtualDisplay = null
        }
        
        try {
            imageReader?.close()
            imageReader = null
            Log.d(TAG, "ImageReader closed")
        } catch (e: Exception) {
            Log.w(TAG, "Error closing ImageReader", e)
            imageReader = null
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
            Log.d(TAG, "MediaProjection stopped")
        } catch (e: android.os.DeadObjectException) {
            Log.w(TAG, "MediaProjection already dead, skipping stop()")
        } catch (e: android.os.RemoteException) {
            Log.w(TAG, "RemoteException stopping MediaProjection", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaProjection", e)
        }
        mediaProjection = null
        
        Log.i(TAG, "MediaProjection cleanup complete")
    }

    override fun onDestroy() {
        val scanner = patternDetector
        val coordinator = pipelineCoordinator
        scanSession.close {
            try { scanner?.close() } finally { coordinator?.release() }
        }
        pipelineCoordinator = null
        try {
            patternNotificationManager.cleanup()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up pattern notification manager", e)
        }
        
        runBlocking {
            cleanupMediaProjectionResources()
        }
        
        // Cleanup v2.0 Pipeline Coordinator
        pipelineCoordinator = null
        
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
