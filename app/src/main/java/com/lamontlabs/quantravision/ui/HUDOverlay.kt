package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.lamontlabs.quantravision.system.PowerPolicy
import com.lamontlabs.quantravision.system.ThermalGuard

/**
 * HUDOverlay
 * Minimal persistent floating heads-up display for live metrics.
 * Shows FPS, temperature level, and power policy.
 */
class HUDOverlay(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 22f
        isAntiAlias = true
    }

    private var fps: Int = 0
    private var tempLevel: Int = 0
    private var policy: PowerPolicy.Policy = PowerPolicy.Policy(15, 75)

    fun updateMetrics(fpsVal: Int, temp: Int, policyVal: PowerPolicy.Policy) {
        fps = fpsVal
        tempLevel = temp
        policy = policyVal
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val text =
            "FPS:$fps  | TempLvl:$tempLevel  | MaxFPS:${policy.maxFps}  | Δ:${policy.captureIntervalMs}ms"
        paint.color = if (tempLevel > 2) Color.YELLOW else Color.WHITE
        canvas.drawText(text, 20f, 40f, paint)
    }
}
