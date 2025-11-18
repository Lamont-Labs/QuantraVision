package com.lamontlabs.quantravision.intelligence

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.math.abs

/**
 * QuantraCore: Indicator Extractor
 * 
 * Uses Google ML Kit Text Recognition (on-device OCR) to extract technical
 * indicators from chart screenshots. Privacy-preserving and offline.
 * 
 * Detects:
 * - RSI values (0-100)
 * - MACD histogram/signal/main line
 * - Volume levels and spikes
 * - Moving averages
 * - Price levels
 */
class IndicatorExtractor(private val context: Context) {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    companion object {
        private const val TAG = "IndicatorExtractor"
        
        // RSI typical ranges
        private const val RSI_MIN = 0.0
        private const val RSI_MAX = 100.0
        private const val RSI_OVERSOLD = 30.0
        private const val RSI_OVERBOUGHT = 70.0
        
        // Volume spike threshold
        private const val VOLUME_SPIKE_MULTIPLIER = 1.5
    }
    
    /**
     * Extract all indicators from chart bitmap
     * ENHANCED: Now captures ALL text and unknown indicators for learning engine
     */
    suspend fun extractIndicators(bitmap: Bitmap): IndicatorContext {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = textRecognizer.process(inputImage).await()
            
            val allText = visionText.textBlocks.flatMap { block ->
                block.lines.flatMap { line -> line.elements }
            }
            
            val textStrings = allText.map { it.text }
            Timber.d("🔍 OCR extracted ${textStrings.size} text elements")
            
            // Extract known indicators
            val rsi = extractRSI(textStrings)
            val macd = extractMACD(textStrings)
            val volume = extractVolume(textStrings)
            val mas = extractMovingAverages(textStrings)
            val price = extractPrice(textStrings)
            
            // Extract ANY other indicators for learning engine
            val otherIndicators = extractUnknownIndicators(textStrings)
            
            val context = IndicatorContext(
                rsi = rsi,
                macd = macd,
                volume = volume,
                movingAverages = mas,
                priceLevel = price,
                otherIndicators = otherIndicators,
                rawText = textStrings  // Store all text for learning engine
            )
            
            Timber.i("📊 Indicators extracted: ${context.getSummary()}")
            if (!otherIndicators.isNullOrEmpty()) {
                Timber.d("🧠 Unknown indicators for learning: ${otherIndicators.keys.joinToString()}")
            }
            context
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract indicators")
            IndicatorContext.empty()
        }
    }
    
    /**
     * Extract unknown/arbitrary indicators from chart
     * Looks for patterns like "INDICATOR_NAME: VALUE" or "INDICATOR_NAME VALUE"
     * Examples: "Stoch: 45.2", "ATR 1.25", "ADX: 32", "CCI 115"
     */
    private fun extractUnknownIndicators(textElements: List<String>): Map<String, String>? {
        val indicators = mutableMapOf<String, String>()
        
        // Common indicator abbreviations to look for
        val knownIndicators = setOf(
            "ATR", "ADX", "CCI", "STOCH", "STOCHASTIC", "OBV", "MFI",
            "WILLR", "ROC", "TRIX", "DPO", "AROON", "PSAR", "SAR",
            "ICHIMOKU", "TENKAN", "KIJUN", "SENKOU", "BOLLINGER", "BB",
            "KELTNER", "DONCHIAN", "PIVOT", "FIBONACCI", "FIB",
            "VWAP", "AVWAP", "SMI", "UO", "WMA", "HMA", "DEMA", "TEMA"
        )
        
        for (i in textElements.indices) {
            val text = textElements[i].uppercase()
            
            // Check if this looks like an indicator name
            val indicatorName = knownIndicators.firstOrNull { text.contains(it) }
            
            if (indicatorName != null) {
                // Look for value in next few elements
                for (j in i..(i + 3).coerceAtMost(textElements.size - 1)) {
                    val value = parseNumber(textElements[j], allowNegative = true)
                    if (value != null) {
                        indicators[indicatorName.lowercase()] = value.toString()
                        Timber.d("🔍 Found indicator: $indicatorName = $value")
                        break
                    }
                }
            }
            
            // Also look for "WORD: NUMBER" patterns (e.g., "Delta: 0.35")
            if (text.contains(":") && text.length in 3..20) {
                val parts = text.split(":")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val value = parseNumber(parts[1].trim(), allowNegative = true)
                    if (value != null && name.matches(Regex("[A-Z]{2,10}"))) {
                        indicators[name.lowercase()] = value.toString()
                        Timber.d("🔍 Found pattern indicator: $name = $value")
                    }
                }
            }
        }
        
        return if (indicators.isNotEmpty()) indicators else null
    }
    
    /**
     * Extract RSI value (0-100)
     * Looks for patterns like "RSI(14): 45.2" or "RSI 45"
     */
    private fun extractRSI(textElements: List<String>): Double? {
        for (i in textElements.indices) {
            val text = textElements[i].uppercase()
            
            // Look for "RSI" keyword
            if (text.contains("RSI")) {
                // Check next few elements for number
                for (j in i..(i + 3).coerceAtMost(textElements.size - 1)) {
                    val value = parseNumber(textElements[j])
                    if (value != null && value >= RSI_MIN && value <= RSI_MAX) {
                        Timber.d("RSI found: $value")
                        return value
                    }
                }
            }
        }
        return null
    }
    
    /**
     * Extract MACD values
     * Looks for MACD histogram value and signal line
     */
    private fun extractMACD(textElements: List<String>): IndicatorContext.MacdValues? {
        var histogram: Double? = null
        var signal: Double? = null
        var crossover: IndicatorContext.CrossoverType? = null
        
        for (i in textElements.indices) {
            val text = textElements[i].uppercase()
            
            if (text.contains("MACD")) {
                // Look for histogram value (can be negative)
                for (j in i..(i + 5).coerceAtMost(textElements.size - 1)) {
                    val value = parseNumber(textElements[j], allowNegative = true)
                    if (value != null && histogram == null) {
                        histogram = value
                        Timber.d("MACD histogram found: $value")
                    }
                }
            }
            
            if (text.contains("SIGNAL")) {
                val value = parseNumber(textElements.getOrNull(i + 1) ?: "", allowNegative = true)
                if (value != null) {
                    signal = value
                    Timber.d("MACD signal found: $value")
                }
            }
        }
        
        // Detect crossover if we have histogram
        if (histogram != null) {
            crossover = when {
                histogram > 0.0 -> IndicatorContext.CrossoverType.BULLISH_CROSSOVER
                histogram < 0.0 -> IndicatorContext.CrossoverType.BEARISH_CROSSOVER
                else -> IndicatorContext.CrossoverType.NONE
            }
        }
        
        return if (histogram != null || signal != null) {
            IndicatorContext.MacdValues(histogram, signal, null, crossover)
        } else {
            null
        }
    }
    
    /**
     * Extract volume information
     * Looks for volume values and "Vol" keyword
     */
    private fun extractVolume(textElements: List<String>): IndicatorContext.VolumeContext? {
        for (i in textElements.indices) {
            val text = textElements[i].uppercase()
            
            if (text.contains("VOL") && !text.contains("VOLUME")) {
                // Look for large numbers (volume is usually in thousands/millions)
                for (j in i..(i + 3).coerceAtMost(textElements.size - 1)) {
                    val value = parseNumber(textElements[j])
                    if (value != null && value > 100.0) {  // Volume is typically large
                        Timber.d("Volume found: $value")
                        // For now, just detect presence - spike detection needs historical data
                        return IndicatorContext.VolumeContext(
                            current = value,
                            average = null,
                            spike = false,  // Would need historical comparison
                            trend = null
                        )
                    }
                }
            }
        }
        return null
    }
    
    /**
     * Extract moving averages
     * Looks for MA(20), EMA(50), etc.
     */
    private fun extractMovingAverages(textElements: List<String>): List<IndicatorContext.MovingAverage>? {
        val mas = mutableListOf<IndicatorContext.MovingAverage>()
        
        for (i in textElements.indices) {
            val text = textElements[i].uppercase()
            
            // Look for MA(period) or EMA(period)
            val maType = when {
                text.contains("EMA") -> IndicatorContext.MaType.EXPONENTIAL
                text.contains("MA") -> IndicatorContext.MaType.SIMPLE
                else -> null
            }
            
            if (maType != null) {
                // Extract period from parentheses
                val period = Regex("""\((\d+)\)""").find(text)?.groupValues?.get(1)?.toIntOrNull()
                
                // Look for value nearby
                for (j in i..(i + 2).coerceAtMost(textElements.size - 1)) {
                    val value = parseNumber(textElements[j])
                    if (value != null && period != null) {
                        mas.add(IndicatorContext.MovingAverage(period, value, maType))
                        Timber.d("MA found: $maType($period) = $value")
                        break
                    }
                }
            }
        }
        
        return if (mas.isNotEmpty()) mas else null
    }
    
    /**
     * Extract current price level
     * Usually largest number on chart
     */
    private fun extractPrice(textElements: List<String>): Double? {
        val numbers = textElements.mapNotNull { parseNumber(it) }
        // Price is typically a moderate-sized number (not RSI 0-100, not huge volume)
        return numbers.filter { it > 1.0 && it < 1_000_000.0 }.maxOrNull()
    }
    
    /**
     * Parse number from text, handling various formats
     * Supports: "45.2", "-12.5", "1,234.56", "1.2K", "3.5M"
     */
    private fun parseNumber(text: String, allowNegative: Boolean = false): Double? {
        try {
            val cleaned = text
                .replace(",", "")  // Remove thousand separators
                .replace("%", "")  // Remove percent signs
                .trim()
            
            // Handle K/M suffixes (e.g., "1.5K" = 1500)
            val multiplier = when {
                cleaned.endsWith("K", ignoreCase = true) -> 1_000.0
                cleaned.endsWith("M", ignoreCase = true) -> 1_000_000.0
                cleaned.endsWith("B", ignoreCase = true) -> 1_000_000_000.0
                else -> 1.0
            }
            
            val numStr = cleaned.replace(Regex("[KMB]", RegexOption.IGNORE_CASE), "")
            val value = numStr.toDoubleOrNull() ?: return null
            
            // Reject negative numbers if not allowed
            if (!allowNegative && value < 0) return null
            
            return value * multiplier
            
        } catch (e: Exception) {
            return null
        }
    }
    
    /**
     * Release resources
     */
    fun cleanup() {
        textRecognizer.close()
    }
}
