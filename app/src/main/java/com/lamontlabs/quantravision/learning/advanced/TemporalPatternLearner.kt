package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.data.TemporalDataEntity
import com.lamontlabs.quantravision.learning.advanced.model.HeatmapCell
import com.lamontlabs.quantravision.learning.advanced.model.HeatmapData
import com.lamontlabs.quantravision.learning.advanced.model.TemporalInsight
import com.lamontlabs.quantravision.learning.advanced.model.TimeRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import kotlin.math.sqrt

class TemporalPatternLearner(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.advancedLearningDao()
    
    private val minSampleSize = 5
    
    suspend fun trackOutcome(
        patternType: String,
        outcome: Outcome,
        timestamp: Long
    ) = withContext(Dispatchers.IO) {
        try {
            val instant = Instant.ofEpochMilli(timestamp)
            val zonedDateTime = instant.atZone(ZoneId.systemDefault())
            
            dao.insertTemporalData(
                TemporalDataEntity(
                    patternType = patternType,
                    hourOfDay = zonedDateTime.hour,
                    dayOfWeek = zonedDateTime.dayOfWeek.value,
                    outcome = outcome,
                    timestamp = timestamp
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to track temporal outcome")
        }
    }
    
    suspend fun getBestTimeOfDay(patternType: String): TimeRange? = withContext(Dispatchers.IO) {
        try {
            val data = dao.getTemporalData(patternType)
            
            if (data.size < minSampleSize) return@withContext null
            
            val hourlyStats = (0..23).map { hour ->
                val hourData = data.filter { it.hourOfDay == hour }
                if (hourData.isEmpty()) return@map Pair(hour, 0.0f)
                
                val wins = hourData.count { it.outcome == Outcome.WIN }
                Pair(hour, wins.toFloat() / hourData.size)
            }
            
            val bestHours = hourlyStats
                .filter { (_, winRate) -> winRate >= 0.60 }
                .sortedByDescending { it.second }
            
            if (bestHours.isEmpty()) return@withContext null
            
            val topHour = bestHours.first().first
            TimeRange(topHour, (topHour + 1) % 24)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best time of day for $patternType")
            null
        }
    }
    
    suspend fun getBestDayOfWeek(patternType: String): DayOfWeek? = withContext(Dispatchers.IO) {
        try {
            val data = dao.getTemporalData(patternType)
            
            if (data.size < minSampleSize) return@withContext null
            
            val dailyStats = DayOfWeek.values().map { day ->
                val dayData = data.filter { it.dayOfWeek == day.value }
                if (dayData.isEmpty()) return@map Pair(day, 0.0f)
                
                val wins = dayData.count { it.outcome == Outcome.WIN }
                Pair(day, wins.toFloat() / dayData.size)
            }
            
            dailyStats.maxByOrNull { it.second }?.first
        } catch (e: Exception) {
            Timber.e(e, "Failed to get best day of week for $patternType")
            null
        }
    }
    
    suspend fun getTemporalHeatmap(patternType: String): HeatmapData = withContext(Dispatchers.IO) {
        try {
            val data = dao.getTemporalData(patternType)
            
            val grid = mutableMapOf<Int, MutableMap<DayOfWeek, HeatmapCell>>()
            
            for (hour in 0..23) {
                for (day in DayOfWeek.values()) {
                    val cellData = data.filter {
                        it.hourOfDay == hour && it.dayOfWeek == day.value
                    }
                    
                    if (cellData.isNotEmpty()) {
                        val wins = cellData.count { it.outcome == Outcome.WIN }
                        val winRate = wins.toFloat() / cellData.size
                        val isSignificant = isStatisticallySignificant(patternType, hour, day)
                        
                        grid.getOrPut(hour) { mutableMapOf() }[day] = HeatmapCell(
                            winRate = winRate,
                            sampleSize = cellData.size,
                            isSignificant = isSignificant
                        )
                    }
                }
            }
            
            HeatmapData(grid)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get temporal heatmap for $patternType")
            HeatmapData(emptyMap())
        }
    }
    
    suspend fun isStatisticallySignificant(
        patternType: String,
        hour: Int,
        day: DayOfWeek
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val allData = dao.getTemporalData(patternType)
            val bucketData = allData.filter { it.hourOfDay == hour && it.dayOfWeek == day.value }
            
            if (bucketData.size < minSampleSize) return@withContext false
            
            val bucketWins = bucketData.count { it.outcome == Outcome.WIN }
            val bucketTotal = bucketData.size
            val bucketWinRate = bucketWins.toFloat() / bucketTotal
            
            val overallWins = allData.count { it.outcome == Outcome.WIN }
            val overallTotal = allData.size
            val overallWinRate = overallWins.toFloat() / overallTotal
            
            val chiSquared = calculateChiSquared(
                bucketWins, bucketTotal,
                overallWinRate
            )
            
            chiSquared > 3.84
        } catch (e: Exception) {
            Timber.e(e, "Failed to check statistical significance")
            false
        }
    }
    
    private fun calculateChiSquared(
        observed: Int,
        total: Int,
        expectedRate: Float
    ): Float {
        val expected = total * expectedRate
        val observedLosses = total - observed
        val expectedLosses = total * (1 - expectedRate)
        
        val chiWins = ((observed - expected) * (observed - expected)) / expected
        val chiLosses = ((observedLosses - expectedLosses) * (observedLosses - expectedLosses)) / expectedLosses
        
        return chiWins + chiLosses
    }
}
