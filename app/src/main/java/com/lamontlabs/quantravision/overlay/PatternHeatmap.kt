package com.lamontlabs.quantravision.overlay

import android.graphics.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.exp
import kotlin.math.max

/**
 * PatternHeatmapOverlay
 * - Renders a faint density heatmap of recent pattern detections.
 * - Operates entirely on-device (no network, no persistence).
 * - Automatically clears entries older than 1 hour.
 */
object PatternHeatmapOverlay {
    private val detections = CopyOnWriteArrayList<HeatEntry>()
    private const val MAX_POINTS = 500
    private const val TTL_MS = 3_600_000L // 1 hour

    fun add(x: Float, y: Float, conf: Float) {
        if (detections.size > MAX_POINTS) detections.removeFirst()
        detections.add(HeatEntry(System.currentTimeMillis(), x, y, conf))
        purgeOld()
    }

    fun draw(canvas: Canvas, w: Int, h: Int) {
        purgeOld()
        if (detections.isEmpty()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        detections.forEach { e ->
            val ageFactor = 1f - ((System.currentTimeMillis() - e.ts).toFloat() / TTL_MS)
            val alpha = (max(0f, ageFactor) * e.conf * 180).toInt()
            val radius = 20f + 60f * e.conf
            val grad = RadialGradient(
                e.x * w,
                e.y * h,
                radius,
                intArrayOf(Color.argb(alpha, 0, 229, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = grad
            canvas.drawCircle(e.x * w, e.y * h, radius, paint)
        }
    }

    private fun purgeOld() {
        val now = System.currentTimeMillis()
        detections.removeIf { now - it.ts > TTL_MS }
    }

    private data class HeatEntry(val ts: Long, val x: Float, val y: Float, val conf: Float)
}
