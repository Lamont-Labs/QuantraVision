package com.lamontlabs.quantravision.pipeline

import android.graphics.Bitmap
import com.lamontlabs.quantravision.apex.models.*

/** Converts measured pixels into explicitly tagged image geometry. No OHLCV is invented. */
class PrimitivesBuilder {
    fun buildPrimitives(visionOutput: VisionManager.VisionOutput?, bitmap: Bitmap): ChartPrimitives? {
        val output = visionOutput ?: return null
        if (output.detectedCandles.size < 10 || bitmap.isRecycled) return null
        val height = bitmap.height.toDouble()
        val candles = output.detectedCandles.sortedBy { it.index }.mapIndexed { i, c ->
            if (c.bounds.left < 0 || c.bounds.right > bitmap.width || c.bounds.top < 0 ||
                c.bounds.bottom > height || c.bodyTop < c.bounds.top || c.bodyBottom > c.bounds.bottom ||
                c.bodyBottom < c.bodyTop || c.bounds.width() <= 0 || c.bounds.height() <= 0) return null
            // Upward image coordinates. timestamp is an ordinal ONLY in SCREEN_GEOMETRY.
            val top = height + 1.0 - c.bodyTop
            val bottom = height + 1.0 - c.bodyBottom
            Candle(i.toLong(), if (c.isBullish) bottom else top,
                height + 1.0 - c.bounds.top, height + 1.0 - c.bounds.bottom,
                if (c.isBullish) top else bottom, volume = 0.0)
        }
        val lines = output.detectedTrendlines.map { line ->
            TrendLine(line.startX.toDouble(), height + 1 - line.startY,
                line.endX.toDouble(), height + 1 - line.endY, line.confidence.toDouble())
        }
        return ChartPrimitives(output.imageHash, candles, lines, output.ocrText, output.chartType,
            source = PrimitiveSource.SCREEN_GEOMETRY, imageWidth = bitmap.width, imageHeight = bitmap.height)
    }
}
