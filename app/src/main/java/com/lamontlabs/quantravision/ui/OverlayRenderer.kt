package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.PatternMatch

/**
 * OverlayRenderer
 * Renders pattern highlights and persistent disclaimer watermark.
 */
class OverlayRenderer(
    context: Context,
    private val matches: List<PatternMatch>
) : View(context) {

    private val paint = Paint().apply {
        color = Color.argb(150, 0, 255, 0)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw pattern highlights
        matches.forEach { m ->
            val rect = m.boundingBox
            canvas.drawRect(rect, paint)
            canvas.drawText(
                "${m.patternName} (${(m.confidence * 100).toInt()}%)",
                rect.left.toFloat(),
                rect.top - 8f,
                textPaint
            )
        }

        // Draw fixed disclaimer watermark
        val msg = "⚠ Illustrative Only — Not Financial Advice"
        textPaint.color = Color.argb(190, 255, 255, 255)
        textPaint.textSize = 22f
        canvas.drawText(msg, 24f, height - 32f, textPaint)
    }
}
