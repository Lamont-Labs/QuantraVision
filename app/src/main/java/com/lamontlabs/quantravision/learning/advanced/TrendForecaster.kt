package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.Forecast
import com.lamontlabs.quantravision.learning.advanced.model.TrendDirection
import com.lamontlabs.quantravision.learning.advanced.model.TrendWarning
import com.lamontlabs.quantravision.learning.advanced.model.WarningSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import kotlin.math.sqrt

class TrendForecaster(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    
    private val minDataPoints = 30
    
    suspend fun predictNextWeekPerformance(patternType: String): Forecast? = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
                .sortedBy { it.timestamp }
            
            if (outcomes.size < minDataPoints) return@withContext null
            
            val dailyWinRates = calculateDailyWinRates(outcomes)
            
            if (dailyWinRates.size < 7) return@withContext null
            
            val (slope, intercept) = calculateLinearRegression(dailyWinRates)
            
            val nextWeekDay = dailyWinRates.size + 7
            val predictedWinRate = (slope * nextWeekDay + intercept).toFloat().coerceIn(0.0f, 1.0f)
            
            val standardError = calculateStandardError(dailyWinRates, slope, intercept)
            val confidenceInterval = 1.96f * standardError.toFloat()
            
            val trend = when {
                slope > 0.01 -> TrendDirection.IMPROVING
                slope < -0.01 -> TrendDirection.DECLINING
                else -> TrendDirection.STABLE
            }
            
            Forecast(
                predictedWinRate = predictedWinRate,
                confidenceIntervalUpper = (predictedWinRate + confidenceInterval).coerceIn(0.0f, 1.0f),
                confidenceIntervalLower = (predictedWinRate - confidenceInterval).coerceIn(0.0f, 1.0f),
                trendDirection = trend,
                confidence = (1.0f - standardError.toFloat()).coerceIn(0.0f, 1.0f)
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to predict performance for $patternType")
            null
        }
    }
    
    suspend fun getTrendStrength(patternType: String): TrendDirection = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
                .sortedBy { it.timestamp }
            
            if (outcomes.size < minDataPoints) return@withContext TrendDirection.STABLE
            
            val dailyWinRates = calculateDailyWinRates(outcomes)
            val (slope, _) = calculateLinearRegression(dailyWinRates)
            
            when {
                slope > 0.02 -> TrendDirection.IMPROVING
                slope < -0.02 -> TrendDirection.DECLINING
                else -> TrendDirection.STABLE
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get trend strength for $patternType")
            TrendDirection.STABLE
        }
    }
    
    suspend fun getBreakoutProbability(patternType: String): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
                .sortedBy { it.timestamp }
            
            if (outcomes.size < minDataPoints) return@withContext 0.0f
            
            val dailyWinRates = calculateDailyWinRates(outcomes)
            val ma7 = calculateMovingAverage(dailyWinRates, 7)
            val ma30 = calculateMovingAverage(dailyWinRates, 30)
            
            if (ma7.isEmpty() || ma30.isEmpty()) return@withContext 0.0f
            
            val recentMA7 = ma7.takeLast(3).average()
            val recentMA30 = ma30.takeLast(3).average()
            
            val momentum = recentMA7 - recentMA30
            val volatility = calculateVolatility(dailyWinRates.takeLast(30))
            
            val probability = ((momentum / volatility) * 50 + 50).toFloat().coerceIn(0.0f, 100.0f) / 100
            
            probability
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate breakout probability for $patternType")
            0.0f
        }
    }
    
    suspend fun getWarningSignals(): List<TrendWarning> = withContext(Dispatchers.IO) {
        try {
            val warnings = mutableListOf<TrendWarning>()
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            for (patternType in patternTypes) {
                val trend = getTrendStrength(patternType)
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                if (outcomes.size < 20) continue
                
                val recentOutcomes = outcomes.takeLast(10)
                val olderOutcomes = outcomes.dropLast(10).takeLast(10)
                
                if (olderOutcomes.isEmpty()) continue
                
                val recentWinRate = recentOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / recentOutcomes.size
                val olderWinRate = olderOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / olderOutcomes.size
                
                val change = (recentWinRate - olderWinRate) * 100
                
                if (trend == TrendDirection.DECLINING && change < -20) {
                    warnings.add(
                        TrendWarning(
                            patternType = patternType,
                            severity = WarningSeverity.CRITICAL,
                            message = "$patternType declining: ${change.toInt()}% drop in last 10 trades",
                            currentTrend = trend
                        )
                    )
                } else if (trend == TrendDirection.DECLINING && change < -10) {
                    warnings.add(
                        TrendWarning(
                            patternType = patternType,
                            severity = WarningSeverity.WARNING,
                            message = "$patternType showing weakness: ${change.toInt()}% drop",
                            currentTrend = trend
                        )
                    )
                } else if (trend == TrendDirection.IMPROVING && change > 20) {
                    warnings.add(
                        TrendWarning(
                            patternType = patternType,
                            severity = WarningSeverity.INFO,
                            message = "$patternType improving: ${change.toInt()}% gain in recent trades",
                            currentTrend = trend
                        )
                    )
                }
            }
            
            warnings.sortedByDescending { it.severity }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get warning signals")
            emptyList()
        }
    }
    
    private fun calculateDailyWinRates(outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>): List<Pair<Long, Double>> {
        val calendar = Calendar.getInstance()
        val dailyGroups = outcomes.groupBy { outcome ->
            calendar.timeInMillis = outcome.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        
        return dailyGroups.map { (day, dayOutcomes) ->
            val winRate = dayOutcomes.count { it.outcome == Outcome.WIN }.toDouble() / dayOutcomes.size
            Pair(day, winRate)
        }.sortedBy { it.first }
    }
    
    private fun calculateLinearRegression(data: List<Pair<Long, Double>>): Pair<Double, Double> {
        val n = data.size
        val x = (0 until n).map { it.toDouble() }
        val y = data.map { it.second }
        
        val xMean = x.average()
        val yMean = y.average()
        
        val numerator = x.zip(y) { xi, yi -> (xi - xMean) * (yi - yMean) }.sum()
        val denominator = x.map { (it - xMean) * (it - xMean) }.sum()
        
        val slope = if (denominator != 0.0) numerator / denominator else 0.0
        val intercept = yMean - slope * xMean
        
        return Pair(slope, intercept)
    }
    
    private fun calculateStandardError(
        data: List<Pair<Long, Double>>,
        slope: Double,
        intercept: Double
    ): Double {
        val residuals = data.mapIndexed { index, (_, y) ->
            val predicted = slope * index + intercept
            (y - predicted) * (y - predicted)
        }
        
        val mse = residuals.sum() / data.size
        return sqrt(mse)
    }
    
    private fun calculateMovingAverage(data: List<Pair<Long, Double>>, window: Int): List<Double> {
        if (data.size < window) return emptyList()
        
        return (0..data.size - window).map { i ->
            data.subList(i, i + window).map { it.second }.average()
        }
    }
    
    private fun calculateVolatility(data: List<Pair<Long, Double>>): Double {
        if (data.isEmpty()) return 1.0
        
        val values = data.map { it.second }
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }
}
