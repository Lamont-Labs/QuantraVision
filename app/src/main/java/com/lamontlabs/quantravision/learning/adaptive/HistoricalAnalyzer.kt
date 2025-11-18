package com.lamontlabs.quantravision.learning.adaptive

import com.lamontlabs.quantravision.PatternDao
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import timber.log.Timber
import kotlin.math.sqrt

/**
 * Pattern Learning Engine: Historical Analyzer
 * 
 * Analyzes historical scan data to discover statistical patterns.
 * Identifies typical indicator ranges, frequencies, and correlations for each pattern type.
 */
class HistoricalAnalyzer(
    private val patternDao: PatternDao
) {
    
    companion object {
        private const val TAG = "HistoricalAnalyzer"
        private const val MIN_SAMPLES_FOR_STATS = 10
    }
    
    /**
     * Analyze historical scans for a specific pattern
     * Returns statistical profile of indicators
     */
    suspend fun analyzePattern(patternName: String): Map<String, PatternIndicatorProfile.IndicatorStats>? {
        try {
            // Get all historical matches for this pattern
            val matches = patternDao.getAll().filter { it.patternName == patternName }
            
            if (matches.size < MIN_SAMPLES_FOR_STATS) {
                Timber.d("📊 Not enough data for $patternName (${matches.size} scans, need $MIN_SAMPLES_FOR_STATS)")
                return null
            }
            
            Timber.i("🧠 Analyzing ${matches.size} historical scans for $patternName")
            
            // Extract all indicators from historical matches
            val allIndicators = mutableMapOf<String, MutableList<Double>>()
            
            matches.forEach { match ->
                val context = IndicatorContext.fromJson(match.indicatorsJson)
                if (context != null) {
                    val indicatorMap = context.toUnifiedMap()
                    
                    indicatorMap.forEach { (name, value) ->
                        val numValue = when (value) {
                            is Number -> value.toDouble()
                            is String -> value.toDoubleOrNull()
                            else -> null
                        }
                        
                        if (numValue != null) {
                            allIndicators.getOrPut(name) { mutableListOf() }.add(numValue)
                        }
                    }
                }
            }
            
            // Calculate statistics for each indicator
            val stats = allIndicators.mapValues { (name, values) ->
                calculateIndicatorStats(name, values, matches.size)
            }
            
            Timber.i("✅ Learned ${stats.size} indicator profiles for $patternName")
            return stats
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze pattern: $patternName")
            return null
        }
    }
    
    /**
     * Calculate statistical profile for a single indicator
     */
    private fun calculateIndicatorStats(
        name: String,
        values: List<Double>,
        totalScans: Int
    ): PatternIndicatorProfile.IndicatorStats {
        
        val sortedValues = values.sorted()
        val count = values.size
        val frequency = count.toDouble() / totalScans
        
        // Calculate mean
        val mean = values.average()
        
        // Calculate standard deviation
        val variance = values.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)
        
        // Calculate median
        val median = if (sortedValues.size % 2 == 0) {
            (sortedValues[sortedValues.size / 2 - 1] + sortedValues[sortedValues.size / 2]) / 2.0
        } else {
            sortedValues[sortedValues.size / 2]
        }
        
        // Calculate typical range (25th to 75th percentile)
        val p25Index = (sortedValues.size * 0.25).toInt()
        val p75Index = (sortedValues.size * 0.75).toInt()
        val typicalRange = Pair(
            sortedValues.getOrElse(p25Index) { sortedValues.first() },
            sortedValues.getOrElse(p75Index) { sortedValues.last() }
        )
        
        // Correlation score: how frequently this indicator appears (high frequency = strong signal)
        val correlationScore = frequency.coerceIn(0.0, 1.0)
        
        return PatternIndicatorProfile.IndicatorStats(
            name = name,
            count = count,
            frequency = frequency,
            mean = mean,
            stdDev = stdDev,
            min = sortedValues.first(),
            max = sortedValues.last(),
            median = median,
            typicalRange = typicalRange,
            correlationScore = correlationScore
        )
    }
    
    /**
     * Analyze all patterns at once
     * Returns map of pattern name -> indicator stats
     */
    suspend fun analyzeAllPatterns(): Map<String, Map<String, PatternIndicatorProfile.IndicatorStats>> {
        val allMatches = patternDao.getAll()
        val patternNames = allMatches.map { it.patternName }.distinct()
        
        val results = mutableMapOf<String, Map<String, PatternIndicatorProfile.IndicatorStats>>()
        
        patternNames.forEach { patternName ->
            val stats = analyzePattern(patternName)
            if (stats != null) {
                results[patternName] = stats
            }
        }
        
        Timber.i("🧠 Analyzed ${results.size} patterns with sufficient data")
        return results
    }
}
