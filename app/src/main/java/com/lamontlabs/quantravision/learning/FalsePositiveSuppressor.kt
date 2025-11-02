package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.SuppressionLevel
import com.lamontlabs.quantravision.learning.model.SuppressionRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class FalsePositiveSuppressor(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val learningDao = db.learningProfileDao()
    private val outcomeDao = db.patternOutcomeDao()
    
    suspend fun shouldSuppress(patternType: String, confidence: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            val rule = learningDao.getSuppressionRule(patternType)
            
            if (rule == null || rule.isUserOverridden) {
                return@withContext false
            }
            
            when (rule.suppressionLevel) {
                SuppressionLevel.NONE -> false
                SuppressionLevel.LOW -> false
                SuppressionLevel.MEDIUM -> confidence < 0.70
                SuppressionLevel.HIGH -> confidence < 0.80
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check suppression for $patternType")
            false
        }
    }
    
    suspend fun getSuppressionScore(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            if (outcomes.isEmpty()) return@withContext 0.0f
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val total = outcomes.size
            val winRate = wins.toDouble() / total
            
            val sampleSizeFactor = sqrt(total.toDouble() / 20.0).coerceAtMost(1.0)
            val suppressionScore = 1.0 - (winRate * sampleSizeFactor)
            
            suppressionScore.toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get suppression score for $patternType")
            0.0f
        }
    }
    
    suspend fun learnFromOutcome(patternType: String, wasCorrect: Boolean) = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            val total = outcomes.size
            
            if (total < 10) return@withContext
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val winRate = wins.toDouble() / total
            
            val (level, reason) = when {
                winRate < 0.20 && total >= 20 -> 
                    SuppressionLevel.HIGH to "Very low success rate (${(winRate * 100).toInt()}%) - consider disabling this pattern"
                winRate < 0.30 && total >= 15 -> 
                    SuppressionLevel.MEDIUM to "Low success rate (${(winRate * 100).toInt()}%) - suppressing low confidence detections"
                winRate < 0.40 && total >= 10 -> 
                    SuppressionLevel.LOW to "Below average success rate (${(winRate * 100).toInt()}%) - use with caution"
                else -> 
                    SuppressionLevel.NONE to "Pattern performing adequately"
            }
            
            val existingRule = learningDao.getSuppressionRule(patternType)
            
            if (existingRule?.isUserOverridden == true) {
                return@withContext
            }
            
            val rule = SuppressionRule(
                patternType = patternType,
                suppressionLevel = level,
                reason = reason,
                winRate = winRate,
                totalOutcomes = total,
                isUserOverridden = false,
                lastUpdated = System.currentTimeMillis()
            )
            
            learningDao.insertSuppressionRule(rule)
            
            Timber.d("Updated suppression rule for $patternType: level=$level, winRate=$winRate")
        } catch (e: Exception) {
            Timber.e(e, "Failed to learn suppression for $patternType")
        }
    }
}
