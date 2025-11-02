package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.RankedPattern
import com.lamontlabs.quantravision.learning.advanced.model.RiskLevel
import com.lamontlabs.quantravision.learning.advanced.model.RiskMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class RiskAdjustedAnalyzer(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    
    private val minSampleSize = 10
    
    suspend fun getSharpeRatio(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            if (outcomes.size < minSampleSize) return@withContext 0.0f
            
            val returns = outcomes.mapNotNull { it.profitLossPercent?.toFloat() }
            
            if (returns.isEmpty()) {
                val winRate = outcomes.count { it.outcome == Outcome.WIN }.toFloat() / outcomes.size
                return@withContext (winRate - 0.5f) / 0.25f
            }
            
            val avgReturn = returns.average().toFloat()
            val variance = returns.map { (it - avgReturn) * (it - avgReturn) }.average()
            val stdDev = sqrt(variance.toDouble()).toFloat()
            
            if (stdDev == 0.0f) return@withContext 0.0f
            
            avgReturn / stdDev
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate Sharpe ratio for $patternType")
            0.0f
        }
    }
    
    suspend fun getExpectedValue(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            if (outcomes.isEmpty()) return@withContext 0.0f
            
            val wins = outcomes.filter { it.outcome == Outcome.WIN }
            val losses = outcomes.filter { it.outcome == Outcome.LOSS }
            
            val winRate = wins.size.toFloat() / outcomes.size
            val lossRate = losses.size.toFloat() / outcomes.size
            
            val avgWin = wins.mapNotNull { it.profitLossPercent }.average().toFloat()
            val avgLoss = losses.mapNotNull { it.profitLossPercent }.average().toFloat()
            
            if (avgWin.isNaN() || avgLoss.isNaN()) {
                return@withContext winRate - 0.5f
            }
            
            (winRate * avgWin) - (lossRate * kotlin.math.abs(avgLoss))
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate expected value for $patternType")
            0.0f
        }
    }
    
    suspend fun getRiskScore(patternType: String): RiskLevel = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            if (outcomes.size < minSampleSize) return@withContext RiskLevel.MEDIUM
            
            val returns = outcomes.mapNotNull { it.profitLossPercent }
            
            if (returns.isEmpty()) return@withContext RiskLevel.MEDIUM
            
            val avgReturn = returns.average()
            val variance = returns.map { (it - avgReturn) * (it - avgReturn) }.average()
            val volatility = sqrt(variance)
            
            when {
                volatility < 2.0 -> RiskLevel.LOW
                volatility < 5.0 -> RiskLevel.MEDIUM
                else -> RiskLevel.HIGH
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate risk score for $patternType")
            RiskLevel.MEDIUM
        }
    }
    
    suspend fun getBestRiskAdjusted(): List<RankedPattern> = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            patternTypes.mapNotNull { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                if (outcomes.size < minSampleSize) return@mapNotNull null
                
                val sharpe = getSharpeRatio(patternType)
                val expectedValue = getExpectedValue(patternType)
                val winRate = outcomes.count { it.outcome == Outcome.WIN }.toFloat() / outcomes.size
                
                RankedPattern(
                    patternType = patternType,
                    sharpeRatio = sharpe,
                    expectedValue = expectedValue,
                    winRate = winRate,
                    sampleSize = outcomes.size
                )
            }.sortedByDescending { it.sharpeRatio }
                .take(10)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best risk-adjusted patterns")
            emptyList()
        }
    }
    
    suspend fun getRiskMetrics(patternType: String): RiskMetrics? = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            if (outcomes.size < minSampleSize) return@withContext null
            
            val returns = outcomes.mapNotNull { it.profitLossPercent }
            
            if (returns.isEmpty()) return@withContext null
            
            val avgReturn = returns.average()
            val variance = returns.map { (it - avgReturn) * (it - avgReturn) }.average()
            val volatility = sqrt(variance).toFloat()
            
            val drawdown = calculateMaxDrawdown(returns)
            val recovery = calculateRecoveryTime(outcomes)
            
            RiskMetrics(
                sharpeRatio = getSharpeRatio(patternType),
                expectedValue = getExpectedValue(patternType),
                volatility = volatility,
                maxDrawdown = drawdown,
                recoveryTime = recovery
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get risk metrics for $patternType")
            null
        }
    }
    
    private fun calculateMaxDrawdown(returns: List<Double>): Float {
        var peak = 0.0
        var maxDrawdown = 0.0
        var cumulative = 0.0
        
        for (ret in returns) {
            cumulative += ret
            if (cumulative > peak) {
                peak = cumulative
            }
            val drawdown = (peak - cumulative) / peak
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown
            }
        }
        
        return maxDrawdown.toFloat()
    }
    
    private fun calculateRecoveryTime(outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>): Long {
        var inDrawdown = false
        var drawdownStart = 0L
        var maxRecovery = 0L
        
        for (outcome in outcomes.sortedBy { it.timestamp }) {
            if (outcome.outcome == Outcome.LOSS && !inDrawdown) {
                inDrawdown = true
                drawdownStart = outcome.timestamp
            } else if (outcome.outcome == Outcome.WIN && inDrawdown) {
                val recovery = outcome.timestamp - drawdownStart
                if (recovery > maxRecovery) {
                    maxRecovery = recovery
                }
                inDrawdown = false
            }
        }
        
        return maxRecovery
    }
}
