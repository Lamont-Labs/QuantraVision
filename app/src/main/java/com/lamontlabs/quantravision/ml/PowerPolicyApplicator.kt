package com.lamontlabs.quantravision.ml

import android.content.Context
import com.lamontlabs.quantravision.capture.LiveOverlayControllerTunable
import com.lamontlabs.quantravision.ml.optimization.PowerPolicyManager
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * PowerPolicyApplicator - Periodically applies power policy to live detection
 * 
 * Monitors battery/thermal state and adjusts:
 * - LiveOverlayController FPS via tunable
 * - Detection resolution (future)
 * - GPU delegate usage (future)
 * 
 * Usage:
 * ```kotlin
 * val applicator = PowerPolicyApplicator(context)
 * applicator.start(scope)
 * ```
 */
class PowerPolicyApplicator(private val context: Context) {
    
    private val powerManager = PowerPolicyManager(context)
    private var job: Job? = null
    
    /**
     * Start periodically applying power policy
     */
    fun start(scope: CoroutineScope, intervalMs: Long = 5000) {
        stop()
        
        job = scope.launch(Dispatchers.Default) {
            while (isActive) {
                applyPolicy()
                delay(intervalMs)
            }
        }
        
        Timber.i("PowerPolicyApplicator started (interval: ${intervalMs}ms)")
    }
    
    /**
     * Stop applying power policy
     */
    fun stop() {
        job?.cancel()
        job = null
        Timber.i("PowerPolicyApplicator stopped")
    }
    
    /**
     * Apply current power policy to system
     */
    private fun applyPolicy() {
        val policy = powerManager.getOptimalPolicy()
        
        LiveOverlayControllerTunable.setTargetFps(policy.fps)
        
        Timber.v("Applied power policy: ${policy.description} (FPS: ${policy.fps})")
    }
    
    /**
     * Get current policy without applying it
     */
    fun getCurrentPolicy() = powerManager.getOptimalPolicy()
}
