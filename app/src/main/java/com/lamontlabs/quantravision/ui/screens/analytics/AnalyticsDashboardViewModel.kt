package com.lamontlabs.quantravision.ui.screens.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.analytics.PatternPerformanceTracker
import com.lamontlabs.quantravision.analytics.model.OverallPerformanceStats
import com.lamontlabs.quantravision.analytics.model.WinRateStats
import com.lamontlabs.quantravision.analytics.model.TimeOfDayStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AnalyticsDashboardViewModel(private val context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    sealed class UiState {
        object Loading : UiState()
        data class Success(val stats: OverallPerformanceStats) : UiState()
        data class Error(val message: String) : UiState()
    }
    
    init {
        refreshStats()
    }
    
    fun refreshStats() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                
                val bestPatterns = PatternPerformanceTracker.getBestPerformingPatterns(context, 5)
                val worstPatterns = PatternPerformanceTracker.getWorstPerformingPatterns(context, 5)
                val timeOfDayStats = PatternPerformanceTracker.getTimeOfDayStats(context)
                
                val allStats = PatternPerformanceTracker.getAllStats(context)
                
                val totalOutcomes = bestPatterns.sumOf { it.totalOutcomes } + 
                                   worstPatterns.sumOf { it.totalOutcomes }
                
                val totalWins = bestPatterns.sumOf { it.wins } + 
                               worstPatterns.sumOf { it.wins }
                
                val overallWinRate = if (totalOutcomes > 0) {
                    totalWins.toDouble() / totalOutcomes
                } else 0.0
                
                val frequencyStats = allStats.map { stat ->
                    com.lamontlabs.quantravision.analytics.model.PatternFrequency(
                        patternName = stat.patternName,
                        detectionCount = stat.totalDetections,
                        lastDetected = System.currentTimeMillis(),
                        avgConfidence = stat.avgConfidence,
                        timeframeDistribution = stat.timeframes
                    )
                }
                
                val overallStats = OverallPerformanceStats(
                    totalPatterns = allStats.size,
                    totalOutcomes = totalOutcomes,
                    overallWinRate = overallWinRate,
                    bestPatterns = bestPatterns,
                    worstPatterns = worstPatterns,
                    frequencyStats = frequencyStats,
                    timeOfDayStats = timeOfDayStats
                )
                
                _uiState.value = UiState.Success(overallStats)
                Timber.i("Analytics stats refreshed: ${allStats.size} patterns tracked")
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh analytics stats")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun getWinRateByPattern(patternName: String, onResult: (WinRateStats?) -> Unit) {
        viewModelScope.launch {
            try {
                val winRate = PatternPerformanceTracker.calculateWinRate(context, patternName)
                onResult(winRate)
            } catch (e: Exception) {
                Timber.e(e, "Failed to get win rate for $patternName")
                onResult(null)
            }
        }
    }
}
