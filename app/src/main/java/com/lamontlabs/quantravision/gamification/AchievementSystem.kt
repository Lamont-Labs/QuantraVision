package com.lamontlabs.quantravision.gamification

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * AchievementSystem
 * Tracks user achievements, badges, and milestones
 * Rewards users with bonus highlights and engagement
 */
object AchievementSystem {

    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val icon: String,
        val reward: Int, // bonus highlights
        val unlocked: Boolean,
        val unlockedDate: String?
    )

    private const val FILE = "achievements.json"

    private val allAchievements = listOf(
        Achievement("first_detection", "First Detection", "Detect your first pattern", "🎯", 1, false, null),
        Achievement("pattern_master_10", "Pattern Explorer", "Detect 10 different patterns", "🔍", 2, false, null),
        Achievement("pattern_master_50", "Pattern Expert", "Detect 50 different patterns", "🏆", 5, false, null),
        Achievement("streak_3", "Three-Day Streak", "Use the app 3 days in a row", "🔥", 2, false, null),
        Achievement("streak_7", "Weekly Warrior", "Use the app 7 days in a row", "⚡", 5, false, null),
        Achievement("streak_30", "Monthly Master", "Use the app 30 days in a row", "👑", 10, false, null),
        Achievement("detections_100", "Century Club", "Reach 100 total detections", "💯", 3, false, null),
        Achievement("detections_500", "Detection Veteran", "Reach 500 total detections", "⭐", 10, false, null),
        Achievement("quiz_master", "Quiz Champion", "Complete 10 pattern quizzes", "📚", 3, false, null),
        Achievement("perfect_week", "Perfect Week", "Detect patterns every day for 7 days", "✨", 5, false, null),
        Achievement("high_confidence", "Sharp Eye", "Detect a pattern with 95%+ confidence", "👁️", 2, false, null),
        Achievement("pattern_variety", "Pattern Collector", "Detect 25 unique pattern types", "🎨", 5, false, null),
        Achievement("early_adopter", "Early Adopter", "Use the app in its first month", "🚀", 3, false, null),
        Achievement("educator", "Pattern Educator", "Complete 5 tutorial lessons", "🎓", 3, false, null),
        Achievement("night_owl", "Night Trader", "Detect patterns after midnight", "🌙", 1, false, null)
    )

    fun getAll(context: Context): List<Achievement> {
        val state = loadState(context)
        return allAchievements.map { base ->
            val unlocked = state.optBoolean(base.id, false)
            val date = if (unlocked) state.optString("${base.id}_date", null) else null
            base.copy(unlocked = unlocked, unlockedDate = date)
        }
    }

    fun unlock(context: Context, achievementId: String): Achievement? {
        val achievement = allAchievements.find { it.id == achievementId } ?: return null
        val state = loadState(context)
        
        if (state.optBoolean(achievementId, false)) {
            return null // Already unlocked
        }

        val today = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        state.put(achievementId, true)
        state.put("${achievementId}_date", today)
        saveState(context, state)

        // Award bonus highlights
        if (achievement.reward > 0) {
            BonusHighlights.add(context, achievement.reward, "Achievement: ${achievement.title}")
        }

        return achievement.copy(unlocked = true, unlockedDate = today)
    }

    fun checkAndUnlock(context: Context, stats: UserStats): List<Achievement> {
        val unlocked = mutableListOf<Achievement>()

        // Check detection count achievements
        if (stats.totalDetections >= 1) {
            unlock(context, "first_detection")?.let { unlocked.add(it) }
        }
        if (stats.totalDetections >= 100) {
            unlock(context, "detections_100")?.let { unlocked.add(it) }
        }
        if (stats.totalDetections >= 500) {
            unlock(context, "detections_500")?.let { unlocked.add(it) }
        }

        // Check unique pattern achievements
        if (stats.uniquePatternsDetected >= 10) {
            unlock(context, "pattern_master_10")?.let { unlocked.add(it) }
        }
        if (stats.uniquePatternsDetected >= 25) {
            unlock(context, "pattern_variety")?.let { unlocked.add(it) }
        }
        if (stats.uniquePatternsDetected >= 50) {
            unlock(context, "pattern_master_50")?.let { unlocked.add(it) }
        }

        // Check streak achievements
        if (stats.currentStreak >= 3) {
            unlock(context, "streak_3")?.let { unlocked.add(it) }
        }
        if (stats.currentStreak >= 7) {
            unlock(context, "streak_7")?.let { unlocked.add(it) }
        }
        if (stats.currentStreak >= 30) {
            unlock(context, "streak_30")?.let { unlocked.add(it) }
        }

        // Check high confidence achievement
        if (stats.highestConfidence >= 0.95) {
            unlock(context, "high_confidence")?.let { unlocked.add(it) }
        }

        // Check quiz achievements
        if (stats.quizzesCompleted >= 10) {
            unlock(context, "quiz_master")?.let { unlocked.add(it) }
        }

        // Check tutorial achievements
        if (stats.tutorialsCompleted >= 5) {
            unlock(context, "educator")?.let { unlocked.add(it) }
        }

        return unlocked
    }

    fun getProgress(context: Context): Map<String, Double> {
        val stats = UserStats.load(context)
        return mapOf(
            "pattern_master_10" to (stats.uniquePatternsDetected / 10.0).coerceAtMost(1.0),
            "pattern_master_50" to (stats.uniquePatternsDetected / 50.0).coerceAtMost(1.0),
            "detections_100" to (stats.totalDetections / 100.0).coerceAtMost(1.0),
            "detections_500" to (stats.totalDetections / 500.0).coerceAtMost(1.0),
            "streak_7" to (stats.currentStreak / 7.0).coerceAtMost(1.0),
            "streak_30" to (stats.currentStreak / 30.0).coerceAtMost(1.0),
            "quiz_master" to (stats.quizzesCompleted / 10.0).coerceAtMost(1.0),
            "educator" to (stats.tutorialsCompleted / 5.0).coerceAtMost(1.0)
        )
    }

    private fun loadState(context: Context): JSONObject {
        val f = File(context.filesDir, FILE)
        return if (f.exists()) JSONObject(f.readText()) else JSONObject()
    }

    private fun saveState(context: Context, obj: JSONObject) {
        File(context.filesDir, FILE).writeText(obj.toString(2))
    }
}
