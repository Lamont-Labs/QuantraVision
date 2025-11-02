package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.ConfidenceProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class AdaptiveConfidenceEngine(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val learningDao = db.learningProfileDao()
    private val outcomeDao = db.patternOutcomeDao()
    
    private val alpha = 0.2
    
    suspend fun getPersonalizedThreshold(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val profile = learningDao.getConfidenceProfile(patternType)
            
            if (profile == null || profile.totalOutcomes < 10) {
                return@withContext 0.5f
            }
            
            return@withContext profile.recommendedThreshold.toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get personalized threshold for $patternType")
            0.5f
        }
    }
    
    suspend fun learnFromOutcome(
        patternType: String,
        confidence: Double,
        outcome: Outcome
    ) = withContext(Dispatchers.IO) {
        try {
            val profile = learningDao.getConfidenceProfile(patternType) ?: ConfidenceProfile(
                patternType = patternType
            )
            
            val bucketIndex = getConfidenceBucket(confidence)
            val wasWin = outcome == Outcome.WIN
            
            val updatedProfile = updateBucketStats(profile, bucketIndex, wasWin)
            
            val newThreshold = calculateOptimalThreshold(updatedProfile)
            val finalProfile = updatedProfile.copy(
                recommendedThreshold = newThreshold,
                totalOutcomes = updatedProfile.totalOutcomes + 1,
                lastUpdated = System.currentTimeMillis()
            )
            
            learningDao.insertConfidenceProfile(finalProfile)
            
            Timber.d("Updated confidence profile for $patternType: threshold=$newThreshold, total=${finalProfile.totalOutcomes}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to learn from outcome for $patternType")
        }
    }
    
    suspend fun getConfidenceAdjustment(
        patternType: String,
        rawConfidence: Double
    ): Float = withContext(Dispatchers.IO) {
        try {
            val profile = learningDao.getConfidenceProfile(patternType)
            
            if (profile == null || profile.totalOutcomes < 10) {
                return@withContext rawConfidence.toFloat()
            }
            
            val bucketIndex = getConfidenceBucket(rawConfidence)
            val bucketWinRate = getBucketWinRate(profile, bucketIndex)
            
            if (bucketWinRate == 0.0) {
                return@withContext rawConfidence.toFloat()
            }
            
            val adjustment = alpha * (bucketWinRate - 0.5)
            val adjusted = (rawConfidence + adjustment).coerceIn(0.0, 1.0)
            
            return@withContext adjusted.toFloat()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get confidence adjustment for $patternType")
            rawConfidence.toFloat()
        }
    }
    
    private fun getConfidenceBucket(confidence: Double): Int {
        return when {
            confidence < 0.3 -> 0
            confidence < 0.5 -> 1
            confidence < 0.7 -> 2
            confidence < 0.9 -> 3
            else -> 4
        }
    }
    
    private fun updateBucketStats(
        profile: ConfidenceProfile,
        bucketIndex: Int,
        wasWin: Boolean
    ): ConfidenceProfile {
        val oldCount = getBucketCount(profile, bucketIndex)
        val oldWinRate = getBucketWinRate(profile, bucketIndex)
        
        val oldWins = (oldWinRate * oldCount).toInt()
        val newWins = if (wasWin) oldWins + 1 else oldWins
        val newCount = oldCount + 1
        val newWinRate = newWins.toDouble() / newCount
        
        return when (bucketIndex) {
            0 -> profile.copy(
                bucket0_30WinRate = newWinRate,
                bucket0_30Count = newCount
            )
            1 -> profile.copy(
                bucket30_50WinRate = newWinRate,
                bucket30_50Count = newCount
            )
            2 -> profile.copy(
                bucket50_70WinRate = newWinRate,
                bucket50_70Count = newCount
            )
            3 -> profile.copy(
                bucket70_90WinRate = newWinRate,
                bucket70_90Count = newCount
            )
            4 -> profile.copy(
                bucket90_100WinRate = newWinRate,
                bucket90_100Count = newCount
            )
            else -> profile
        }
    }
    
    private fun getBucketWinRate(profile: ConfidenceProfile, bucketIndex: Int): Double {
        return when (bucketIndex) {
            0 -> profile.bucket0_30WinRate
            1 -> profile.bucket30_50WinRate
            2 -> profile.bucket50_70WinRate
            3 -> profile.bucket70_90WinRate
            4 -> profile.bucket90_100WinRate
            else -> 0.0
        }
    }
    
    private fun getBucketCount(profile: ConfidenceProfile, bucketIndex: Int): Int {
        return when (bucketIndex) {
            0 -> profile.bucket0_30Count
            1 -> profile.bucket30_50Count
            2 -> profile.bucket50_70Count
            3 -> profile.bucket70_90Count
            4 -> profile.bucket90_100Count
            else -> 0
        }
    }
    
    private fun calculateOptimalThreshold(profile: ConfidenceProfile): Double {
        val buckets = listOf(
            Triple(0.15, profile.bucket0_30WinRate, profile.bucket0_30Count),
            Triple(0.40, profile.bucket30_50WinRate, profile.bucket30_50Count),
            Triple(0.60, profile.bucket50_70WinRate, profile.bucket50_70Count),
            Triple(0.80, profile.bucket70_90WinRate, profile.bucket70_90Count),
            Triple(0.95, profile.bucket90_100WinRate, profile.bucket90_100Count)
        )
        
        val bestBucket = buckets
            .filter { it.third >= 3 }
            .maxByOrNull { it.second } ?: return 0.5
        
        val targetWinRate = 0.6
        return if (bestBucket.second >= targetWinRate) {
            bestBucket.first * 0.9
        } else {
            (bestBucket.first + 0.1).coerceIn(0.4, 0.7)
        }
    }
}
