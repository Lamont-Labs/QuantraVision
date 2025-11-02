package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.data.StrategyMetricsEntity
import com.lamontlabs.quantravision.learning.advanced.model.PatternPortfolio
import com.lamontlabs.quantravision.learning.advanced.model.PortfolioStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class StrategyLearner(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.advancedLearningDao()
    private val outcomeDao = db.patternOutcomeDao()
    private val riskAnalyzer = RiskAdjustedAnalyzer(context)
    
    private val minSampleSize = 20
    
    suspend fun getBestPortfolio(maxPatterns: Int = 5): PatternPortfolio? = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            val patternPerformance = patternTypes.mapNotNull { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                if (outcomes.size < minSampleSize) return@mapNotNull null
                
                val winRate = outcomes.count { it.outcome == Outcome.WIN }.toFloat() / outcomes.size
                val sharpe = riskAnalyzer.getSharpeRatio(patternType)
                
                Triple(patternType, winRate, sharpe)
            }
            
            if (patternPerformance.size < maxPatterns) return@withContext null
            
            val topPatterns = patternPerformance
                .sortedByDescending { (it.second + it.third) / 2 }
                .take(maxPatterns)
            
            val patterns = topPatterns.map { it.first }
            val totalScore = topPatterns.sumOf { (it.second + it.third).toDouble() }
            
            val allocation = topPatterns.associate { (pattern, winRate, sharpe) ->
                val score = (winRate + sharpe) / totalScore.toFloat()
                pattern to score
            }
            
            val combinedWinRate = topPatterns.map { it.second }.average().toFloat()
            val diversification = calculateDiversification(patterns, allOutcomes)
            
            dao.insertStrategyMetrics(
                StrategyMetricsEntity(
                    portfolioPatterns = patterns.joinToString(","),
                    winRate = combinedWinRate,
                    sharpeRatio = topPatterns.map { it.third }.average().toFloat(),
                    diversification = diversification,
                    sampleSize = topPatterns.sumOf { (pattern, _, _) ->
                        allOutcomes.count { it.patternName == pattern }
                    },
                    lastUpdated = System.currentTimeMillis()
                )
            )
            
            PatternPortfolio(
                patterns = patterns,
                allocation = allocation,
                combinedWinRate = combinedWinRate,
                diversification = diversification
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best portfolio")
            null
        }
    }
    
    suspend fun getComplementaryPatterns(pattern: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct().filter { it != pattern }
            
            val targetOutcomes = allOutcomes.filter { it.patternName == pattern }
            if (targetOutcomes.isEmpty()) return@withContext emptyList()
            
            patternTypes.mapNotNull { otherPattern ->
                val otherOutcomes = allOutcomes.filter { it.patternName == otherPattern }
                
                if (otherOutcomes.size < 10) return@mapNotNull null
                
                val combinedWinRate = calculateCombinedWinRate(targetOutcomes, otherOutcomes)
                val targetWinRate = targetOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / targetOutcomes.size
                val otherWinRate = otherOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / otherOutcomes.size
                
                val synergy = combinedWinRate - ((targetWinRate + otherWinRate) / 2)
                
                if (synergy > 0.05f) Pair(otherPattern, synergy) else null
            }
                .sortedByDescending { it.second }
                .map { it.first }
                .take(5)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get complementary patterns for $pattern")
            emptyList()
        }
    }
    
    suspend fun getDiversificationScore(): Float = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            if (patternTypes.size < 2) return@withContext 0.0f
            
            val patternCounts = patternTypes.map { patternType ->
                allOutcomes.count { it.patternName == patternType }
            }
            
            val totalCount = patternCounts.sum()
            val proportions = patternCounts.map { it.toFloat() / totalCount }
            
            val herfindahl = proportions.sumOf { (it * it).toDouble() }
            val normalizedDiversity = (1.0 - herfindahl) / (1.0 - (1.0 / patternTypes.size))
            
            normalizedDiversity.toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate diversification score")
            0.0f
        }
    }
    
    suspend fun getPortfolioMetrics(): PortfolioStats = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            val winRates = patternTypes.map { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                outcomes.count { it.outcome == Outcome.WIN }.toFloat() / outcomes.size
            }
            
            val sharpeRatios = patternTypes.map { riskAnalyzer.getSharpeRatio(it) }
            val expectedValues = patternTypes.map { riskAnalyzer.getExpectedValue(it) }
            
            PortfolioStats(
                totalPatterns = patternTypes.size,
                avgWinRate = winRates.average().toFloat(),
                diversificationScore = getDiversificationScore(),
                sharpeRatio = sharpeRatios.average().toFloat(),
                expectedValue = expectedValues.average().toFloat()
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get portfolio metrics")
            PortfolioStats(
                totalPatterns = 0,
                avgWinRate = 0.0f,
                diversificationScore = 0.0f,
                sharpeRatio = 0.0f,
                expectedValue = 0.0f
            )
        }
    }
    
    private fun calculateDiversification(
        patterns: List<String>,
        allOutcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): Float {
        val patternCounts = patterns.map { pattern ->
            allOutcomes.count { it.patternName == pattern }
        }
        
        val totalCount = patternCounts.sum()
        if (totalCount == 0) return 0.0f
        
        val proportions = patternCounts.map { it.toFloat() / totalCount }
        val herfindahl = proportions.sumOf { (it * it).toDouble() }
        
        return (1.0f - herfindahl.toFloat()).coerceIn(0.0f, 1.0f)
    }
    
    private fun calculateCombinedWinRate(
        outcomes1: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>,
        outcomes2: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): Float {
        val combined = outcomes1 + outcomes2
        return combined.count { it.outcome == Outcome.WIN }.toFloat() / combined.size
    }
}
