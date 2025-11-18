package com.lamontlabs.quantravision.learning.adaptive

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Pattern Learning Engine: Indicator Profile
 * 
 * Stores learned statistical profiles for each pattern type.
 * Captures typical indicator ranges, frequencies, and success patterns.
 * 
 * Example: "Head & Shoulders usually has RSI 65-82, MACD bearish crossover, volume spike"
 */
@Entity(tableName = "pattern_indicator_profiles")
data class PatternIndicatorProfile(
    @PrimaryKey
    val patternName: String,
    
    val totalScans: Int = 0,                    // Number of scans with this pattern
    val lastUpdated: Long = System.currentTimeMillis(),
    
    val indicatorStatsJson: String? = null,     // Statistical profiles for each indicator
    val indicatorFrequencyJson: String? = null, // How often each indicator appears with this pattern
    val typicalRangesJson: String? = null,      // Typical value ranges for each indicator
    val confidenceWeightsJson: String? = null,  // Learned weights for confidence scoring
    
    val avgQuantraScore: Double = 0.0,          // Average QuantraScore for this pattern
    val successRate: Double = 0.0,              // Success rate if outcome data available
    
    val learningPhase: LearningPhase = LearningPhase.BASELINE
) {
    
    enum class LearningPhase {
        BASELINE,        // < 20 scans: collecting data
        LEARNING,        // 20-100 scans: building profiles
        ADAPTIVE,        // 100+ scans: fully adaptive scoring
        EXPERT           // 500+ scans: high confidence profiles
    }
    
    /**
     * Get indicator statistics as map
     */
    fun getIndicatorStats(): Map<String, IndicatorStats>? {
        if (indicatorStatsJson.isNullOrBlank()) return null
        return try {
            val type = object : TypeToken<Map<String, IndicatorStats>>() {}.type
            Gson().fromJson(indicatorStatsJson, type)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Set indicator statistics from map
     */
    fun setIndicatorStats(stats: Map<String, IndicatorStats>): PatternIndicatorProfile {
        return copy(indicatorStatsJson = Gson().toJson(stats))
    }
    
    /**
     * Statistical profile for a single indicator
     */
    data class IndicatorStats(
        val name: String,
        val count: Int = 0,                // Times seen with this pattern
        val frequency: Double = 0.0,       // % of time this indicator appears (0-1)
        val mean: Double = 0.0,            // Average value
        val stdDev: Double = 0.0,          // Standard deviation
        val min: Double = 0.0,             // Minimum seen value
        val max: Double = 0.0,             // Maximum seen value
        val median: Double = 0.0,          // Median value
        val typicalRange: Pair<Double, Double>? = null,  // 25th-75th percentile range
        val correlationScore: Double = 0.0  // How strongly this indicator predicts pattern success
    )
    
    /**
     * Check if profile has enough data to be useful
     */
    fun hasMinimumData(): Boolean = totalScans >= 20
    
    /**
     * Check if profile is in adaptive phase
     */
    fun isAdaptive(): Boolean = learningPhase == LearningPhase.ADAPTIVE || 
                                 learningPhase == LearningPhase.EXPERT
    
    /**
     * Calculate confidence boost/penalty based on current indicators
     * Returns adjustment to apply to QuantraScore (-20 to +20)
     */
    fun calculateAdjustment(currentIndicators: Map<String, Any>): Double {
        if (!isAdaptive()) return 0.0
        
        val stats = getIndicatorStats() ?: return 0.0
        var totalAdjustment = 0.0
        var matchCount = 0
        
        currentIndicators.forEach { (name, value) ->
            val indicatorStat = stats[name]
            if (indicatorStat != null && indicatorStat.frequency > 0.5) {
                // This indicator commonly appears with this pattern
                val numValue = when (value) {
                    is Number -> value.toDouble()
                    is String -> value.toDoubleOrNull()
                    else -> null
                }
                
                if (numValue != null) {
                    // Check if value is within typical range
                    val inRange = indicatorStat.typicalRange?.let { (min, max) ->
                        numValue in min..max
                    } ?: false
                    
                    if (inRange) {
                        // Value matches learned profile - boost score
                        totalAdjustment += indicatorStat.correlationScore * 5.0
                        matchCount++
                    } else {
                        // Value outside typical range - penalize
                        totalAdjustment -= 3.0
                    }
                }
            }
        }
        
        // Return average adjustment, capped at +/- 20
        return if (matchCount > 0) {
            (totalAdjustment / matchCount).coerceIn(-20.0, 20.0)
        } else {
            0.0
        }
    }
}
