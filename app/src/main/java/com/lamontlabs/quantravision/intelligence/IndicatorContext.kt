package com.lamontlabs.quantravision.intelligence

import com.google.gson.Gson

/**
 * QuantraCore: Indicator Context
 * 
 * Captures technical indicators extracted from chart screenshots via OCR.
 * Used for multi-signal confluence analysis with pattern detection.
 * 
 * All values are nullable - if indicator not found or can't be parsed, it's null.
 */
data class IndicatorContext(
    val rsi: Double? = null,              // RSI value (0-100)
    val macd: MacdValues? = null,         // MACD histogram, signal, main line
    val volume: VolumeContext? = null,    // Volume analysis
    val movingAverages: List<MovingAverage>? = null,  // Detected MAs
    val priceLevel: Double? = null,       // Current price if detected
    val timestamp: Long = System.currentTimeMillis()
) {
    
    /**
     * MACD indicator values
     */
    data class MacdValues(
        val histogram: Double? = null,    // MACD histogram value
        val signal: Double? = null,       // Signal line value
        val macdLine: Double? = null,     // Main MACD line value
        val crossover: CrossoverType? = null  // Detected crossover pattern
    )
    
    /**
     * Volume context and analysis
     */
    data class VolumeContext(
        val current: Double? = null,      // Current volume
        val average: Double? = null,      // Average volume (if detected)
        val spike: Boolean = false,       // True if volume > 1.5x average
        val trend: VolumeTrend? = null    // Rising/falling/neutral
    )
    
    /**
     * Moving average detection
     */
    data class MovingAverage(
        val period: Int,                  // Period (e.g., 20, 50, 200)
        val value: Double,                // MA value
        val type: MaType = MaType.SIMPLE  // SMA, EMA, etc.
    )
    
    enum class CrossoverType {
        BULLISH_CROSSOVER,   // MACD crossed above signal
        BEARISH_CROSSOVER,   // MACD crossed below signal
        NONE
    }
    
    enum class VolumeTrend {
        RISING,
        FALLING,
        NEUTRAL
    }
    
    enum class MaType {
        SIMPLE,
        EXPONENTIAL,
        WEIGHTED
    }
    
    /**
     * Convert to JSON for database storage
     */
    fun toJson(): String = Gson().toJson(this)
    
    companion object {
        /**
         * Parse from JSON stored in database
         */
        fun fromJson(json: String?): IndicatorContext? {
            if (json.isNullOrBlank()) return null
            return try {
                Gson().fromJson(json, IndicatorContext::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Empty context when no indicators detected
         */
        fun empty() = IndicatorContext()
    }
    
    /**
     * Check if any indicators were successfully extracted
     */
    fun hasAnyIndicators(): Boolean {
        return rsi != null || 
               macd != null || 
               volume != null || 
               !movingAverages.isNullOrEmpty() ||
               priceLevel != null
    }
    
    /**
     * Get human-readable summary of indicators
     */
    fun getSummary(): String {
        val parts = mutableListOf<String>()
        
        rsi?.let { parts.add("RSI: ${it.toInt()}") }
        macd?.crossover?.let { 
            if (it != CrossoverType.NONE) {
                parts.add("MACD: $it")
            }
        }
        volume?.let { 
            if (it.spike) parts.add("Volume Spike")
        }
        
        return if (parts.isEmpty()) "No indicators" else parts.joinToString(", ")
    }
}
