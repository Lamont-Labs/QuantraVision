package com.lamontlabs.quantravision.system

import android.content.Context
import android.os.BatteryManager

/**
 * PowerPolicy
 * Adaptive frame rate and capture interval management based on power state.
 * Deterministic adjustments only; no predictive scaling.
 */
object PowerPolicy {

    fun getPolicy(context: Context): Policy {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val charging = bm.isCharging()
        return when {
            charging -> Policy(maxFps = 20, captureIntervalMs = 50)
            pct > 60 -> Policy(maxFps = 15, captureIntervalMs = 75)
            pct > 30 -> Policy(maxFps = 10, captureIntervalMs = 100)
            else -> Policy(maxFps = 6, captureIntervalMs = 150)
        }
    }

    data class Policy(val maxFps: Int, val captureIntervalMs: Long)
}
