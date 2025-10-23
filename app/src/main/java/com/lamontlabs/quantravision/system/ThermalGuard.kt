package com.lamontlabs.quantravision.system

import android.content.Context
import android.os.Temperature
import android.os.ThermalEventListener
import android.os.ThermalService
import android.widget.Toast

/**
 * ThermalGuard
 * Monitors system temperature and throttles overlay FPS when overheating.
 * Works offline using Android ThermalService (API 29+).
 */
object ThermalGuard {

    private var throttled = false

    fun start(context: Context, onThrottle: (Boolean) -> Unit) {
        try {
            val mgr = context.getSystemService(Context.THERMAL_SERVICE) as android.os.ThermalService?
            if (mgr == null) return
            val listener = object : ThermalEventListener() {
                override fun onThermalEvent(temp: Temperature?) {
                    if (temp == null) return
                    val lvl = temp.value
                    if (lvl > 65) {
                        if (!throttled) {
                            throttled = true
                            onThrottle(true)
                            Toast.makeText(context, "ThermalGuard: throttling detection for safety", Toast.LENGTH_SHORT).show()
                        }
                    } else if (lvl < 55 && throttled) {
                        throttled = false
                        onThrottle(false)
                        Toast.makeText(context, "ThermalGuard: normal operation resumed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            mgr.registerThermalEventListener(listener)
        } catch (_: Exception) { }
    }

    fun isThrottled(): Boolean = throttled
}
