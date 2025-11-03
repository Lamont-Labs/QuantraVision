package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.PatternRecommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class SuccessPatternRecommender(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    
    suspend fun getBestPatterns(topN: Int = 5): List<PatternRecommendation> = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            val recommendations = patternTypes.mapNotNull { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                if (outcomes.size < 5) return@mapNotNull null
                
                val wins = outcomes.count { it.outcome == Outcome.WIN }
                val total = outcomes.size
                val winRate = wins.toDouble() / total
                
                if (winRate < 0.60) return@mapNotNull null
                
                val avgConfidence = outcomes.mapNotNull { it.profitLossPercent }.average()
                
                PatternRecommendation.create(
                    patternType = patternType,
                    winRate = winRate,
                    totalOutcomes = total,
                    avgConfidence = avgConfidence
                )
            }
            
            // Compute scores for all recommendations before sorting (suspend-safe)
            val recommendationsWithScores = recommendations.map { recommendation ->
                recommendation to getPatternScore(recommendation.patternType)
            }
            
            recommendationsWithScores
                .sortedByDescending { (_, score) -> score }
                .map { (recommendation, _) -> recommendation }
                .take(topN)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best patterns")
            emptyList()
        }
    }
    
    suspend fun getPatternScore(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            if (outcomes.isEmpty()) return@withContext 0.0f
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val total = outcomes.size
            val winRate = wins.toDouble() / total
            
            val sampleSizeBonus = sqrt(total.toDouble() / 20.0).coerceAtMost(1.0)
            
            val score = (winRate * 0.7 + sampleSizeBonus * 0.3)
            score.toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get pattern score for $patternType")
            0.0f
        }
    }
    
    suspend fun shouldRecommend(patternType: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            if (outcomes.size < 10) return@withContext false
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val winRate = wins.toDouble() / outcomes.size
            
            winRate >= 0.60
        } catch (e: Exception) {
            Timber.e(e, "Failed to check if should recommend $patternType")
            false
        }
    }
}
