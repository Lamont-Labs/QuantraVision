package com.lamontlabs.quantravision.gamification

import android.content.Context
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * UserStats
 * Comprehensive tracking of user activity and progress
 */
data class UserStats(
    val totalDetections: Int = 0,
    val uniquePatternsDetected: Int = 0,
    val highestConfidence: Double = 0.0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastUsedDate: String = "",
    val firstUsedDate: String = "",
    val quizzesCompleted: Int = 0,
    val tutorialsCompleted: Int = 0,
    val totalHighlights: Int = 0,
    val patternsPerDay: Map<String, Int> = emptyMap(),
    val favoritePattern: String = "",
    val nightDetections: Int = 0
) {
    companion object {
        private const val FILE = "user_stats.json"

        fun load(context: Context): UserStats {
            val f = File(context.filesDir, FILE)
            if (!f.exists()) {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                return UserStats(firstUsedDate = today, lastUsedDate = today)
            }

            val json = JSONObject(f.readText())
            val patternsPerDayJson = json.optJSONObject("patternsPerDay") ?: JSONObject()
            val patternsPerDayMap = mutableMapOf<String, Int>()
            patternsPerDayJson.keys().forEach { key ->
                patternsPerDayMap[key] = patternsPerDayJson.getInt(key)
            }

            return UserStats(
                totalDetections = json.optInt("totalDetections", 0),
                uniquePatternsDetected = json.optInt("uniquePatternsDetected", 0),
                highestConfidence = json.optDouble("highestConfidence", 0.0),
                currentStreak = json.optInt("currentStreak", 0),
                longestStreak = json.optInt("longestStreak", 0),
                lastUsedDate = json.optString("lastUsedDate", ""),
                firstUsedDate = json.optString("firstUsedDate", ""),
                quizzesCompleted = json.optInt("quizzesCompleted", 0),
                tutorialsCompleted = json.optInt("tutorialsCompleted", 0),
                totalHighlights = json.optInt("totalHighlights", 0),
                patternsPerDay = patternsPerDayMap,
                favoritePattern = json.optString("favoritePattern", ""),
                nightDetections = json.optInt("nightDetections", 0)
            )
        }

        fun save(context: Context, stats: UserStats) {
            val json = JSONObject()
            json.put("totalDetections", stats.totalDetections)
            json.put("uniquePatternsDetected", stats.uniquePatternsDetected)
            json.put("highestConfidence", stats.highestConfidence)
            json.put("currentStreak", stats.currentStreak)
            json.put("longestStreak", stats.longestStreak)
            json.put("lastUsedDate", stats.lastUsedDate)
            json.put("firstUsedDate", stats.firstUsedDate)
            json.put("quizzesCompleted", stats.quizzesCompleted)
            json.put("tutorialsCompleted", stats.tutorialsCompleted)
            json.put("totalHighlights", stats.totalHighlights)
            json.put("favoritePattern", stats.favoritePattern)
            json.put("nightDetections", stats.nightDetections)

            val patternsPerDayJson = JSONObject()
            stats.patternsPerDay.forEach { (date, count) ->
                patternsPerDayJson.put(date, count)
            }
            json.put("patternsPerDay", patternsPerDayJson)

            File(context.filesDir, FILE).writeText(json.toString(2))
        }

        fun incrementDetection(context: Context, patternName: String, confidence: Double) {
            val stats = load(context)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            
            // Update streak
            val newStreak = if (stats.lastUsedDate == today) {
                stats.currentStreak
            } else if (isYesterday(stats.lastUsedDate, today)) {
                stats.currentStreak + 1
            } else {
                1
            }

            // Track night detections
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val nightDetections = if (hour >= 0 && hour < 6) stats.nightDetections + 1 else stats.nightDetections

            // Update patterns per day
            val updatedPatternsPerDay = stats.patternsPerDay.toMutableMap()
            updatedPatternsPerDay[today] = (updatedPatternsPerDay[today] ?: 0) + 1

            // Calculate favorite pattern
            val patternCounts = mutableMapOf<String, Int>()
            File(context.filesDir, "pattern_history.json").takeIf { it.exists() }?.let { file ->
                val history = JSONObject(file.readText())
                history.keys().forEach { key ->
                    patternCounts[key] = history.getInt(key)
                }
            }
            patternCounts[patternName] = (patternCounts[patternName] ?: 0) + 1
            val favoritePattern = patternCounts.maxByOrNull { it.value }?.key ?: patternName

            // Save updated pattern counts
            val historyJson = JSONObject()
            patternCounts.forEach { (name, count) -> historyJson.put(name, count) }
            File(context.filesDir, "pattern_history.json").writeText(historyJson.toString(2))

            val updated = stats.copy(
                totalDetections = stats.totalDetections + 1,
                uniquePatternsDetected = patternCounts.size,
                highestConfidence = maxOf(stats.highestConfidence, confidence),
                currentStreak = newStreak,
                longestStreak = maxOf(stats.longestStreak, newStreak),
                lastUsedDate = today,
                patternsPerDay = updatedPatternsPerDay,
                favoritePattern = favoritePattern,
                nightDetections = nightDetections
            )

            save(context, updated)

            // Check for achievements
            AchievementSystem.checkAndUnlock(context, updated)
        }

        private fun isYesterday(lastDate: String, today: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val last = sdf.parse(lastDate) ?: return false
                val todayDate = sdf.parse(today) ?: return false
                val diff = (todayDate.time - last.time) / (1000 * 60 * 60 * 24)
                diff == 1L
            } catch (e: Exception) {
                false
            }
        }
    }
}
