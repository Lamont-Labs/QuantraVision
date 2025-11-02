package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.AlertItem
import com.lamontlabs.quantravision.learning.advanced.model.AlertPriority
import com.lamontlabs.quantravision.learning.advanced.model.Anomaly
import com.lamontlabs.quantravision.learning.advanced.model.AnomalyType
import com.lamontlabs.quantravision.learning.advanced.model.PerformanceShift
import com.lamontlabs.quantravision.learning.advanced.model.WarningSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.sqrt

class AnomalyDetector(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val outcomeDao = db.patternOutcomeDao()
    
    private val zScoreThreshold = 2.5f
    private val minSampleSize = 20
    
    suspend fun detectAnomalies(): List<Anomaly> = withContext(Dispatchers.IO) {
        try {
            val anomalies = mutableListOf<Anomaly>()
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            for (patternType in patternTypes) {
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                
                if (outcomes.size < minSampleSize) continue
                
                anomalies.addAll(detectSuddenChanges(patternType, outcomes))
                anomalies.addAll(detectUnusualStreaks(patternType, outcomes))
            }
            
            anomalies.sortedByDescending { it.severity }
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect anomalies")
            emptyList()
        }
    }
    
    suspend fun isOutlier(outcome: com.lamontlabs.quantravision.analytics.model.PatternOutcome): Boolean = withContext(Dispatchers.IO) {
        try {
            val allOutcomes = outcomeDao.getByPatternType(outcome.patternName)
            
            if (allOutcomes.size < 10) return@withContext false
            
            val profitLoss = outcome.profitLossPercent ?: return@withContext false
            
            val returns = allOutcomes.mapNotNull { it.profitLossPercent }
            val mean = returns.average()
            val variance = returns.map { (it - mean) * (it - mean) }.average()
            val stdDev = sqrt(variance)
            
            val zScore = abs(profitLoss - mean) / stdDev
            
            zScore > zScoreThreshold
        } catch (e: Exception) {
            Timber.e(e, "Failed to check if outcome is outlier")
            false
        }
    }
    
    suspend fun getPerformanceShifts(): List<PerformanceShift> = withContext(Dispatchers.IO) {
        try {
            val shifts = mutableListOf<PerformanceShift>()
            val allOutcomes = outcomeDao.getAll()
            val patternTypes = allOutcomes.map { it.patternName }.distinct()
            
            for (patternType in patternTypes) {
                val outcomes = allOutcomes.filter { it.patternName == patternType }
                    .sortedBy { it.timestamp }
                
                if (outcomes.size < 30) continue
                
                val midpoint = outcomes.size / 2
                val oldOutcomes = outcomes.take(midpoint)
                val newOutcomes = outcomes.drop(midpoint)
                
                val oldWinRate = oldOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / oldOutcomes.size
                val newWinRate = newOutcomes.count { it.outcome == Outcome.WIN }.toFloat() / newOutcomes.size
                
                val changePercent = ((newWinRate - oldWinRate) / oldWinRate) * 100
                
                if (abs(changePercent) > 25) {
                    val reason = when {
                        changePercent > 0 -> "Improved performance - possible learning effect or threshold adjustment"
                        else -> "Declining performance - may need threshold recalibration or pattern review"
                    }
                    
                    shifts.add(
                        PerformanceShift(
                            patternType = patternType,
                            oldWinRate = oldWinRate,
                            newWinRate = newWinRate,
                            changePercent = changePercent,
                            detectionTimestamp = System.currentTimeMillis(),
                            likelyReason = reason
                        )
                    )
                }
            }
            
            shifts
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect performance shifts")
            emptyList()
        }
    }
    
    suspend fun getAttentionRequired(): List<AlertItem> = withContext(Dispatchers.IO) {
        try {
            val alerts = mutableListOf<AlertItem>()
            val anomalies = detectAnomalies()
            val shifts = getPerformanceShifts()
            
            anomalies.forEach { anomaly ->
                val priority = when (anomaly.severity) {
                    WarningSeverity.CRITICAL -> AlertPriority.URGENT
                    WarningSeverity.WARNING -> AlertPriority.HIGH
                    WarningSeverity.INFO -> AlertPriority.MEDIUM
                }
                
                val action = when (anomaly.type) {
                    AnomalyType.SUDDEN_DROP -> "Review pattern settings and recent outcomes"
                    AnomalyType.SUDDEN_IMPROVEMENT -> "Document what changed to maintain improvements"
                    AnomalyType.UNUSUAL_STREAK -> "Verify pattern reliability with more data"
                    AnomalyType.PERFORMANCE_SHIFT -> "Recalibrate confidence thresholds"
                    AnomalyType.OUTLIER_DETECTION -> "Investigate unusual trade conditions"
                }
                
                alerts.add(
                    AlertItem(
                        patternType = anomaly.patternType,
                        priority = priority,
                        message = anomaly.description,
                        actionRecommended = action
                    )
                )
            }
            
            shifts.forEach { shift ->
                val priority = when {
                    abs(shift.changePercent) > 40 -> AlertPriority.URGENT
                    abs(shift.changePercent) > 30 -> AlertPriority.HIGH
                    else -> AlertPriority.MEDIUM
                }
                
                alerts.add(
                    AlertItem(
                        patternType = shift.patternType,
                        priority = priority,
                        message = "Performance shift: ${shift.changePercent.toInt()}% change",
                        actionRecommended = shift.likelyReason
                    )
                )
            }
            
            alerts.sortedByDescending { it.priority }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get attention required")
            emptyList()
        }
    }
    
    private fun detectSuddenChanges(
        patternType: String,
        outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()
        
        if (outcomes.size < 20) return anomalies
        
        val recent = outcomes.takeLast(7)
        val older = outcomes.dropLast(7).takeLast(14)
        
        if (older.isEmpty()) return anomalies
        
        val recentWinRate = recent.count { it.outcome == Outcome.WIN }.toFloat() / recent.size
        val olderWinRate = older.count { it.outcome == Outcome.WIN }.toFloat() / older.size
        
        val change = (recentWinRate - olderWinRate) * 100
        
        if (change < -30) {
            anomalies.add(
                Anomaly(
                    type = AnomalyType.SUDDEN_DROP,
                    patternType = patternType,
                    severity = WarningSeverity.CRITICAL,
                    description = "Sudden drop: $patternType win rate dropped ${abs(change).toInt()}% in last 7 days",
                    timestamp = System.currentTimeMillis(),
                    zScore = abs(change) / 10f
                )
            )
        } else if (change > 30) {
            anomalies.add(
                Anomaly(
                    type = AnomalyType.SUDDEN_IMPROVEMENT,
                    patternType = patternType,
                    severity = WarningSeverity.INFO,
                    description = "Sudden improvement: $patternType improved ${change.toInt()}% in last 7 days",
                    timestamp = System.currentTimeMillis(),
                    zScore = change / 10f
                )
            )
        }
        
        return anomalies
    }
    
    private fun detectUnusualStreaks(
        patternType: String,
        outcomes: List<com.lamontlabs.quantravision.analytics.model.PatternOutcome>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()
        
        val sorted = outcomes.sortedBy { it.timestamp }
        
        var currentStreak = 0
        var currentOutcome: Outcome? = null
        var maxWinStreak = 0
        var maxLossStreak = 0
        
        for (outcome in sorted) {
            if (outcome.outcome == currentOutcome) {
                currentStreak++
            } else {
                if (currentOutcome == Outcome.WIN && currentStreak > maxWinStreak) {
                    maxWinStreak = currentStreak
                } else if (currentOutcome == Outcome.LOSS && currentStreak > maxLossStreak) {
                    maxLossStreak = currentStreak
                }
                currentStreak = 1
                currentOutcome = outcome.outcome
            }
        }
        
        val avgWinRate = outcomes.count { it.outcome == Outcome.WIN }.toFloat() / outcomes.size
        val expectedMaxStreak = -kotlin.math.ln(0.02) / kotlin.math.ln(1 - avgWinRate)
        
        if (maxWinStreak > expectedMaxStreak * 1.5) {
            anomalies.add(
                Anomaly(
                    type = AnomalyType.UNUSUAL_STREAK,
                    patternType = patternType,
                    severity = WarningSeverity.INFO,
                    description = "Unusual streak: $maxWinStreak consecutive wins on $patternType (98th percentile)",
                    timestamp = System.currentTimeMillis(),
                    zScore = maxWinStreak / expectedMaxStreak.toFloat()
                )
            )
        }
        
        return anomalies
    }
}
