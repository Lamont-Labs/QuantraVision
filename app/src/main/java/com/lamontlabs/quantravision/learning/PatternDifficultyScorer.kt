package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.Difficulty
import com.lamontlabs.quantravision.learning.model.DifficultyDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class PatternDifficultyScorer(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    
    suspend fun getDifficulty(patternType: String): Difficulty = withContext(Dispatchers.IO) {
        try {
            val details = getDifficultyDetails(patternType)
            details.difficulty
        } catch (e: Exception) {
            Timber.e(e, "Failed to get difficulty for $patternType")
            Difficulty.UNKNOWN
        }
    }
    
    suspend fun getDifficultyDetails(patternType: String): DifficultyDetails = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            if (outcomes.size < 5) {
                return@withContext DifficultyDetails(
                    patternType = patternType,
                    difficulty = Difficulty.UNKNOWN,
                    winRate = 0.0,
                    consistency = 0.0,
                    sampleSize = outcomes.size,
                    recommendedAction = "Not enough data yet. Continue practicing to get personalized insights."
                )
            }
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val winRate = wins.toDouble() / outcomes.size
            
            val outcomeList = outcomes.map { it.outcome == Outcome.WIN }
            
            DifficultyDetails.calculate(
                patternType = patternType,
                winRate = winRate,
                outcomes = outcomeList,
                sampleSize = outcomes.size
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get difficulty details for $patternType")
            DifficultyDetails(
                patternType = patternType,
                difficulty = Difficulty.UNKNOWN,
                winRate = 0.0,
                consistency = 0.0,
                sampleSize = 0,
                recommendedAction = "Unable to calculate difficulty. Please try again later."
            )
        }
    }
    
    suspend fun getAllDifficulties(): Map<String, Difficulty> = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            patternTypes.associateWith { patternType ->
                getDifficulty(patternType)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get all difficulties")
            emptyMap()
        }
    }
}
