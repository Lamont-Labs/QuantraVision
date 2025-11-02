package com.lamontlabs.quantravision.ui.screens.learning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.*
import com.lamontlabs.quantravision.learning.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

data class LearningStats(
    val totalFeedbackCount: Int = 0,
    val bestPatterns: List<PatternPerformance> = emptyList(),
    val improvingPatterns: List<PatternPerformance> = emptyList(),
    val needsPractice: List<PatternPerformance> = emptyList(),
    val overallWinRate: Double = 0.0,
    val recentWinRate: Double = 0.0,
    val winRateTrend: List<WinRateDataPoint> = emptyList(),
    val recommendations: List<Recommendation> = emptyList(),
    val difficultyBreakdown: Map<Difficulty, Int> = emptyMap()
)

data class PatternPerformance(
    val patternType: String,
    val winRate: Double,
    val totalOutcomes: Int,
    val difficulty: Difficulty
)

data class WinRateDataPoint(
    val date: String,
    val winRate: Double,
    val outcomeCount: Int
)

class LearningDashboardViewModel(application: Application) : AndroidViewModel(application) {
    
    private val db = PatternDatabase.getInstance(application)
    private val outcomeDao = db.patternOutcomeDao()
    
    private val recommender = SuccessPatternRecommender(application)
    private val difficultyScorer = PatternDifficultyScorer(application)
    private val recommendationEngine = RecommendationEngine(application)
    
    private val _learningStats = MutableStateFlow(LearningStats())
    val learningStats: StateFlow<LearningStats> = _learningStats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        refreshStats()
    }
    
    fun refreshStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val allOutcomes = outcomeDao.getAll()
                val totalFeedback = allOutcomes.size
                
                val patternTypes = allOutcomes.map { it.patternName }.distinct()
                
                val bestPatterns = recommender.getBestPatterns(5).map { rec ->
                    val difficulty = difficultyScorer.getDifficulty(rec.patternType)
                    PatternPerformance(
                        patternType = rec.patternType,
                        winRate = rec.winRate,
                        totalOutcomes = rec.totalOutcomes,
                        difficulty = difficulty
                    )
                }
                
                val improvingPatterns = findImprovingPatterns(patternTypes, allOutcomes)
                
                val needsPractice = findNeedsPracticePatterns(patternTypes, allOutcomes)
                
                val overallWins = allOutcomes.count { it.outcome == Outcome.WIN }
                val overallWinRate = if (totalFeedback > 0) overallWins.toDouble() / totalFeedback else 0.0
                
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                val recentOutcomes = allOutcomes.filter { it.timestamp >= thirtyDaysAgo }
                val recentWins = recentOutcomes.count { it.outcome == Outcome.WIN }
                val recentWinRate = if (recentOutcomes.isNotEmpty()) recentWins.toDouble() / recentOutcomes.size else 0.0
                
                val winRateTrend = calculateWinRateTrend(allOutcomes)
                
                val recommendations = recommendationEngine.getActionableRecommendations()
                
                val difficultyBreakdown = calculateDifficultyBreakdown(patternTypes)
                
                _learningStats.value = LearningStats(
                    totalFeedbackCount = totalFeedback,
                    bestPatterns = bestPatterns,
                    improvingPatterns = improvingPatterns,
                    needsPractice = needsPractice,
                    overallWinRate = overallWinRate,
                    recentWinRate = recentWinRate,
                    winRateTrend = winRateTrend,
                    recommendations = recommendations,
                    difficultyBreakdown = difficultyBreakdown
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh learning stats")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun findImprovingPatterns(
        patternTypes: List<String>,
        allOutcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): List<PatternPerformance> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        
        return patternTypes.mapNotNull { patternType ->
            val outcomes = allOutcomes.filter { it.patternName == patternType }
            if (outcomes.size < 10) return@mapNotNull null
            
            val recentOutcomes = outcomes.filter { it.timestamp >= thirtyDaysAgo }
            val olderOutcomes = outcomes.filter { it.timestamp < thirtyDaysAgo }
            
            if (recentOutcomes.size < 5 || olderOutcomes.size < 5) return@mapNotNull null
            
            val recentWinRate = recentOutcomes.count { it.outcome == Outcome.WIN }.toDouble() / recentOutcomes.size
            val olderWinRate = olderOutcomes.count { it.outcome == Outcome.WIN }.toDouble() / olderOutcomes.size
            
            val improvement = recentWinRate - olderWinRate
            if (improvement <= 0.10) return@mapNotNull null
            
            val difficulty = difficultyScorer.getDifficulty(patternType)
            PatternPerformance(
                patternType = patternType,
                winRate = recentWinRate,
                totalOutcomes = outcomes.size,
                difficulty = difficulty
            )
        }.take(5)
    }
    
    private suspend fun findNeedsPracticePatterns(
        patternTypes: List<String>,
        allOutcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): List<PatternPerformance> {
        return patternTypes.mapNotNull { patternType ->
            val outcomes = allOutcomes.filter { it.patternName == patternType }
            if (outcomes.size < 5) return@mapNotNull null
            
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val winRate = wins.toDouble() / outcomes.size
            
            if (winRate >= 0.50) return@mapNotNull null
            
            val difficulty = difficultyScorer.getDifficulty(patternType)
            PatternPerformance(
                patternType = patternType,
                winRate = winRate,
                totalOutcomes = outcomes.size,
                difficulty = difficulty
            )
        }.sortedBy { it.winRate }.take(5)
    }
    
    private fun calculateWinRateTrend(
        allOutcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): List<WinRateDataPoint> {
        if (allOutcomes.isEmpty()) return emptyList()
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val groupedByDate = allOutcomes.groupBy { outcome ->
            sdf.format(Date(outcome.timestamp))
        }
        
        return groupedByDate.map { (date, outcomes) ->
            val wins = outcomes.count { it.outcome == Outcome.WIN }
            val winRate = wins.toDouble() / outcomes.size
            WinRateDataPoint(
                date = date,
                winRate = winRate,
                outcomeCount = outcomes.size
            )
        }.sortedBy { it.date }.takeLast(30)
    }
    
    private suspend fun calculateDifficultyBreakdown(patternTypes: List<String>): Map<Difficulty, Int> {
        val breakdown = mutableMapOf<Difficulty, Int>()
        
        patternTypes.forEach { patternType ->
            val difficulty = difficultyScorer.getDifficulty(patternType)
            breakdown[difficulty] = breakdown.getOrDefault(difficulty, 0) + 1
        }
        
        return breakdown
    }
    
    fun getTrendAnalysis(): String {
        val stats = _learningStats.value
        
        if (stats.totalFeedbackCount < 10) {
            return "Not enough data for trend analysis. Record more outcomes!"
        }
        
        val trendChange = stats.recentWinRate - stats.overallWinRate
        
        return when {
            trendChange > 0.10 -> "📈 Strong upward trend! You're improving significantly."
            trendChange > 0.05 -> "📈 Positive trend! Keep up the good work."
            trendChange > -0.05 -> "➡️ Stable performance. Consider focusing on specific patterns."
            else -> "📉 Recent dip detected. Review struggling patterns and practice more."
        }
    }
}
