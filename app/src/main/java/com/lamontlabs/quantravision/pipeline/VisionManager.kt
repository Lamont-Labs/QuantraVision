package com.lamontlabs.quantravision.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lamontlabs.quantravision.vision.ScreenChartExtractor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.abs

/** On-device measurement and OCR. No downloaded vision model is required. */
class VisionManager(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val textReader: ChartTextReader = MlKitChartTextReader()
) {
    data class VisionOutput(
        val chartType: String,
        val detectedTicker: String?,
        val detectedTimeframe: String?,
        val detectedCandles: List<DetectedCandle>,
        val detectedTrendlines: List<DetectedTrendline>,
        val detectedLevels: List<DetectedLevel>,
        val ocrText: String,
        val imageHash: String,
        val processingTimeMs: Long
    )
    data class DetectedCandle(
        val index: Int, val bounds: RectF, val isBullish: Boolean, val confidence: Float,
        val bodyTop: Float = bounds.top, val bodyBottom: Float = bounds.bottom
    )
    data class DetectedTrendline(
        val startX: Float, val startY: Float, val endX: Float, val endY: Float,
        val lineType: TrendlineType, val confidence: Float
    )
    enum class TrendlineType { SUPPORT, RESISTANCE, CHANNEL_UPPER, CHANNEL_LOWER, TREND_UP, TREND_DOWN, UNKNOWN }
    /** pixelY is an image coordinate, never a market price. */
    data class DetectedLevel(val pixelY: Float, val levelType: LevelType, val strength: Float)
    enum class LevelType { SUPPORT, RESISTANCE, PIVOT }

    suspend fun processImage(bitmap: Bitmap): VisionOutput? = withContext(Dispatchers.Default) {
        if (bitmap.isRecycled || bitmap.width < 160 || bitmap.height < 120 ||
            bitmap.width.toLong() * bitmap.height > 4_000_000) return@withContext null
        val started = android.os.SystemClock.elapsedRealtime()
        try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val geometry = ScreenChartExtractor().extract(bitmap.width, bitmap.height, pixels)
            if (geometry.isEmpty()) return@withContext null
            val candles = geometry.mapIndexed { i, c ->
                DetectedCandle(i, RectF(c.left.toFloat(), c.highY.toFloat(), c.right.toFloat(), c.lowY.toFloat()),
                    c.bullish, c.confidence, c.bodyTop.toFloat(), c.bodyBottom.toFloat())
            }
            val ocrText = try {
                textReader.read(bitmap)
            } catch (cancel: CancellationException) { throw cancel
            } catch (error: Exception) {
                Timber.w(error, "Chart OCR unavailable; symbol and timeframe remain unknown")
                ""
            }
            val bytes = ByteBuffer.allocate(8 + pixels.size * 4).putInt(bitmap.width).putInt(bitmap.height)
            pixels.forEach { bytes.putInt(it) }
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes.array()).joinToString("") { "%02x".format(it) }
            VisionOutput("Candlestick", extractTicker(ocrText), extractTimeframe(ocrText), candles,
                fitBoundaries(candles), findLevels(candles), ocrText, hash,
                android.os.SystemClock.elapsedRealtime() - started)
        } catch (cancel: CancellationException) { throw cancel
        } catch (error: Exception) {
            Timber.e(error, "Image measurement failed")
            null
        }
    }

    /** Inferred least-squares boundaries, only retained when residuals are small. */
    private fun fitBoundaries(candles: List<DetectedCandle>): List<DetectedTrendline> {
        val xs = candles.map { it.bounds.centerX().toDouble() }
        val meanX = xs.average()
        val variance = xs.sumOf { (it - meanX) * (it - meanX) }
        if (variance <= 0.0) return emptyList()
        return listOf(TrendlineType.SUPPORT, TrendlineType.RESISTANCE).mapNotNull { type ->
            val ys = candles.map { if (type == TrendlineType.SUPPORT) it.bounds.bottom.toDouble() else it.bounds.top.toDouble() }
            val meanY = ys.average()
            val slope = xs.indices.sumOf { (xs[it] - meanX) * (ys[it] - meanY) } / variance
            val residual = xs.indices.map { abs(ys[it] - (meanY + slope * (xs[it] - meanX))) }.average()
            val tolerance = candles.map { it.bounds.height().toDouble() }.average().coerceAtLeast(1.0)
            val confidence = (1.0 - residual / tolerance).coerceIn(0.0, 1.0)
            if (confidence < 0.65) null else DetectedTrendline(xs.first().toFloat(),
                (meanY + slope * (xs.first() - meanX)).toFloat(), xs.last().toFloat(),
                (meanY + slope * (xs.last() - meanX)).toFloat(), type, confidence.toFloat())
        }
    }

    private fun findLevels(candles: List<DetectedCandle>): List<DetectedLevel> {
        val tolerance = (candles.map { it.bounds.height() }.average() * 0.15).coerceAtLeast(2.0)
        return listOf(LevelType.SUPPORT, LevelType.RESISTANCE).flatMap { type ->
            val values = candles.map { if (type == LevelType.SUPPORT) it.bounds.bottom else it.bounds.top }.sorted()
            val groups = mutableListOf<MutableList<Float>>()
            for (y in values) {
                val group = groups.lastOrNull()
                if (group != null && y - group.first() <= tolerance) group += y else groups += mutableListOf(y)
            }
            groups.filter { it.size >= 3 }.map { DetectedLevel(it.average().toFloat(), type,
                (it.size.toFloat() / candles.size).coerceIn(0f, 1f)) }
        }
    }

    private fun extractTicker(text: String): String? =
        Regex("(?:NASDAQ|NYSE|AMEX|TICKER)\\s*[:·]\\s*([A-Z][A-Z0-9.]{0,7})\\b").find(text)?.groupValues?.get(1)
            ?: Regex("\\$([A-Z][A-Z.]{0,5})\\b").find(text)?.groupValues?.get(1)

    private fun extractTimeframe(text: String): String? =
        Regex("(?i)\\b(1|3|5|15|30|60|120|240|4)(m|h|d|w)\\b").find(text)?.value

    fun release() = textReader.close()
}

interface ChartTextReader : AutoCloseable {
    suspend fun read(bitmap: Bitmap): String
    override fun close() {}
}

private class MlKitChartTextReader : ChartTextReader {
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    override suspend fun read(bitmap: Bitmap): String = recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text
    override fun close() { recognizer.close() }
}
