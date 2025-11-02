package com.lamontlabs.quantravision.analytics

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.PatternOutcome
import com.lamontlabs.quantravision.analytics.model.PerformanceStats
import com.lamontlabs.quantravision.analytics.model.TimeOfDayStats
import com.lamontlabs.quantravision.analytics.model.WinRateStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * PatternPerformanceTracker
 * Tracks pattern detection frequency, confidence trends, and accuracy
 * Helps users identify "hot patterns" and reliable signals
 * 
 * ENHANCED: Now supports Room database for outcome tracking and analytics
 */
object PatternPerformanceTracker {

    data class PatternStats(
        val patternName: String,
        val totalDetections: Int,
        val avgConfidence: Double,
        val highestConfidence: Double,
        val lowestConfidence: Double,
        val lastDetected: String,
        val detectionsThisWeek: Int,
        val detectionsThisMonth: Int,
        val timeframes: Map<String, Int>
    )

    data class HotPattern(
        val patternName: String,
        val detectionCount: Int,
        val avgConfidence: Double,
        val trend: String // "rising", "stable", "falling"
    )

    private const val FILE = "pattern_performance.json"

    fun recordDetection(
        context: Context,
        patternName: String,
        confidence: Double,
        timeframe: String
    ) {
        val data = loadData(context)
        val patternData = data.optJSONObject(patternName) ?: JSONObject()

        val count = patternData.optInt("count", 0) + 1
        val totalConf = patternData.optDouble("totalConfidence", 0.0) + confidence
        val highConf = maxOf(patternData.optDouble("highestConfidence", 0.0), confidence)
        val lowConf = if (count == 1) confidence else minOf(patternData.optDouble("lowestConfidence", 1.0), confidence)
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        
        // Update timeframe stats
        val timeframes = patternData.optJSONObject("timeframes") ?: JSONObject()
        timeframes.put(timeframe, timeframes.optInt(timeframe, 0) + 1)

        // Update detection history
        val history = patternData.optJSONArray("history") ?: JSONArray()
        history.put(JSONObject().apply {
            put("date", today)
            put("confidence", confidence)
            put("timeframe", timeframe)
        })

        patternData.apply {
            put("count", count)
            put("totalConfidence", totalConf)
            put("avgConfidence", totalConf / count)
            put("highestConfidence", highConf)
            put("lowestConfidence", lowConf)
            put("lastDetected", today)
            put("timeframes", timeframes)
            put("history", history)
        }

        data.put(patternName, patternData)
        saveData(context, data)
    }

    fun getStats(context: Context, patternName: String): PatternStats? {
        val data = loadData(context)
        val patternData = data.optJSONObject(patternName) ?: return null

        val timeframes = mutableMapOf<String, Int>()
        val timeframesJson = patternData.optJSONObject("timeframes") ?: JSONObject()
        timeframesJson.keys().forEach { key ->
            timeframes[key] = timeframesJson.getInt(key)
        }

        // Calculate week/month detections
        val (weekCount, monthCount) = calculateRecentCounts(patternData)

        return PatternStats(
            patternName = patternName,
            totalDetections = patternData.getInt("count"),
            avgConfidence = patternData.getDouble("avgConfidence"),
            highestConfidence = patternData.getDouble("highestConfidence"),
            lowestConfidence = patternData.getDouble("lowestConfidence"),
            lastDetected = patternData.getString("lastDetected"),
            detectionsThisWeek = weekCount,
            detectionsThisMonth = monthCount,
            timeframes = timeframes
        )
    }

    fun getAllStats(context: Context): List<PatternStats> {
        val data = loadData(context)
        val stats = mutableListOf<PatternStats>()

        data.keys().forEach { patternName ->
            getStats(context, patternName)?.let { stats.add(it) }
        }

        return stats.sortedByDescending { it.totalDetections }
    }

    fun getHotPatterns(context: Context, limit: Int = 5): List<HotPattern> {
        val data = loadData(context)
        val hotPatterns = mutableListOf<HotPattern>()

        data.keys().forEach { patternName ->
            val patternData = data.getJSONObject(patternName)
            val (weekCount, _) = calculateRecentCounts(patternData)
            
            if (weekCount > 0) {
                val trend = calculateTrend(patternData)
                hotPatterns.add(
                    HotPattern(
                        patternName = patternName,
                        detectionCount = weekCount,
                        avgConfidence = patternData.getDouble("avgConfidence"),
                        trend = trend
                    )
                )
            }
        }

        return hotPatterns
            .sortedByDescending { it.detectionCount }
            .take(limit)
    }

    fun getConfidenceTrend(context: Context, patternName: String, days: Int = 30): List<Pair<String, Double>> {
        val data = loadData(context)
        val patternData = data.optJSONObject(patternName) ?: return emptyList()
        val history = patternData.optJSONArray("history") ?: return emptyList()

        val trend = mutableListOf<Pair<String, Double>>()
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in 0 until history.length()) {
            val entry = history.getJSONObject(i)
            val dateStr = entry.getString("date")
            val date = sdf.parse(dateStr) ?: continue
            
            if (date.after(cutoffDate)) {
                trend.add(dateStr to entry.getDouble("confidence"))
            }
        }

