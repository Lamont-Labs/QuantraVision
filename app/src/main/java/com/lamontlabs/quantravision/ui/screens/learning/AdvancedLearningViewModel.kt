package com.lamontlabs.quantravision.ui.screens.learning

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.learning.advanced.*
import com.lamontlabs.quantravision.learning.advanced.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class AdvancedLearningViewModel : ViewModel() {
    
    private val _portfolioStats = MutableStateFlow<PortfolioStats?>(null)
    val portfolioStats: StateFlow<PortfolioStats?> = _portfolioStats.asStateFlow()
    
    private val _bestRiskAdjusted = MutableStateFlow<List<RankedPattern>>(emptyList())
    val bestRiskAdjusted: StateFlow<List<RankedPattern>> = _bestRiskAdjusted.asStateFlow()
    
    private val _behavioralWarnings = MutableStateFlow<List<BehavioralWarning>>(emptyList())
    val behavioralWarnings: StateFlow<List<BehavioralWarning>> = _behavioralWarnings.asStateFlow()
    
    private val _bestPortfolio = MutableStateFlow<PatternPortfolio?>(null)
    val bestPortfolio: StateFlow<PatternPortfolio?> = _bestPortfolio.asStateFlow()
    
    private val _trendWarnings = MutableStateFlow<List<TrendWarning>>(emptyList())
    val trendWarnings: StateFlow<List<TrendWarning>> = _trendWarnings.asStateFlow()
    
    private val _anomalies = MutableStateFlow<List<Anomaly>>(emptyList())
    val anomalies: StateFlow<List<Anomaly>> = _anomalies.asStateFlow()
    
    fun loadAnalytics(context: Context) {
        viewModelScope.launch {
            try {
                val strategyLearner = StrategyLearner(context)
                val riskAnalyzer = RiskAdjustedAnalyzer(context)
                val behavioralAnalyzer = BehavioralAnalyzer(context)
                val trendForecaster = TrendForecaster(context)
                val anomalyDetector = AnomalyDetector(context)
                
                _portfolioStats.value = strategyLearner.getPortfolioMetrics()
                _bestRiskAdjusted.value = riskAnalyzer.getBestRiskAdjusted()
                _behavioralWarnings.value = behavioralAnalyzer.getBehavioralWarnings()
                _bestPortfolio.value = strategyLearner.getBestPortfolio(5)
                _trendWarnings.value = trendForecaster.getWarningSignals()
                _anomalies.value = anomalyDetector.detectAnomalies()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load advanced analytics")
            }
        }
    }
    
    suspend fun generateReport(context: Context) {
        try {
            val reportGenerator = ReportGenerator(context)
            val reportFile = reportGenerator.generateWeeklyReport()
            Timber.i("Report generated: ${reportFile.absolutePath}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate report")
        }
    }
}
