package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.CalibrationResult
import com.lamontlabs.quantravision.learning.advanced.model.ConvergenceStatus
import com.lamontlabs.quantravision.learning.advanced.model.OptimalThreshold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

class GradientDescentCalibrator(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    private val learningDao = db.learningProfileDao()
    
    private val maxIterations = 100
    private val initialLearningRate = 0.01f
    private val convergenceThreshold = 0.001f
    private val minSampleSize = 30
    
    suspend fun optimizeThresholds(): Map<String, OptimalThreshold> = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            patternTypes.mapNotNull { patternType ->
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                if (outcomes.size < minSampleSize) return@mapNotNull null
                
                val optimal = findOptimalThreshold(patternType, outcomes)
                patternType to optimal
            }.toMap()
        } catch (e: Exception) {
            Timber.e(e, "Failed to optimize thresholds")
            emptyMap()
        }
    }
    
    suspend fun calculateLoss(
        threshold: Float,
        outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): Float = withContext(Dispatchers.IO) {
        try {
            val truePositives = outcomes.count {
                it.outcome == Outcome.WIN
            }
            val falsePositives = outcomes.count {
                it.outcome == Outcome.LOSS
            }
            val total = outcomes.size
            
            val truePositiveRate = truePositives.toFloat() / total
            val falsePositiveRate = falsePositives.toFloat() / total
            
            falsePositiveRate + (1.0f - truePositiveRate)
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate loss")
            1.0f
        }
    }
    
    suspend fun performGradientStep(
        patternType: String,
        currentThreshold: Float,
        learningRate: Float
    ): Float = withContext(Dispatchers.IO) {
        try {
            val outcomes = outcomeDao.getByPatternType(patternType)
            
            val epsilon = 0.001f
            val lossPlus = calculateLoss(currentThreshold + epsilon, outcomes)
            val lossMinus = calculateLoss(currentThreshold - epsilon, outcomes)
            
            val gradient = (lossPlus - lossMinus) / (2 * epsilon)
            
            val newThreshold = (currentThreshold - learningRate * gradient).coerceIn(0.3f, 0.9f)
            
            val newLoss = calculateLoss(newThreshold, outcomes)
            newLoss
        } catch (e: Exception) {
            Timber.e(e, "Failed to perform gradient step")
            1.0f
        }
    }
    
    suspend fun getConvergenceStatus(
        patternType: String,
        lossHistory: List<Float>
    ): ConvergenceStatus = withContext(Dispatchers.IO) {
        try {
            if (lossHistory.size < 2) return@withContext ConvergenceStatus.INSUFFICIENT_DATA
            
            val recentLosses = lossHistory.takeLast(5)
            val lossChange = abs(recentLosses.last() - recentLosses.first())
            
            when {
                lossChange < convergenceThreshold -> ConvergenceStatus.CONVERGED
                recentLosses.last() < recentLosses.first() -> ConvergenceStatus.IMPROVING
                else -> ConvergenceStatus.STUCK
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get convergence status")
            ConvergenceStatus.INSUFFICIENT_DATA
        }
    }
    
    suspend fun calibrateAll(): CalibrationResult = withContext(Dispatchers.IO) {
        try {
            val optimalThresholds = optimizeThresholds()
            
            val avgLoss = optimalThresholds.values.map {
                it.falsePositiveRate + (1.0f - it.truePositiveRate)
            }.average().toFloat()
            
            val status = if (optimalThresholds.isEmpty()) {
                ConvergenceStatus.INSUFFICIENT_DATA
            } else {
                ConvergenceStatus.CONVERGED
            }
            
            CalibrationResult(
                finalLoss = avgLoss,
                iterations = maxIterations,
                convergenceStatus = status,
                optimalThresholds = optimalThresholds
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calibrate all patterns")
            CalibrationResult(
                finalLoss = 1.0f,
                iterations = 0,
                convergenceStatus = ConvergenceStatus.INSUFFICIENT_DATA,
                optimalThresholds = emptyMap()
            )
        }
    }
    
    private suspend fun findOptimalThreshold(
        patternType: String,
        outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): OptimalThreshold {
        var threshold = 0.5f
        var learningRate = initialLearningRate
        val lossHistory = mutableListOf<Float>()
        
        repeat(maxIterations) { iteration ->
            val loss = calculateLoss(threshold, outcomes)
            lossHistory.add(loss)
            
            if (lossHistory.size > 1) {
                val lossChange = abs(lossHistory.last() - lossHistory[lossHistory.size - 2])
                if (lossChange < convergenceThreshold) {
                    return@repeat
                }
            }
            
            val epsilon = 0.001f
            val lossPlus = calculateLoss(threshold + epsilon, outcomes)
            val lossMinus = calculateLoss(threshold - epsilon, outcomes)
            
            val gradient = (lossPlus - lossMinus) / (2 * epsilon)
            
            threshold = (threshold - learningRate * gradient).coerceIn(0.3f, 0.9f)
            
            if (iteration > 0 && lossHistory[iteration] > lossHistory[iteration - 1]) {
                learningRate *= 0.5f
            }
        }
        
        val truePositives = outcomes.count { it.outcome == Outcome.WIN }
        val falsePositives = outcomes.count { it.outcome == Outcome.LOSS }
        val total = outcomes.size
        
        val tpr = truePositives.toFloat() / total
        val fpr = falsePositives.toFloat() / total
        val precision = if (truePositives + falsePositives > 0) {
            truePositives.toFloat() / (truePositives + falsePositives)
        } else 0.0f
        
        val f1 = if (precision + tpr > 0) {
            2 * (precision * tpr) / (precision + tpr)
        } else 0.0f
        
        return OptimalThreshold(
            patternType = patternType,
            threshold = threshold,
            truePositiveRate = tpr,
            falsePositiveRate = fpr,
            f1Score = f1
        )
    }
}
