package com.lamontlabs.quantravision.overlay

import android.util.Log

object ScanThrottler {
    private const val TAG = "ScanThrottler"
    
    private const val MIN_FRAME_INTERVAL_MS = 250L
    private const val MAX_FRAME_INTERVAL_MS = 500L
    private const val TARGET_FRAME_INTERVAL_MS = 333L
    
    private var lastScanTimestamp = 0L
    private var frameCount = 0
    private var startTime = System.currentTimeMillis()
    
    fun shouldScan(): Boolean {
        val now = System.currentTimeMillis()
        val elapsed = now - lastScanTimestamp
        
        return if (elapsed >= TARGET_FRAME_INTERVAL_MS) {
            lastScanTimestamp = now
            frameCount++
            logFrameRate(now)
            true
        } else {
            Log.v(TAG, "Scan throttled: ${elapsed}ms < ${TARGET_FRAME_INTERVAL_MS}ms")
            false
        }
    }
    
    fun getCurrentFPS(): Double {
        val elapsed = System.currentTimeMillis() - startTime
        return if (elapsed > 0) {
            (frameCount * 1000.0) / elapsed
        } else {
            0.0
        }
    }
    
    fun reset() {
        lastScanTimestamp = 0L
        frameCount = 0
        startTime = System.currentTimeMillis()
        Log.d(TAG, "Throttler reset")
    }
    
    private fun logFrameRate(now: Long) {
        if (frameCount % 10 == 0) {
            val fps = getCurrentFPS()
            Log.d(TAG, "Frame rate: %.2f FPS (target: 2-4 FPS)".format(fps))
            
            if (fps > 4.5) {
                Log.w(TAG, "Frame rate exceeds 4 FPS target, consider throttling")
            } else if (fps < 1.5) {
                Log.w(TAG, "Frame rate below 2 FPS target, performance issue?")
            }
        }
    }
}
