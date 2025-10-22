package com.lamontlabs.quantravision

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

/**
 * Deterministic multi-scale pyramid for template matching across timeframes.
 *
 * Rationale:
 * - Timeframes change apparent candle width and pattern geometry on screen.
 * - To remain timeframe-agnostic, we search a bounded set of scales.
 *
 * Implementation:
 * - Build a geometric scale ladder [minScale..maxScale] with fixed stride.
 * - Resize input up/down and run detector per scale.
 * - Keep best match per template across scales.
 */
object ScaleSpace {

    data class ScaleConfig(
        val minScale: Double = 0.6,
        val maxScale: Double = 1.8,
        val stride: Double = 0.15
    )

    fun scales(cfg: ScaleConfig): List<Double> {
        val list = mutableListOf<Double>()
        var s = cfg.minScale
        while (s <= cfg.maxScale + 1e-9) {
            // quantize to 2 decimals to stay deterministic
            list.add(kotlin.math.round(s * 100) / 100.0)
            s += cfg.stride
        }
        return list
    }

    fun resizeForScale(src: Mat, scale: Double): Mat {
        val dst = Mat()
        val newW = (src.width() * scale).toInt().coerceAtLeast(8)
        val newH = (src.height() * scale).toInt().coerceAtLeast(8)
        Imgproc.resize(src, dst, org.opencv.core.Size(newW.toDouble(), newH.toDouble()), 0.0, 0.0, Imgproc.INTER_AREA)
        return dst
    }
}
