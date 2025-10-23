package com.lamontlabs.quantravision.system

import android.content.Context
import android.os.BatteryManager
import android.widget.Toast

/**
 * PowerHint
 * Checks battery state and adjusts capture rate dynamically.
 */
object PowerHint {

    private var lowPower = false

    fun check(context: Context, onAdjust: (Boolean) -> Unit) {
        try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val lvl = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val plugged = bm.isCharging
            if (!plugged && lvl < 15) {
                if (!lowPower) {
                    lowPower = true
                    Toast.makeText(context, "PowerHint: low battery — lowering FPS", Toast.LENGTH_SHORT).show()
                    onAdjust(true)
                }
            } else if (plugged || lvl > 25) {
                if (lowPower) {
                    lowPower = false
                    Toast.makeText(context, "PowerHint: normal power — restoring FPS", Toast.LENGTH_SHORT).show()
                    onAdjust(false)
                }
            }
        } catch (_: Exception) { }
    }

    fun isLowPower() = lowPower
}
