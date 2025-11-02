package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.data.BehavioralEventEntity
import com.lamontlabs.quantravision.learning.advanced.model.BehavioralWarning
import com.lamontlabs.quantravision.learning.advanced.model.BehavioralWarningType
import com.lamontlabs.quantravision.learning.advanced.model.OvertradingAnalysis
import com.lamontlabs.quantravision.learning.advanced.model.RevengePattern
import com.lamontlabs.quantravision.learning.advanced.model.WarningSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds

class BehavioralAnalyzer(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.advancedLearningDao()
    private val outcomeDao = db.patternOutcomeDao()
    
    private val minSampleSize = 10
    
    suspend fun trackEvent(
        sessionId: String,
        patternType: String,
        outcome: Outcome,
        timestamp: Long,
        sessionStartTime: Long,
        patternCountInSession: Int,
        timeSinceLastPattern: Long,
        isAfterLoss: Boolean
    ) = withContext(Dispatchers.IO) {
        try {
            dao.insertBehavioralEvent(
                BehavioralEventEntity(
                    sessionId = sessionId,
                    patternType = patternType,
                    outcome = outcome,
                    timestamp = timestamp,
                    sessionStartTime = sessionStartTime,
                    patternCountInSession = patternCountInSession,
                    timeSinceLastPattern = timeSinceLastPattern,
                    isAfterLoss = isAfterLoss
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to track behavioral event")
        }
    }
    
    suspend fun detectOvertrading(): OvertradingAnalysis = withContext(Dispatchers.IO) {
        try {
            val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            val events = dao.getRecentBehavioralEvents(oneWeekAgo)
            
            if (events.isEmpty()) {
                return@withContext OvertradingAnalysis(
                    patternsPerHour = 0.0f,
                    normalRate = 0.0f,
                    impactOnWinRate = 0.0f,
                    isOvertrading = false
                )
            }
            
            val sessions = events.groupBy { it.sessionId }
            val hourlyRates = sessions.map { (_, sessionEvents) ->
                val sessionDuration = (sessionEvents.maxOf { it.timestamp } - sessionEvents.minOf { it.sessionStartTime }).toFloat()
                val hours = sessionDuration / (60 * 60 * 1000)
                sessionEvents.size / hours
            }
            
            val avgPatternsPerHour = hourlyRates.average().toFloat()
            val normalRate = 4.0f
            
            val highRateSessions = sessions.filter { (_, sessionEvents) ->
                val sessionDuration = (sessionEvents.maxOf { it.timestamp } - sessionEvents.minOf { it.sessionStartTime }).toFloat()
                val hours = sessionDuration / (60 * 60 * 1000)
                (sessionEvents.size / hours) > normalRate * 1.5
            }
            
            val highRateWinRate = highRateSessions.flatMap { it.value }
                .count { it.outcome == Outcome.WIN }.toFloat() / highRateSessions.flatMap { it.value }.size
            
            val normalWinRate = events.count { it.outcome == Outcome.WIN }.toFloat() / events.size
            
            val impact = normalWinRate - highRateWinRate
            
            OvertradingAnalysis(
                patternsPerHour = avgPatternsPerHour,
                normalRate = normalRate,
                impactOnWinRate = impact,
                isOvertrading = avgPatternsPerHour > normalRate * 1.5 && impact > 0.10f
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect overtrading")
            OvertradingAnalysis(
                patternsPerHour = 0.0f,
                normalRate = 0.0f,
                impactOnWinRate = 0.0f,
                isOvertrading = false
            )
        }
    }
    
    suspend fun detectRevengeTrading(): RevengePattern = withContext(Dispatchers.IO) {
        try {
            val oneMonthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            val events = dao.getRecentBehavioralEvents(oneMonthAgo).sortedBy { it.timestamp }
            
            if (events.size < minSampleSize) {
                return@withContext RevengePattern(
                    detectedPostLoss = false,
                    avgTimeToNextTrade = 0.milliseconds,
                    normalTime = 0.milliseconds,
                    postLossWinRate = 0.0f,
                    normalWinRate = 0.0f
                )
            }
            
            var postLossTimeSum = 0L
            var postLossCount = 0
            val postLossOutcomes = mutableListOf<Outcome>()
            
            for (i in 1 until events.size) {
                if (events[i-1].outcome == Outcome.LOSS) {
                    postLossTimeSum += events[i].timestamp - events[i-1].timestamp
                    postLossCount++
                    postLossOutcomes.add(events[i].outcome)
                }
            }
            
            val avgPostLossTime = if (postLossCount > 0) postLossTimeSum / postLossCount else 0L
            val normalAvgTime = events.zipWithNext { a, b -> b.timestamp - a.timestamp }.average().toLong()
            
            val postLossWinRate = if (postLossOutcomes.isNotEmpty())
                postLossOutcomes.count { it == Outcome.WIN }.toFloat() / postLossOutcomes.size
            else 0.0f
            
            val normalWinRate = events.count { it.outcome == Outcome.WIN }.toFloat() / events.size
            
            val detected = avgPostLossTime < normalAvgTime * 0.5 && (normalWinRate - postLossWinRate) > 0.20f
            
            RevengePattern(
                detectedPostLoss = detected,
                avgTimeToNextTrade = avgPostLossTime.milliseconds,
                normalTime = normalAvgTime.milliseconds,
                postLossWinRate = postLossWinRate,
                normalWinRate = normalWinRate
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to detect revenge trading")
            RevengePattern(
                detectedPostLoss = false,
                avgTimeToNextTrade = 0.milliseconds,
                normalTime = 0.milliseconds,
                postLossWinRate = 0.0f,
                normalWinRate = 0.0f
            )
        }
    }
    
    suspend fun getOptimalSessionLength(): Duration = withContext(Dispatchers.IO) {
        try {
            val oneMonthAgo = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L
            val events = dao.getRecentBehavioralEvents(oneMonthAgo)
            
            val sessions = events.groupBy { it.sessionId }
            
            val sessionPerformance = sessions.map { (_, sessionEvents) ->
                val duration = (sessionEvents.maxOf { it.timestamp } - sessionEvents.minOf { it.sessionStartTime })
                val winRate = sessionEvents.count { it.outcome == Outcome.WIN }.toFloat() / sessionEvents.size
                Pair(duration, winRate)
            }
            
            val optimal = sessionPerformance
                .filter { it.first > 0 }
                .groupBy { it.first / (60 * 60 * 1000) }
                .mapValues { (_, pairs) -> pairs.map { it.second }.average() }
                .maxByOrNull { it.value }
            
            (optimal?.key?.toLong() ?: 2L).hours
        } catch (e: Exception) {
            Timber.e(e, "Failed to get optimal session length")
            2.5.hours
        }
    }
    
    suspend fun getBehavioralWarnings(): List<BehavioralWarning> = withContext(Dispatchers.IO) {
        try {
            val warnings = mutableListOf<BehavioralWarning>()
            
            val overtrading = detectOvertrading()
            if (overtrading.isOvertrading) {
                warnings.add(
                    BehavioralWarning(
                        type = BehavioralWarningType.OVERTRADING,
                        severity = WarningSeverity.WARNING,
                        message = "Taking too many patterns (${overtrading.patternsPerHour.toInt()}/hour) - success rate drops ${(overtrading.impactOnWinRate * 100).toInt()}% when rushed",
                        recommendation = "Slow down to ${overtrading.normalRate.toInt()} patterns per hour for better results"
                    )
                )
            }
            
            val revenge = detectRevengeTrading()
            if (revenge.detectedPostLoss) {
                val dropPercent = ((revenge.normalWinRate - revenge.postLossWinRate) * 100).toInt()
                warnings.add(
                    BehavioralWarning(
                        type = BehavioralWarningType.REVENGE_TRADING,
                        severity = WarningSeverity.CRITICAL,
                        message = "Win rate $dropPercent% lower after losses - detected revenge trading pattern",
                        recommendation = "Take a break after losses - wait at least ${revenge.normalTime.inWholeMinutes} minutes"
                    )
                )
            }
            
            val optimalLength = getOptimalSessionLength()
            warnings.add(
                BehavioralWarning(
                    type = BehavioralWarningType.FATIGUE,
                    severity = WarningSeverity.INFO,
                    message = "Optimal session length: ${optimalLength.inWholeHours} hours",
                    recommendation = "Take breaks every ${optimalLength.inWholeHours} hours to maintain peak performance"
                )
            )
            
            warnings
        } catch (e: Exception) {
            Timber.e(e, "Failed to get behavioral warnings")
            emptyList()
        }
    }
}