        return trend.sortedBy { it.first }
    }

    private fun calculateRecentCounts(patternData: JSONObject): Pair<Int, Int> {
        val history = patternData.optJSONArray("history") ?: return 0 to 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()
        val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        val monthAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }

        var weekCount = 0
        var monthCount = 0

        for (i in 0 until history.length()) {
            val entry = history.getJSONObject(i)
            val dateStr = entry.getString("date")
            val date = sdf.parse(dateStr) ?: continue

            if (date.after(weekAgo.time)) weekCount++
            if (date.after(monthAgo.time)) monthCount++
        }

        return weekCount to monthCount
    }

    private fun calculateTrend(patternData: JSONObject): String {
        val history = patternData.optJSONArray("history") ?: return "stable"
        if (history.length() < 4) return "stable"

        val recent = history.length() - 2
        val older = history.length() - 4

        val recentCount = 2
        val olderCount = 2

        return when {
            recentCount > olderCount * 1.5 -> "rising"
            recentCount < olderCount * 0.5 -> "falling"
            else -> "stable"
        }
    }

    private fun loadData(context: Context): JSONObject {
        val f = File(context.filesDir, FILE)
        return if (f.exists()) JSONObject(f.readText()) else JSONObject()
    }

    private fun saveData(context: Context, obj: JSONObject) {
        File(context.filesDir, FILE).writeText(obj.toString(2))
    }
    
    suspend fun trackOutcome(
        context: Context,
        patternMatchId: Int,
        patternName: String,
        outcome: Outcome,
        userFeedback: String = "",
        profitLossPercent: Double? = null,
        timeframe: String = "unknown"
    ) = withContext(Dispatchers.IO) {
        try {
            val db = PatternDatabase.getInstance(context)
            val patternOutcome = PatternOutcome(
                patternMatchId = patternMatchId,
                patternName = patternName,
                outcome = outcome,
                timestamp = System.currentTimeMillis(),
                userFeedback = userFeedback,
                profitLossPercent = profitLossPercent,
                timeframe = timeframe
            )
            db.patternOutcomeDao().insert(patternOutcome)
            Timber.i("Tracked outcome for $patternName: $outcome")
        } catch (e: Exception) {
            Timber.e(e, "Failed to track outcome")
        }
    }
    
    suspend fun calculateWinRate(context: Context, patternName: String): WinRateStats? = withContext(Dispatchers.IO) {
        try {
            val db = PatternDatabase.getInstance(context)
            val dao = db.patternOutcomeDao()
            
            val wins = dao.getOutcomeCount(patternName, Outcome.WIN)
            val losses = dao.getOutcomeCount(patternName, Outcome.LOSS)
            val neutral = dao.getOutcomeCount(patternName, Outcome.NEUTRAL)
            val total = dao.getTotalCount(patternName)
            
            if (total == 0) return@withContext null
            
            val winRate = wins.toDouble() / total
            val avgProfitLoss = dao.getAvgProfitLoss(patternName) ?: 0.0
            
            WinRateStats(
                patternName = patternName,
                totalOutcomes = total,
                wins = wins,
                losses = losses,
                neutral = neutral,
                winRate = winRate,
                avgProfitLoss = avgProfitLoss
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to calculate win rate for $patternName")
            null
        }
    }
    
    suspend fun getBestPerformingPatterns(context: Context, limit: Int = 5): List<WinRateStats> = withContext(Dispatchers.IO) {
        try {
            val db = PatternDatabase.getInstance(context)
            val outcomes = db.patternOutcomeDao().getAll()
            
            val patternNames = outcomes.map { it.patternName }.distinct()
            val stats = patternNames.mapNotNull { calculateWinRate(context, it) }
            
            stats
                .filter { it.totalOutcomes >= 3 }
                .sortedByDescending { it.winRate }
                .take(limit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best performing patterns")
            emptyList()
        }
    }
    
    suspend fun getWorstPerformingPatterns(context: Context, limit: Int = 5): List<WinRateStats> = withContext(Dispatchers.IO) {
        try {
            val db = PatternDatabase.getInstance(context)
            val outcomes = db.patternOutcomeDao().getAll()
            
            val patternNames = outcomes.map { it.patternName }.distinct()
            val stats = patternNames.mapNotNull { calculateWinRate(context, it) }
            
            stats
                .filter { it.totalOutcomes >= 3 }
                .sortedBy { it.winRate }
                .take(limit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get worst performing patterns")
            emptyList()
        }
    }
    
    suspend fun getTimeOfDayStats(context: Context): List<TimeOfDayStats> = withContext(Dispatchers.IO) {
        try {
            val db = PatternDatabase.getInstance(context)
            val outcomes = db.patternOutcomeDao().getAll()
            
            val calendar = Calendar.getInstance()
            val hourlyStats = mutableMapOf<Int, MutableList<PatternOutcome>>()
            
            outcomes.forEach { outcome ->
                calendar.timeInMillis = outcome.timestamp
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                hourlyStats.getOrPut(hour) { mutableListOf() }.add(outcome)
            }
            
            hourlyStats.map { (hour, outcomesList) ->
                val detectionCount = outcomesList.size
                val wins = outcomesList.count { it.outcome == Outcome.WIN }
                val winRate = if (detectionCount > 0) wins.toDouble() / detectionCount else 0.0
                
                val avgConfidence = outcomesList
                    .mapNotNull { it.profitLossPercent }
                    .average()
                    .let { if (it.isNaN()) 0.0 else it }
                
                TimeOfDayStats(
                    hourOfDay = hour,
                    detectionCount = detectionCount,
                    winRate = winRate,
                    avgConfidence = avgConfidence
                )
            }.sortedBy { it.hourOfDay }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get time of day stats")
            emptyList()
        }
    }
}
