package com.lamontlabs.quantravision.detection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.lamontlabs.quantravision.ScaleSpace
import com.lamontlabs.quantravision.Template
import com.lamontlabs.quantravision.TemplateLibrary
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

/** Single-frame template matching, with no alerts, learning writes or hidden fallback. */
class VisualTemplateScanner(context: Context) : AutoCloseable {
    private val library = TemplateLibrary(context)
    private var templates: List<Template>? = null

    @Synchronized
    private fun templates(): List<Template> = templates ?: library.loadTemplates().sortedBy { it.name }.also { templates = it }

    fun getTemplateCount(): Int = templates().size

    @Synchronized
    fun scan(bitmap: Bitmap): List<DetectionResult> {
        val input = Mat()
        // A pyramid belongs to this frame only. Native pointers are not content hashes.
        val pyramid = mutableMapOf<Double, Mat>()
        try {
            Utils.bitmapToMat(bitmap, input)
            Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)
            return templates().groupBy { it.name }.mapNotNull { (name, family) ->
                var best: DetectionResult? = null
                for (template in family) {
                    val config = ScaleSpace.ScaleConfig(minScale = template.scaleMin.coerceIn(0.4, 2.5),
                        maxScale = template.scaleMax.coerceIn(0.4, 2.5))
                    for (scale in ScaleSpace.scales(config)) {
                        val scaled = pyramid.getOrPut(scale) { ScaleSpace.resizeForScaleNoCache(input, scale) }
                        if (scaled.cols() < template.image.cols() || scaled.rows() < template.image.rows()) continue
                        val output = Mat()
                        try {
                            Imgproc.matchTemplate(scaled, template.image, output, Imgproc.TM_CCOEFF_NORMED)
                            val match = Core.minMaxLoc(output)
                            // The same geometry can have reversed brightness on dark charts.
                            val inverted = abs(match.minVal) > abs(match.maxVal)
                            val confidence = if (inverted) abs(match.minVal) else abs(match.maxVal)
                            val location = if (inverted) match.minLoc else match.maxLoc
                            if (!confidence.isFinite() || confidence < template.threshold || confidence !in 0.0..1.0 ||
                                confidence <= (best?.confidence ?: -1.0)) continue
                            val b = SourceBounds.fromScaled(location.x, location.y,
                                template.image.cols().toDouble(), template.image.rows().toDouble(), scale)
                            if (b.right > bitmap.width + 1 || b.bottom > bitmap.height + 1) continue
                            best = DetectionResult(name, confidence, RectF(b.left.toFloat(), b.top.toFloat(),
                                b.right.coerceAtMost(bitmap.width.toDouble()).toFloat(),
                                b.bottom.coerceAtMost(bitmap.height.toDouble()).toFloat()),
                                reasoning = "Visual template similarity; market price levels unavailable")
                        } finally { output.release() }
                    }
                }
                best
            }.sortedByDescending { it.confidence }
        } finally {
            pyramid.values.forEach { it.release() }
            input.release()
        }
    }

    @Synchronized
    override fun close() {
        templates?.forEach { it.image.release() }
        templates = null
    }
}

/** Coordinates from a resized input must be divided by that input's scale. */
data class SourceBounds(val left: Double, val top: Double, val right: Double, val bottom: Double) {
    companion object {
        fun fromScaled(x: Double, y: Double, width: Double, height: Double, scale: Double): SourceBounds {
            require(listOf(x, y, width, height, scale).all { it.isFinite() })
            require(x >= 0 && y >= 0 && width > 0 && height > 0 && scale > 0)
            return SourceBounds(x / scale, y / scale, (x + width) / scale, (y + height) / scale)
        }
    }
}
