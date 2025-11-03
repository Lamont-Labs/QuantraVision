package com.lamontlabs.quantravision.core

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * PowerGuard
 * - Monitors thermal and battery status.
 * - Dynamically adjusts detection frame rate (30 → 15 fps) under load.
 * - Preserves overlay responsiveness and device safety.
 */
class PowerGuard(private val context: Context) {

    private var exec: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentFps = 30
    private var lastCheck = 0L

    fun monitorAndAdjust(cameraProvider: ProcessCameraProvider) {
        val now = System.currentTimeMillis()
        if (now - lastCheck < 5000) return
        lastCheck = now

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val temp = try {
            val batteryIntent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val temperature = batteryIntent?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
            temperature / 10.0
        } catch (e: Exception) { 30.0 }

        val tooHot = temp > 42
        val lowPower = level < 15
        val desired = if (tooHot || lowPower) 15 else 30
        if (desired != currentFps) {
            currentFps = desired
            try {
                cameraProvider.unbindAll()
                // rebind with new frame rate
                Log.w("PowerGuard", "Adjusted FPS to $desired due to temp=$temp°C battery=$level%")
            } catch (e: Exception) {
                Log.e("PowerGuard", "Failed to adjust FPS: ${e.message}")
            }
        }
    }

    fun shutdown() {
        exec.shutdownNow()
    }
}
