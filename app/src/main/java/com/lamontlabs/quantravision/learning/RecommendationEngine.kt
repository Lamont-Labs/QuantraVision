package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

data class Recommendation(
    val title: String,
    val message: String,
    val type: RecommendationType,
    val patternTypes: List<String> = emptyList(),
    val actionable: Boolean = true
)

enum class RecommendationType {
    SUCCESS,       
    IMPROVEMENT,   
    WARNING,       
    PROGRESS       
}

class RecommendationEngine(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    private val recommender = SuccessPatternRecommender(context)
    private val suppressor = FalsePositiveSuppressor(context)
    private val difficultyScorer = PatternDifficultyScorer(context)
    
    suspend fun getActionableRecommendations(): List<Recommendation> = withContext(Dispatchers.IO) {
        try {
            val recommendations = mutableListOf<Recommendation>()
            
            val bestPatterns = recommender.getBestPatterns(3)
            if (bestPatterns.isNotEmpty()) {
                val patternNames = bestPatterns.joinToString(", ") { it.patternType }
                val avgWinRate = bestPatterns.map { it.winRate }.average()
                recommendations.add(
                    Recommendation(
                        title = "⭐ Your Best Patterns",
                        message = "Focus on $patternNames - you have ${(avgWinRate * 100).toInt()}% success rate! (educational data only)",
                        type = RecommendationType.SUCCESS,
                        patternTypes = bestPatterns.map { it.patternType }
                    )
                )
            }
            
            val improvementPatterns = findImprovementOpportunities()
            if (improvementPatterns.isNotEmpty()) {
                recommendations.add(
                    Recommendation(
                        title = "📈 Practice Opportunities",
                        message = "Practice ${improvementPatterns.joinToString(", ")} - medium difficulty patterns worth improving (educational).",
                        type = RecommendationType.IMPROVEMENT,
                        patternTypes = improvementPatterns
                    )
                )
            }
            
            val weakPatterns = findWeakPatterns()
            if (weakPatterns.isNotEmpty()) {
                recommendations.add(
                    Recommendation(
                        title = "⚠️ Consider Avoiding",
                        message = "Avoid ${weakPatterns.joinToString(", ")} below 75% confidence - low success rate in educational practice.",
                        type = RecommendationType.WARNING,
                        patternTypes = weakPatterns
                    )
                )
            }
            
            val progressRec = getProgressRecommendation()
            if (progressRec != null) {
                recommendations.add(progressRec)
            }
            
            recommendations
        } catch (e: Exception) {
            Timber.e(e, "Failed to get actionable recommendations")
            emptyList()
        }
    }
    
    private suspend fun findImprovementOpportunities(): List<String> {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            return patternTypes.filter { patternType ->
                val difficulty = difficultyScorer.getDifficulty(patternType)
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                difficulty == Difficulty.MEDIUM && outcomes.size >= 5
            }.take(3)
        } catch (e: Exception) {
            Timber.e(e, "Failed to find improvement opportunities")
            return emptyList()
        }
    }
    
    private suspend fun findWeakPatterns(): List<String> {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            return patternTypes.filter { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                if (outcomes.size < 10) return@filter false
                
                val wins = outcomes.count { it.outcome == Outcome.WIN }
                val winRate = wins.toDouble() / outcomes.size
                
                winRate < 0.40
            }.take(3)
        } catch (e: Exception) {
            Timber.e(e, "Failed to find weak patterns")
            return emptyList()
        }
    }
    
    private suspend fun getProgressRecommendation(): Recommendation? {
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val recentOutcomes = outcomeDao.getByDateRange(thirtyDaysAgo, System.currentTimeMillis())
            
            if (recentOutcomes.size < 10) {
                return Recommendation(
                    title = "📚 Keep Learning",
                    message = "You have ${recentOutcomes.size} practice outcomes recorded. Keep going to unlock personalized insights!",
                    type = RecommendationType.PROGRESS,
                    actionable = false
                )
            }
            
            val recentWins = recentOutcomes.count { it.outcome == Outcome.WIN }
            val recentWinRate = recentWins.toDouble() / recentOutcomes.size
            
            val sixtyDaysAgo = System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000)
            val olderOutcomes = outcomeDao.getByDateRange(sixtyDaysAgo, thirtyDaysAgo)
            
            if (olderOutcomes.size >= 10) {
                val olderWins = olderOutcomes.count { it.outcome == Outcome.WIN }
                val olderWinRate = olderWins.toDouble() / olderOutcomes.size
                
                val improvement = recentWinRate - olderWinRate
                if (improvement > 0.10) {
                    return Recommendation(
                        title = "🎉 You're Improving!",
                        message = "Win rate up ${(improvement * 100).toInt()}% this month! Great progress in your educational practice.",
                        type = RecommendationType.PROGRESS,
                        actionable = false
                    )
                }
            }
            
            return Recommendation(
                title = "📊 Good Progress",
                message = "You have ${recentOutcomes.size} practice outcomes this month with ${(recentWinRate * 100).toInt()}% success rate (educational).",
                type = RecommendationType.PROGRESS,
                actionable = false
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get progress recommendation")
            return null
        }
    }
}
