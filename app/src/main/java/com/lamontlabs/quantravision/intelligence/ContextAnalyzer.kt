package com.lamontlabs.quantravision.intelligence

import timber.log.Timber

/**
 * QuantraCore: Context Analyzer
 * 
 * Analyzes confluence between pattern detection and technical indicators.
 * Determines if multiple signals align to create high-probability trade setups.
 * 
 * Confluence Types:
 * - Bullish: Bullish pattern + RSI oversold + MACD bullish crossover + volume spike
 * - Bearish: Bearish pattern + RSI overbought + MACD bearish crossover + volume spike
 * - Neutral: Mixed signals or insufficient data
 */
class ContextAnalyzer {
    
    companion object {
        private const val TAG = "ContextAnalyzer"
        
        // RSI thresholds
        private const val RSI_OVERSOLD = 30.0
        private const val RSI_OVERBOUGHT = 70.0
        private const val RSI_NEUTRAL_LOW = 40.0
        private const val RSI_NEUTRAL_HIGH = 60.0
        
        // Bullish pattern keywords
        private val BULLISH_PATTERNS = setOf(
            "cup and handle", "double bottom", "inverse head and shoulders",
            "ascending triangle", "bullish flag", "bullish pennant",
            "rising wedge", "morning star", "hammer", "bullish engulfing"
        )
        
        // Bearish pattern keywords
        private val BEARISH_PATTERNS = setOf(
            "head and shoulders", "double top", "descending triangle",
            "bearish flag", "bearish pennant", "falling wedge",
            "evening star", "shooting star", "bearish engulfing"
        )
    }
    
    /**
     * Analysis result with confluence details
     */
    data class AnalysisResult(
        val confluenceType: ConfluenceType,
        val signalStrength: SignalStrength,
        val supportingFactors: List<String>,
        val conflictingFactors: List<String>,
        val confidenceBoost: Double  // Multiplier for pattern confidence (0.8 - 1.3)
    )
    
    enum class ConfluenceType {
        BULLISH,
        BEARISH,
        NEUTRAL,
        CONFLICTING
    }
    
    enum class SignalStrength {
        STRONG,      // 3+ supporting factors, 0 conflicts
        MODERATE,    // 2 supporting factors, max 1 conflict
        WEAK,        // 1 supporting factor or 2+ conflicts
        INSUFFICIENT // No indicators available
    }
    
    /**
     * Analyze confluence between pattern and indicators
     */
    fun analyze(patternName: String, patternConfidence: Double, indicators: IndicatorContext): AnalysisResult {
        val supporting = mutableListOf<String>()
        val conflicting = mutableListOf<String>()
        
        // Determine pattern bias
        val patternBias = getPatternBias(patternName)
        
        // If no indicators, return neutral with no boost
        if (!indicators.hasAnyIndicators()) {
            return AnalysisResult(
                confluenceType = ConfluenceType.NEUTRAL,
                signalStrength = SignalStrength.INSUFFICIENT,
                supportingFactors = emptyList(),
                conflictingFactors = listOf("No indicators detected"),
                confidenceBoost = 1.0
            )
        }
        
        // Analyze RSI
        indicators.rsi?.let { rsi ->
            when (patternBias) {
                ConfluenceType.BULLISH -> {
                    when {
                        rsi < RSI_OVERSOLD -> supporting.add("RSI oversold (${"%.0f".format(rsi)})")
                        rsi < RSI_NEUTRAL_LOW -> supporting.add("RSI below neutral (${"%.0f".format(rsi)})")
                        rsi > RSI_OVERBOUGHT -> conflicting.add("RSI overbought on bullish pattern")
                        else -> {}
                    }
                }
                ConfluenceType.BEARISH -> {
                    when {
                        rsi > RSI_OVERBOUGHT -> supporting.add("RSI overbought (${"%.0f".format(rsi)})")
                        rsi > RSI_NEUTRAL_HIGH -> supporting.add("RSI above neutral (${"%.0f".format(rsi)})")
                        rsi < RSI_OVERSOLD -> conflicting.add("RSI oversold on bearish pattern")
                        else -> {}
                    }
                }
                else -> {}
            }
        }
        
        // Analyze MACD
        indicators.macd?.crossover?.let { crossover ->
            when (patternBias) {
                ConfluenceType.BULLISH -> {
                    when (crossover) {
                        IndicatorContext.CrossoverType.BULLISH_CROSSOVER -> 
                            supporting.add("MACD bullish crossover")
                        IndicatorContext.CrossoverType.BEARISH_CROSSOVER -> 
                            conflicting.add("MACD bearish crossover on bullish pattern")
                        else -> {}
                    }
                }
                ConfluenceType.BEARISH -> {
                    when (crossover) {
                        IndicatorContext.CrossoverType.BEARISH_CROSSOVER -> 
                            supporting.add("MACD bearish crossover")
                        IndicatorContext.CrossoverType.BULLISH_CROSSOVER -> 
                            conflicting.add("MACD bullish crossover on bearish pattern")
                        else -> {}
                    }
                }
                else -> {}
            }
        }
        
        // Analyze Volume
        indicators.volume?.let { vol ->
            if (vol.spike) {
                supporting.add("Volume spike detected")
            }
        }
        
        // Determine confluence type and strength
        val confluenceType = when {
            supporting.size >= 2 && conflicting.isEmpty() -> patternBias
            conflicting.size >= 2 -> ConfluenceType.CONFLICTING
            else -> ConfluenceType.NEUTRAL
        }
        
        val signalStrength = when {
            supporting.size >= 3 && conflicting.isEmpty() -> SignalStrength.STRONG
            supporting.size >= 2 && conflicting.size <= 1 -> SignalStrength.MODERATE
            supporting.size >= 1 || conflicting.size <= 1 -> SignalStrength.WEAK
            else -> SignalStrength.INSUFFICIENT
        }
        
        // Calculate confidence boost
        val boost = calculateConfidenceBoost(signalStrength, supporting.size, conflicting.size)
        
        val result = AnalysisResult(
            confluenceType = confluenceType,
            signalStrength = signalStrength,
            supportingFactors = supporting,
            conflictingFactors = conflicting,
            confidenceBoost = boost
        )
        
        Timber.i("Confluence analysis: $confluenceType / $signalStrength (boost: ${"%.2f".format(boost)}x)")
        return result
    }
    
    /**
     * Determine if pattern is bullish, bearish, or neutral
     */
    private fun getPatternBias(patternName: String): ConfluenceType {
        val normalized = patternName.lowercase()
        return when {
            BULLISH_PATTERNS.any { normalized.contains(it) } -> ConfluenceType.BULLISH
            BEARISH_PATTERNS.any { normalized.contains(it) } -> ConfluenceType.BEARISH
            else -> ConfluenceType.NEUTRAL
        }
    }
    
    /**
     * Calculate confidence boost multiplier based on signal strength
     * Range: 0.8 (conflicting signals) to 1.3 (strong confluence)
     */
    private fun calculateConfidenceBoost(
        strength: SignalStrength,
        supportCount: Int,
        conflictCount: Int
    ): Double {
        return when (strength) {
            SignalStrength.STRONG -> 1.25 + (supportCount - 3) * 0.05  // 1.25 - 1.35
            SignalStrength.MODERATE -> 1.10 + (supportCount - 2) * 0.05  // 1.10 - 1.20
            SignalStrength.WEAK -> 1.0  // No boost
            SignalStrength.INSUFFICIENT -> 0.95  // Slight penalty
        }.coerceIn(0.8, 1.3) - (conflictCount * 0.05)  // Reduce for conflicts
    }
}
