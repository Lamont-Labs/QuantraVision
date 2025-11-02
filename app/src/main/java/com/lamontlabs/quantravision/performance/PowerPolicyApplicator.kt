package com.lamontlabs.quantravision.performance

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import timber.log.Timber

/**
 * Power policy applicator for adaptive performance.
 * Adjusts FPS and detection quality based on battery level and thermal state.
 */
class PowerPolicyApplicator(private val context: Context) {
    
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    
    data class PowerPolicy(
        val targetFps: Int,
        val scaleIterations: Int,
        val enableParallelProcessing: Boolean,
        val reason: String
    )
    
    enum class PowerLevel {
        HIGH, MEDIUM, LOW, CRITICAL
    }
    
    enum class ThermalState {
        NORMAL, MODERATE, HOT, CRITICAL
    }
    
    /**
     * Get current power policy based on device state.
     */
    fun getCurrentPolicy(): PowerPolicy {
        val batteryLevel = getBatteryLevel()
        val powerLevel = determinePowerLevel(batteryLevel)
        val thermalState = getThermalState()
        val isPowerSaveMode = isPowerSaveMode()
        
        return when {
            isPowerSaveMode -> PowerPolicy(
                targetFps = 5,
                scaleIterations = 3,
                enableParallelProcessing = false,
                reason = "Power save mode active"
            )
            thermalState == ThermalState.CRITICAL -> PowerPolicy(
                targetFps = 1,
                scaleIterations = 2,
                enableParallelProcessing = false,
                reason = "Thermal throttling (critical)"
            )
            thermalState == ThermalState.HOT -> PowerPolicy(
                targetFps = 10,
                scaleIterations = 4,
                enableParallelProcessing = false,
                reason = "Thermal throttling (hot)"
            )
            powerLevel == PowerLevel.CRITICAL -> PowerPolicy(
                targetFps = 5,
                scaleIterations = 3,
                enableParallelProcessing = false,
                reason = "Battery critical (<10%)"
            )
            powerLevel == PowerLevel.LOW -> PowerPolicy(
                targetFps = 10,
                scaleIterations = 5,
                enableParallelProcessing = false,
                reason = "Battery low (10-20%)"
            )
            powerLevel == PowerLevel.MEDIUM -> PowerPolicy(
                targetFps = 15,
                scaleIterations = 7,
                enableParallelProcessing = true,
                reason = "Battery medium (20-60%)"
            )
            else -> PowerPolicy(
                targetFps = 30,
                scaleIterations = 10,
                enableParallelProcessing = true,
                reason = "Battery high (>60%)"
            )
        }
    }
    
    /**
     * Get battery level as percentage (0-100).
     */
    fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        
        return batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            
            if (level >= 0 && scale > 0) {
                (level * 100 / scale)
            } else {
                100 // Default to high if unable to determine
            }
        } ?: 100
    }
    
    /**
     * Determine power level from battery percentage.
     */
    private fun determinePowerLevel(batteryPercent: Int): PowerLevel {
        return when {
            batteryPercent < 10 -> PowerLevel.CRITICAL
            batteryPercent < 20 -> PowerLevel.LOW
            batteryPercent < 60 -> PowerLevel.MEDIUM
            else -> PowerLevel.HIGH
        }
    }
    
    /**
     * Get thermal state (approximation for API levels < 29).
     */
    private fun getThermalState(): ThermalState {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                when (powerManager.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE,
                    PowerManager.THERMAL_STATUS_LIGHT -> ThermalState.NORMAL
                    PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.MODERATE
                    PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.HOT
                    PowerManager.THERMAL_STATUS_CRITICAL,
                    PowerManager.THERMAL_STATUS_EMERGENCY,
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalState.CRITICAL
                    else -> ThermalState.NORMAL
                }
            } else {
                // For older API levels, estimate based on battery temperature
                ThermalState.NORMAL
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get thermal state")
            ThermalState.NORMAL
        }
    }
    
    /**
     * Check if power save mode is enabled.
     */
    private fun isPowerSaveMode(): Boolean {
        return powerManager.isPowerSaveMode
    }
    
    /**
     * Calculate frame skip factor based on policy.
     */
    fun getFrameSkipFactor(policy: PowerPolicy): Int {
        return when (policy.targetFps) {
            30 -> 1  // Process every frame
            15 -> 2  // Process every 2nd frame
            10 -> 3  // Process every 3rd frame
            5 -> 6   // Process every 6th frame
            1 -> 30  // Process every 30th frame
            else -> 1
        }
    }
    
    /**
     * Log current power state.
     */
    fun logPowerState() {
        val policy = getCurrentPolicy()
        val batteryLevel = getBatteryLevel()
        
        Timber.i(
            "Power Policy: ${policy.reason}\n" +
            "  Battery: $batteryLevel%\n" +
            "  Target FPS: ${policy.targetFps}\n" +
            "  Scale Iterations: ${policy.scaleIterations}\n" +
            "  Parallel Processing: ${policy.enableParallelProcessing}"
        )
    }
}
