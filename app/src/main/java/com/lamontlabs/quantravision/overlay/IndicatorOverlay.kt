package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.*
import android.view.View
import com.lamontlabs.quantravision.analysis.IndicatorHit
import com.lamontlabs.quantravision.analysis.IndicatorType

/**
 * Renders lightweight badges for detected indicators with minimal obstruction.
 * Call setIndicators(...) each frame after IndicatorDetector.analyze().
 */
class IndicatorOverlay(context: Context) : View(context) {

    private val hits = mutableListOf<IndicatorHit>()
    private val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 10, 16, 22) }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2f; color = Color.argb(255, 0, 229, 255)
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 12f * resources.displayMetrics.scaledDensity
        typeface = Typeface.create("Inter", Typeface.BOLD)
    }

    fun setIndicators(list: List<IndicatorHit>) {
        hits.clear(); hits.addAll(list); postInvalidateOnAnimation()
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        if (hits.isEmpty()) return

        val pad = 10f
        var y = pad + 8f
        val x = width - 8f

        // Draw a compact right-side stack of indicator chips
        hits.sortedByDescending { it.confidence }.take(6).forEach { h ->
            val label = chipText(h)
            val tw = text.measureText(label)
            val th = text.fontMetrics.run { bottom - top }
            val left = x - tw - 20f
            val top = y - th
            val rect = RectF(left, top - 8f, x, y + 8f)

            // background + neon edge
            c.drawRoundRect(rect, 14f, 14f, bg)
            stroke.color = colorFor(h.type)
            c.drawRoundRect(rect, 14f, 14f, stroke)

            // text
            c.drawText(label, rect.left + 10f, rect.centerY() + th/3.4f - 2f, text)
            y += th + 14f
        }
    }

    private fun chipText(h: IndicatorHit): String {
        val pct = (h.confidence.coerceIn(0f,1f) * 100).toInt()
        return "${shortName(h.type)} ${h.label}  ${pct}%"
    }

    private fun shortName(t: IndicatorType) = when (t) {
        IndicatorType.MA_EMA -> "EMA"
        IndicatorType.MA_SMA -> "SMA"
        IndicatorType.MA_WMA -> "WMA"
        IndicatorType.BOLLINGER -> "BB"
        IndicatorType.VWAP -> "VWAP"
        IndicatorType.RSI -> "RSI"
        IndicatorType.MACD -> "MACD"
        IndicatorType.VOLUME -> "VOL"
        IndicatorType.ICHIMOKU -> "ICH"
        else -> "IND"
    }

    private fun colorFor(t: IndicatorType): Int = when (t) {
        IndicatorType.MA_EMA -> Color.argb(255, 0, 229, 255)
        IndicatorType.MA_SMA -> Color.argb(255, 0, 200, 255)
        IndicatorType.MA_WMA -> Color.argb(255, 80, 220, 255)
        IndicatorType.BOLLINGER -> Color.argb(255, 0, 255, 180)
        IndicatorType.VWAP -> Color.argb(255, 0, 255, 130)
        IndicatorType.RSI -> Color.argb(255, 255, 180, 0)
        IndicatorType.MACD -> Color.argb(255, 255, 100, 80)
        IndicatorType.VOLUME -> Color.argb(255, 160, 160, 255)
        IndicatorType.ICHIMOKU -> Color.argb(255, 120, 255, 160)
        else -> Color.argb(255, 180, 180, 180)
    }
}
