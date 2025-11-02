package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.data.MarketConditionOutcomeEntity
import com.lamontlabs.quantravision.learning.advanced.model.ConditionBreakdown
import com.lamontlabs.quantravision.learning.advanced.model.MarketCondition
import com.lamontlabs.quantravision.learning.advanced.model.RecommendationStrength
import com.lamontlabs.quantravision.regime.RegimeNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class MarketConditionLearner(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.advancedLearningDao()
    
    private val minSampleSize = 10
    
    suspend fun trackOutcome(
        patternType: String,
        outcome: Outcome,
        volatility: RegimeNavigator.VolatilityLevel,
        trendStrength: RegimeNavigator.TrendStrength
    ) = withContext(Dispatchers.IO) {
        try {
            val condition = MarketCondition.fromRegime(volatility, trendStrength)
            
            dao.insertMarketConditionOutcome(
                MarketConditionOutcomeEntity(
                    patternType = patternType,
                    marketCondition = condition,
                    outcome = outcome,
                    timestamp = System.currentTimeMillis(),
                    volatilityLevel = volatility.name,
                    trendStrength = trendStrength.name
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to track market condition outcome")
        }
    }
    
    suspend fun getBestPatternsForCondition(condition: MarketCondition): List<String> = withContext(Dispatchers.IO) {
        try {
            val outcomes = dao.getAllOutcomesForCondition(condition)
            
            val patternStats = outcomes.groupBy { it.patternType }
                .mapValues { (_, outcomesList) ->
                    val wins = outcomesList.count { it.outcome == Outcome.WIN }
                    val total = outcomesList.size
                    wins.toFloat() / total
                }
                .filter { (_, winRate) -> winRate >= 0.60 }
            
            patternStats.entries
                .sortedByDescending { it.value }
                .map { it.key }
                .take(5)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best patterns for condition $condition")
            emptyList()
        }
    }
    
    suspend fun getConditionAnalysis(patternType: String): List<ConditionBreakdown> = withContext(Dispatchers.IO) {
        try {
            MarketCondition.values().mapNotNull { condition ->
                val outcomes = dao.getOutcomesByCondition(patternType, condition)
                
                if (outcomes.size < minSampleSize) return@mapNotNull null
                
                val wins = outcomes.count { it.outcome == Outcome.WIN }
                val winRate = wins.toFloat() / outcomes.size
                
                ConditionBreakdown(
                    condition = condition,
                    winRate = winRate,
                    sampleSize = outcomes.size,
                    recommendationStrength = getRecommendationStrength(winRate)
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get condition analysis for $patternType")
            emptyList()
        }
    }
    
    suspend fun getCurrentOptimalPatterns(
        currentVolatility: RegimeNavigator.VolatilityLevel,
        currentTrendStrength: RegimeNavigator.TrendStrength
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val currentCondition = MarketCondition.fromRegime(currentVolatility, currentTrendStrength)
            getBestPatternsForCondition(currentCondition)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current optimal patterns")
            emptyList()
        }
    }
    
    private fun getRecommendationStrength(winRate: Float): RecommendationStrength {
        return when {
            winRate >= 0.75 -> RecommendationStrength.STRONG_BUY
            winRate >= 0.65 -> RecommendationStrength.BUY
            winRate >= 0.45 -> RecommendationStrength.NEUTRAL
            winRate >= 0.35 -> RecommendationStrength.AVOID
            else -> RecommendationStrength.STRONG_AVOID
        }
    }
}
