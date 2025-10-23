package com.lamontlabs.quantravision.system

import android.content.Context
import android.os.Build
import android.os.Temperature
import android.os.Temperature.TYPE_CPU
import android.os.Temperature.TYPE_GPU
import android.os.Temperature.TYPE_SKIN
import android.os.ThermalEventListener
import android.os.ThermalService
import android.util.Log
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicInteger

/**
 * ThermalGuard
 * Monitors thermal status and applies deterministic throttling signals.
 * No network, no randomness. Only uses system thermal APIs if available.
 */
object ThermalGuard {

    private val currentStatus = AtomicInteger(0)

    fun start(context: Context, onThrottle: (Int) -> Unit) {
        if (Build.VERSION.SDK_INT < 29) return
        try {
            val serviceClass = Class.forName("android.os.ThermalService")
            val method: Method = serviceClass.getDeclaredMethod("getCurrentTemperatures")
            val service = context.getSystemService(Context.THERMAL_SERVICE) as? ThermalService ?: return
            val listener = object : ThermalEventListener() {
                override fun notifyThrottling(level: Int, temp: Temperature?) {
                    currentStatus.set(level)
                    onThrottle(level)
                }
            }
            service.registerThermalEventListener(listener)
        } catch (e: Exception) {
            Log.e("ThermalGuard", "Thermal monitoring not available: ${e.message}")
        }
    }

    fun status(): Int = currentStatus.get()
}
