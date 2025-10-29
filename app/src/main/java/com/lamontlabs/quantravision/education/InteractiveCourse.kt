package com.lamontlabs.quantravision.education

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * InteractiveCourse
 * 3-lesson foundation course: Introduction, Bull Flag, Head & Shoulders
 * Includes quizzes with explanations and certificate of completion (70%+ average)
 */
object InteractiveCourse {

    data class Lesson(
        val id: String,
        val title: String,
        val content: String,
        val imageRef: String,
        val quiz: Quiz,
        val order: Int
    )

    data class Quiz(
        val questions: List<Question>
    )

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int,
        val explanation: String
    )

    data class CourseProgress(
        val completedLessons: Set<String>,
        val quizScores: Map<String, Int>,
        val certificateEarned: Boolean
    )

    private val lessons = listOf(
        Lesson(
            id = "lesson_01",
            title = "Introduction to Chart Patterns",
            content = """
                Chart patterns are visual formations created by price movements on a chart. 
                They help traders identify potential future price movements based on historical behavior.
                
                Key Concepts:
                • Support and Resistance
                • Trend Lines
                • Price Action
                • Volume Confirmation
                
                Patterns are categorized into:
                1. Continuation Patterns (trend continues)
                2. Reversal Patterns (trend changes)
            """.trimIndent(),
            imageRef = "pattern_doji.png",
            quiz = Quiz(
                listOf(
                    Question(
                        "What do chart patterns help traders identify?",
                        listOf(
                            "Past price movements only",
                            "Potential future price movements",
                            "Random market noise",
                            "Trading volume"
                        ),
                        1,
                        "Chart patterns help identify potential future price movements based on historical behavior."
                    )
                )
            ),
            order = 1
        ),
        Lesson(
            id = "lesson_02",
            title = "Bull Flag Pattern",
            content = """
                The Bull Flag is a continuation pattern that appears in strong uptrends.
                
                Structure:
                1. Flagpole: Strong upward price movement
                2. Flag: Consolidation in a downward-sloping channel
                3. Breakout: Continuation of upward trend
                
                Trading Strategy:
                • Entry: On breakout above flag resistance
                • Target: Measure flagpole height, project from breakout
                • Stop Loss: Below flag support
            """.trimIndent(),
            imageRef = "pattern_bull_flag_pattern.png",
            quiz = Quiz(
                listOf(
                    Question(
                        "What type of pattern is the Bull Flag?",
                        listOf("Reversal", "Continuation", "Neutral", "Random"),
                        1,
                        "Bull Flag is a continuation pattern - the trend continues after consolidation."
                    )
                )
            ),
            order = 2
        ),
        Lesson(
            id = "lesson_03",
            title = "Head and Shoulders",
            content = """
                Head and Shoulders is a powerful reversal pattern signaling trend change.
                
                Structure:
                • Left Shoulder: First peak
                • Head: Highest peak
                • Right Shoulder: Third peak (similar height to left)
                • Neckline: Support connecting the troughs
                
                Confirmation:
                • Break below neckline
                • Increased volume on breakdown
                • Target: Neckline to head distance projected down
            """.trimIndent(),
            imageRef = "pattern_head_and_shoulders.png",
            quiz = Quiz(
                listOf(
                    Question(
                        "How many peaks form a Head and Shoulders pattern?",
                        listOf("2", "3", "4", "5"),
                        1,
                        "Head and Shoulders has three peaks: left shoulder, head, and right shoulder."
                    )
                )
            ),
            order = 3
        )
        // Additional lessons would continue here...
    )

    fun getLesson(lessonId: String): Lesson? {
        return lessons.find { it.id == lessonId }
    }

    fun getAllLessons(): List<Lesson> {
        return lessons.sortedBy { it.order }
    }

    fun loadProgress(context: Context): CourseProgress {
        val file = File(context.filesDir, "course_progress.json")
        if (!file.exists()) {
            return CourseProgress(emptySet(), emptyMap(), false)
        }

        val json = JSONObject(file.readText())
        val completedJson = json.optJSONArray("completed") ?: JSONArray()
        val completed = mutableSetOf<String>()
        for (i in 0 until completedJson.length()) {
            completed.add(completedJson.getString(i))
        }

        val scoresJson = json.optJSONObject("scores") ?: JSONObject()
        val scores = mutableMapOf<String, Int>()
        scoresJson.keys().forEach { key ->
            scores[key] = scoresJson.getInt(key)
        }

        return CourseProgress(
            completedLessons = completed,
            quizScores = scores,
            certificateEarned = json.optBoolean("certificate", false)
        )
    }

    fun saveProgress(context: Context, progress: CourseProgress) {
        val json = JSONObject()
        
        val completedJson = JSONArray()
        progress.completedLessons.forEach { completedJson.put(it) }
        json.put("completed", completedJson)

        val scoresJson = JSONObject()
        progress.quizScores.forEach { (lesson, score) ->
            scoresJson.put(lesson, score)
        }
        json.put("scores", scoresJson)
        json.put("certificate", progress.certificateEarned)

        File(context.filesDir, "course_progress.json").writeText(json.toString(2))
    }

    fun completeLesson(context: Context, lessonId: String, quizScore: Int) {
        val progress = loadProgress(context)
        val updated = progress.copy(
            completedLessons = progress.completedLessons + lessonId,
            quizScores = progress.quizScores + (lessonId to quizScore)
        )

        // Check if certificate earned (all lessons complete, average score >= 70%)
        val certificate = if (updated.completedLessons.size == lessons.size) {
            val avgScore = updated.quizScores.values.average()
            avgScore >= 70.0
        } else false

        saveProgress(context, updated.copy(certificateEarned = certificate))

        // Note: Bonus highlights only available for Standard/Pro tiers
        // Free tier does NOT earn bonus highlights from lessons
    }

    fun getCertificate(context: Context): String? {
        val progress = loadProgress(context)
        if (!progress.certificateEarned) return null

        val avgScore = progress.quizScores.values.average().toInt()
        return """
            ═══════════════════════════════════════
                 CERTIFICATE OF COMPLETION
            ═══════════════════════════════════════
            
            This certifies that the holder has
            successfully completed the
            
            QUANTRAVISION PATTERN TRADING COURSE
            
            ${lessons.size} Lessons Completed
            Average Score: $avgScore%
            
            Lamont Labs
            Pattern Detection & Trading Education
            
            ═══════════════════════════════════════
        """.trimIndent()
    }
}
