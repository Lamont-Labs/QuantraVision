package com.lamontlabs.quantravision.intelligence.llm

/**
 * Template-based fallback explanations when LLM is unavailable
 * 
 * Provides basic pattern descriptions as a graceful degradation
 * when the Gemma model isn't downloaded or loaded.
 */
object FallbackExplanations {
    
    /**
     * Generate a template-based explanation for a pattern
     */
    fun getPatternExplanation(
        patternName: String,
        quantraScore: Int,
        hasRSI: Boolean = false,
        hasMACD: Boolean = false,
        hasVolume: Boolean = false
    ): String {
        val grade = when {
            quantraScore >= 90 -> "exceptional"
            quantraScore >= 80 -> "high-quality"
            quantraScore >= 60 -> "good"
            quantraScore >= 40 -> "fair"
            else -> "low-quality"
        }
        
        val patternInfo = getPatternInfo(patternName)
        val indicatorConfirmation = buildIndicatorText(hasRSI, hasMACD, hasVolume)
        
        return buildString {
            append("This $patternName pattern scored $quantraScore/100 ($grade). ")
            append(patternInfo)
            if (indicatorConfirmation.isNotBlank()) {
                append(" $indicatorConfirmation")
            }
        }
    }
    
    /**
     * Get basic pattern information
     */
    private fun getPatternInfo(patternName: String): String {
        return when {
            patternName.contains("Head and Shoulders", ignoreCase = true) ->
                "This reversal pattern typically signals a trend change when the neckline breaks."
            
            patternName.contains("Double Top", ignoreCase = true) ->
                "This bearish reversal forms when price tests a resistance level twice and fails."
            
            patternName.contains("Double Bottom", ignoreCase = true) ->
                "This bullish reversal occurs when price finds support at the same level twice."
            
            patternName.contains("Triangle", ignoreCase = true) ->
                "Triangle patterns represent consolidation before a breakout in either direction."
            
            patternName.contains("Flag", ignoreCase = true) || patternName.contains("Pennant", ignoreCase = true) ->
                "This continuation pattern typically resolves in the direction of the prior trend."
            
            patternName.contains("Wedge", ignoreCase = true) ->
                "Wedge patterns often signal trend reversals with converging trendlines."
            
            patternName.contains("Cup", ignoreCase = true) || patternName.contains("Handle", ignoreCase = true) ->
                "This bullish continuation pattern shows accumulation before an upward move."
            
            patternName.contains("Channel", ignoreCase = true) ->
                "Price is moving within parallel support and resistance levels."
            
            else ->
                "This pattern was detected with our pattern recognition system."
        }
    }
    
    /**
     * Build indicator confirmation text
     */
    private fun buildIndicatorText(hasRSI: Boolean, hasMACD: Boolean, hasVolume: Boolean): String {
        val indicators = mutableListOf<String>()
        if (hasRSI) indicators.add("RSI")
        if (hasMACD) indicators.add("MACD")
        if (hasVolume) indicators.add("volume")
        
        return when (indicators.size) {
            0 -> ""
            1 -> "Confirmed by ${indicators[0]} analysis."
            2 -> "Confirmed by ${indicators[0]} and ${indicators[1]} analysis."
            else -> "Confirmed by ${indicators.joinToString(", ", limit = 2, truncated = "and more")} analysis."
        }
    }
    
    /**
     * Educational answer fallback
     */
    fun getEducationalAnswer(question: String): String {
        return when {
            question.contains("RSI", ignoreCase = true) ->
                "The Relative Strength Index (RSI) measures momentum on a 0-100 scale. " +
                "Above 70 suggests overbought conditions, below 30 suggests oversold. " +
                "It helps identify potential reversal points."
            
            question.contains("MACD", ignoreCase = true) ->
                "The Moving Average Convergence Divergence (MACD) shows the relationship between two moving averages. " +
                "Crossovers and divergences can signal trend changes and momentum shifts."
            
            question.contains("volume", ignoreCase = true) ->
                "Volume confirms price movements. High volume on breakouts suggests strong conviction, " +
                "while low volume may indicate weak or false moves."
            
            question.contains("support", ignoreCase = true) || question.contains("resistance", ignoreCase = true) ->
                "Support and resistance are price levels where buying or selling pressure tends to emerge. " +
                "They help identify potential entry and exit points."
            
            else ->
                "I don't have detailed information on that topic yet. For AI-powered explanations, " +
                "download the language model from Settings > AI Features."
        }
    }
    
    /**
     * Weekly summary fallback
     */
    fun getWeeklySummary(totalScans: Int, avgScore: Int): String {
        val quality = when {
            avgScore >= 80 -> "excellent"
            avgScore >= 60 -> "good"
            avgScore >= 40 -> "moderate"
            else -> "mixed"
        }
        
        return "You performed $totalScans scans this week with an average QuantraScore of $avgScore/100 ($quality quality). " +
               "Keep scanning to build your pattern recognition skills and train the learning engine."
    }
}
