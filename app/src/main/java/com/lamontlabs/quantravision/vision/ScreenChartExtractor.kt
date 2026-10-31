package com.lamontlabs.quantravision.vision

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Measures red/green (including teal bullish) candlesticks in screen pixels.
 * This is deterministic image geometry, not a source of prices, volume or time.
 * Unsupported palettes, line charts and ambiguous images return no candles.
 */
class ScreenChartExtractor {
    data class CandleGeometry(
        val left: Int, val right: Int,
        val highY: Int, val lowY: Int,
        val bodyTop: Int, val bodyBottom: Int,
        val bullish: Boolean, val confidence: Float
    ) {
        val centerX: Float get() = (left + right) / 2f
    }

    fun extract(width: Int, height: Int, pixels: IntArray): List<CandleGeometry> {
        require(width > 0 && height > 0 && width.toLong() * height == pixels.size.toLong())
        if (width < 160 || height < 120 || pixels.size > 4_000_000) return emptyList()
        val mask = ByteArray(pixels.size) { classify(pixels[it]) }
        val queue = IntArray(pixels.size)
        val candidates = mutableListOf<CandleGeometry>()
        for (start in mask.indices) {
            val color = mask[start]
            if (color.toInt() == 0) continue
            var head = 0
            var tail = 1
            queue[0] = start
            mask[start] = 0
            var left = width
            var right = 0
            var top = height
            var bottom = 0
            while (head < tail) {
                val index = queue[head++]
                val x = index % width
                val y = index / width
                left = min(left, x); right = max(right, x)
                top = min(top, y); bottom = max(bottom, y)
                fun visit(next: Int) {
                    if (mask[next] == color) { mask[next] = 0; queue[tail++] = next }
                }
                if (x > 0) visit(index - 1)
                if (x + 1 < width) visit(index + 1)
                if (y > 0) visit(index - width)
                if (y + 1 < height) visit(index + width)
            }
            val bodyWidth = right - left + 1
            val totalHeight = bottom - top + 1
            if (bodyWidth !in 3..max(4, width / 25) || totalHeight < bodyWidth + 2 ||
                totalHeight > height * 0.60 || tail < 10) continue
            val rows = IntArray(totalHeight)
            for (i in 0 until tail) rows[queue[i] / width - top]++
            val wideRows = rows.indices.filter { rows[it] >= max(3, (bodyWidth * 0.65).toInt()) }
            if (wideRows.isEmpty()) continue
            val bodyTop = top + wideRows.first()
            val bodyBottom = top + wideRows.last()
            // A candle must have a measurable body and a narrower wick. This
            // rejects volume histograms and ordinary colored buttons/labels.
            if (bodyBottom - bodyTop < 1 || (bodyTop == top && bodyBottom == bottom)) continue
            if ((bodyTop..bodyBottom).any { rows[it - top] < 2 }) continue
            val center = (left + right) / 2f
            val wickPixels = (0 until tail).filter {
                val y = queue[it] / width
                y < bodyTop || y > bodyBottom
            }
            if (wickPixels.any { abs(queue[it] % width - center) > max(1.5f, bodyWidth * 0.25f) }) continue
            val fill = tail.toFloat() / (bodyWidth * (bodyBottom - bodyTop + 1) + totalHeight)
            candidates += CandleGeometry(left, right + 1, top, bottom + 1, bodyTop,
                bodyBottom + 1, color.toInt() == 1, fill.coerceIn(0f, 1f))
        }
        if (candidates.size < MIN_CANDLES) return emptyList()
        // Pick a consistent candle width and one observation per x position.
        val medianWidth = candidates.map { it.right - it.left }.sorted().let { it[it.size / 2] }
        val ordered = candidates.filter { abs((it.right - it.left) - medianWidth) <= max(2, medianWidth / 2) }
            .sortedBy { it.centerX }
        val series = mutableListOf<CandleGeometry>()
        for (candle in ordered) {
            if (series.isEmpty() || candle.left >= series.last().right) series += candle
            else if (candle.confidence > series.last().confidence) series[series.lastIndex] = candle
        }
        if (series.size < MIN_CANDLES || series.last().right - series.first().left < width / 4) return emptyList()
        val gaps = series.zipWithNext { a, b -> b.centerX - a.centerX }.sorted()
        val medianGap = gaps[gaps.size / 2]
        if (gaps.count { it in medianGap * 0.55f..medianGap * 1.8f } < gaps.size * 0.75) return emptyList()
        return series.take(500)
    }

    private fun classify(argb: Int): Byte {
        if ((argb ushr 24) < 128) return 0
        val r = (argb ushr 16) and 255
        val g = (argb ushr 8) and 255
        val b = argb and 255
        if (max(r, max(g, b)) < 65 || max(r, max(g, b)) - min(r, min(g, b)) < 45) return 0
        return when {
            g > r * 1.25 && g >= b * 0.85 -> 1
            r > g * 1.25 && r > b * 1.15 -> 2
            else -> 0
        }
    }

    companion object { const val MIN_CANDLES = 10 }
}
