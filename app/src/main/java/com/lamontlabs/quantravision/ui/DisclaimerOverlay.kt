package com.lamontlabs.quantravision.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

/**
 * DisclaimerOverlay
 * Draws persistent “Illustrative Only” watermark on all active overlays.
 * Always visible.  Semi-transparent.  Deterministic placement.
 */
class DisclaimerOverlay : View(context) {

    private val paint = Paint().apply {
        color = Color.argb(180, 255, 255, 255)
        textSize = 28f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val msg = "⚠ Illustrative Only — Not Financial Advice"
        val x = 24f
        val y = height - 48f
        canvas.drawText(msg, x, y, paint)
    }
}
