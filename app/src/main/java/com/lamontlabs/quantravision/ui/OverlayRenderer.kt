package com.lamontlabs.quantravision.ui

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.boundingBox
import com.lamontlabs.quantravision.detection.HighlightGate

/**
 * OverlayRenderer (gated)
 * Renders matches but enforces free highlight quota for non-Pro users.
 */
class OverlayRenderer(
    private val appContext: Context,
    matches: List<PatternMatch>
) : View(appContext) {

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

    private val gated = HighlightGate.filterForRender(appContext, matches)
    var quotaExceeded: Boolean = gated.isEmpty() && matches.isNotEmpty()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw allowed highlights; count consumption per rendered detection
        gated.forEach { m ->
            if (!HighlightGate.allowAndCount(appContext)) {
                quotaExceeded = true
                return@forEach
            }
            val rect = m.boundingBox
            canvas.drawRect(rect, paint)
            canvas.drawText("${m.patternName} (${(m.confidence * 100).toInt()}%)",
                rect.left.toFloat(), rect.top - 8f, textPaint)
        }

        // Watermark disclaimer
        val msg = "⚠ Illustrative Only — Not Financial Advice"
        textPaint.color = Color.argb(190, 255, 255, 255)
        textPaint.textSize = 22f
        canvas.drawText(msg, 24f, height - 32f, textPaint)

        // If quota exceeded, dim overlay and prompt host to show UpgradePrompt
        if (quotaExceeded) {
            canvas.drawColor(Color.argb(120, 0, 0, 0))
            textPaint.textSize = 20f
            canvas.drawText("Free highlights used. Upgrade to continue.", 24f, height / 2f, textPaint)
        }
    }
}
