package com.lamontlabs.quantravision.vision

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.apex.models.Candle
import com.lamontlabs.quantravision.apex.models.ChartPrimitives
import com.lamontlabs.quantravision.apex.models.TrendLine
import com.lamontlabs.quantravision.intelligence.IndicatorExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.MessageDigest

class PrimitiveExtractor(private val context: Context) {

    companion object {
        private const val TAG = "PrimitiveExtractor"
    }

    suspend fun extract(bitmap: Bitmap): ChartPrimitives {
        return withContext(Dispatchers.Default) {
            Timber.d("$TAG: Extracting primitives from ${bitmap.width}x${bitmap.height} bitmap")
            
            val hash = computeImageHash(bitmap)
            val chartType = detectChartType(bitmap)
            val candles = extractCandles(bitmap)
            val lines = extractLines(bitmap)
            val ocrText = extractOCRText(bitmap)
            
            val primitives = ChartPrimitives(
                rawImageHash = hash,
                candles = candles,
                detectedLines = lines,
                ocrText = ocrText.joinToString(" "),
                chartType = chartType
            )
            
            Timber.i("$TAG: Extracted primitives: hash=$hash, type=$chartType, candles=${candles.size}, lines=${lines.size}, ocrTokens=${ocrText.size}")
            primitives
        }
    }

    private suspend fun extractCandles(bitmap: Bitmap): List<Candle> {
        return try {
            Timber.v("$TAG: Candle extraction not yet implemented (future batch)")
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Candle extraction failed")
            emptyList()
        }
    }

    private suspend fun extractLines(bitmap: Bitmap): List<TrendLine> {
        return try {
            Timber.v("$TAG: Line detection not yet implemented (future batch)")
            emptyList()
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Line extraction failed")
            emptyList()
        }
    }

    private suspend fun extractOCRText(bitmap: Bitmap): List<String> {
        return try {
            val indicatorExtractor = IndicatorExtractor(context)
            val indicatorContext = indicatorExtractor.extractIndicators(bitmap)
            
            val rawText = indicatorContext.rawText ?: emptyList()
            Timber.d("$TAG: OCR extracted ${rawText.size} text elements")
            
            rawText
        } catch (e: Exception) {
            Timber.e(e, "$TAG: OCR extraction failed")
            emptyList()
        }
    }

    private fun detectChartType(bitmap: Bitmap): String {
        return try {
            Timber.v("$TAG: Chart type detection defaulting to CANDLE (future enhancement)")
            "CANDLE"
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Chart type detection failed")
            "Unknown"
        }
    }

    private fun computeImageHash(bitmap: Bitmap): String {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val timestamp = System.currentTimeMillis()
            
            val input = "$width:$height:$timestamp"
            val bytes = input.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(bytes)
            
            hashBytes.joinToString("") { "%02x".format(it) }.take(16)
        } catch (e: Exception) {
            Timber.e(e, "$TAG: Hash computation failed, using fallback")
            "${bitmap.width}x${bitmap.height}_${System.currentTimeMillis()}"
        }
    }
}
