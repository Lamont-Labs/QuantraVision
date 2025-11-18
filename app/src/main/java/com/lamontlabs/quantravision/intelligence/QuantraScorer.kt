package com.lamontlabs.quantravision.intelligence

import timber.log.Timber
import kotlin.math.roundToInt

/**
 * QuantraCore: QuantraScore Calculation Engine
 * 
 * Calculates a 0-100 composite quality score for each pattern detection.
 * Combines pattern confidence, indicator confluence, and signal strength.
 * 
 * Score Breakdown:
 * - 0-39: Low quality, filter out
 * - 40-59: Fair quality, review carefully
 * - 60-79: Good quality, tradeable
 * - 80-89: High quality, strong signal
 * - 90-100: Exceptional quality, very high probability
 * 
 * Formula:
 * QuantraScore = (PatternConfidence * ConfluenceBoost) * 100
 *              + BonusPoints (signal strength, multiple indicators)
 *              - Penalties (conflicts, missing indicators)
 */
class QuantraScorer {
    
    companion object {
        private const val TAG = "QuantraScorer"
        
        // Score thresholds
        const val SCORE_MIN = 0
        const val SCORE_MAX = 100
        const val THRESHOLD_LOW = 40
        const val THRESHOLD_FAIR = 60
        const val THRESHOLD_GOOD = 80
        const val THRESHOLD_EXCEPTIONAL = 90
        
        // Bonus points
        private const val BONUS_STRONG_CONFLUENCE = 10.0
        private const val BONUS_MODERATE_CONFLUENCE = 5.0
        private const val BONUS_MULTIPLE_INDICATORS = 3.0
        
        // Penalties
        private const val PENALTY_CONFLICTING_SIGNALS = -15.0
        private const val PENALTY_NO_INDICATORS = -10.0
    }
    
    /**
     * Score result with breakdown
     */
    data class ScoreResult(
        val quantraScore: Int,              // 0-100 final score
        val baseScore: Double,              // Pattern confidence * 100
        val confluenceBoost: Double,        // Multiplier from confluence (0.8-1.3)
        val bonusPoints: Double,            // Bonus from signal strength
        val penalties: Double,              // Deductions from conflicts
        val grade: ScoreGrade,              // Letter grade
        val reasoning: String               // Human-readable explanation
    )
    
    enum class ScoreGrade(val label: String, val emoji: String) {
        EXCEPTIONAL("Exceptional", "🔥"),
        HIGH("High Quality", "⭐"),
        GOOD("Good", "✅"),
        FAIR("Fair", "⚠️"),
        LOW("Low Quality", "❌")
    }
    
    /**
     * Calculate QuantraScore for a pattern detection
     */
    fun calculateScore(
        patternName: String,
        patternConfidence: Double,
        indicators: IndicatorContext,
        analysis: ContextAnalyzer.AnalysisResult
    ): ScoreResult {
        
        // Base score from pattern confidence (0-100)
        val baseScore = patternConfidence * 100.0
        
        // Apply confluence boost
        val boostedScore = baseScore * analysis.confidenceBoost
        
        // Calculate bonus points
        var bonusPoints = 0.0
        when (analysis.signalStrength) {
            ContextAnalyzer.SignalStrength.STRONG -> 
                bonusPoints += BONUS_STRONG_CONFLUENCE
            ContextAnalyzer.SignalStrength.MODERATE -> 
                bonusPoints += BONUS_MODERATE_CONFLUENCE
            else -> {}
        }
        
        // Bonus for multiple indicators
        val indicatorCount = listOfNotNull(
            indicators.rsi,
            indicators.macd,
            indicators.volume
        ).size
        if (indicatorCount >= 2) {
            bonusPoints += BONUS_MULTIPLE_INDICATORS
        }
        
        // Calculate penalties
        var penalties = 0.0
        when (analysis.confluenceType) {
            ContextAnalyzer.ConfluenceType.CONFLICTING -> 
                penalties += PENALTY_CONFLICTING_SIGNALS
            else -> {}
        }
        
        if (!indicators.hasAnyIndicators()) {
            penalties += PENALTY_NO_INDICATORS
        }
        
        // Final score calculation
        val rawScore = boostedScore + bonusPoints + penalties
        val finalScore = rawScore.coerceIn(SCORE_MIN.toDouble(), SCORE_MAX.toDouble()).roundToInt()
        
        // Determine grade
        val grade = when {
            finalScore >= THRESHOLD_EXCEPTIONAL -> ScoreGrade.EXCEPTIONAL
            finalScore >= THRESHOLD_GOOD -> ScoreGrade.HIGH
            finalScore >= THRESHOLD_FAIR -> ScoreGrade.GOOD
            finalScore >= THRESHOLD_LOW -> ScoreGrade.FAIR
            else -> ScoreGrade.LOW
        }
        
        // Build reasoning
        val reasoning = buildReasoning(
            patternName, 
            patternConfidence, 
            analysis, 
            indicatorCount,
            finalScore
        )
        
        val result = ScoreResult(
            quantraScore = finalScore,
            baseScore = baseScore,
            confluenceBoost = analysis.confidenceBoost,
            bonusPoints = bonusPoints,
            penalties = penalties,
            grade = grade,
            reasoning = reasoning
        )
        
        Timber.i("QuantraScore: $finalScore/100 (${grade.label}) - $patternName")
        return result
    }
    
    /**
     * Build human-readable reasoning for score
     */
    private fun buildReasoning(
        patternName: String,
        patternConfidence: Double,
        analysis: ContextAnalyzer.AnalysisResult,
        indicatorCount: Int,
        finalScore: Int
    ): String {
        val parts = mutableListOf<String>()
        
        // Pattern confidence
        val confPercent = (patternConfidence * 100).roundToInt()
        parts.add("Pattern: $confPercent%")
        
        // Confluence
        when (analysis.signalStrength) {
            ContextAnalyzer.SignalStrength.STRONG -> 
                parts.add("Strong confluence")
            ContextAnalyzer.SignalStrength.MODERATE -> 
                parts.add("Moderate confluence")
            ContextAnalyzer.SignalStrength.WEAK -> 
                parts.add("Weak confluence")
            ContextAnalyzer.SignalStrength.INSUFFICIENT -> 
                parts.add("No indicator data")
        }
        
        // Supporting factors
        if (analysis.supportingFactors.isNotEmpty()) {
            parts.add(analysis.supportingFactors.joinToString(", "))
        }
        
        // Conflicts
        if (analysis.conflictingFactors.isNotEmpty()) {
            parts.add("⚠️ ${analysis.conflictingFactors.joinToString(", ")}")
        }
        
        return parts.joinToString(" | ")
    }
    
    /**
     * Quick score without full analysis (fallback)
     */
    fun quickScore(patternConfidence: Double): Int {
        return (patternConfidence * 100).roundToInt().coerceIn(SCORE_MIN, SCORE_MAX)
    }
    
    /**
     * Check if score meets minimum threshold for alerts
     */
    fun meetsThreshold(score: Int, threshold: Int = THRESHOLD_FAIR): Boolean {
        return score >= threshold
    }
}
