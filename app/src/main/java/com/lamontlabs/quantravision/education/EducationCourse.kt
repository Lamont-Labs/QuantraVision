package com.lamontlabs.quantravision.education

import android.content.Context
import com.lamontlabs.quantravision.education.course.CourseRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Complete 25-lesson interactive education course
 * Comprehensive pattern trading curriculum from fundamentals to advanced strategies
 * 
 * REFACTORED: Individual lessons extracted to education/course/ directory
 * This facade delegates to CourseRegistry for lesson content
 */
object EducationCourse {

    data class Lesson(
        val id: Int,
        val title: String,
        val description: String,
        val content: String,
        val keyPoints: List<String>,
        val examples: List<PatternExample>,
        val quiz: Quiz
    )

    data class PatternExample(
        val patternName: String,
        val description: String,
        val identificationTips: List<String>
    )

    data class Quiz(
        val questions: List<Question>
    )

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswerIndex: Int,
        val explanation: String
    )

    data class LessonProgress(
        val lessonId: Int,
        val completed: Boolean,
        val quizScore: Int,
        val bonusHighlightsEarned: Int
    )

    data class Certificate(
        val userName: String,
        val completionDate: Long,
        val averageScore: Int,
        val totalLessons: Int
    )

    /**
     * All 25 lessons - Complete course
     * Delegates to CourseRegistry
     */
    fun getAllLessons(): List<Lesson> {
        return CourseRegistry.getAllCourseLessons()
    }

    /**
     * Save lesson progress
     */
    suspend fun saveLessonProgress(context: Context, progress: LessonProgress) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "education_progress.json")
        val allProgress = loadAllProgress(context).toMutableList()
        allProgress.removeIf { it.lessonId == progress.lessonId }
        allProgress.add(progress)
        file.writeText(allProgress.joinToString("\n") { 
            "${it.lessonId},${it.completed},${it.quizScore},${it.bonusHighlightsEarned}"
        })
    }

    /**
     * Load all lesson progress
     */
    suspend fun loadAllProgress(context: Context): List<LessonProgress> = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "education_progress.json")
        if (!file.exists()) return@withContext emptyList()
        
        file.readLines().mapNotNull { line ->
            val parts = line.split(",")
            if (parts.size == 4) {
                LessonProgress(
                    lessonId = parts[0].toInt(),
                    completed = parts[1].toBoolean(),
                    quizScore = parts[2].toInt(),
                    bonusHighlightsEarned = parts[3].toInt()
                )
            } else null
        }
    }

    /**
     * Check if eligible for certificate (70%+ average)
     */
    fun isEligibleForCertificate(allProgress: List<LessonProgress>): Boolean {
        if (allProgress.size < 25) return false
        val avgScore = allProgress.map { it.quizScore }.average()
        return avgScore >= 70.0
    }

    /**
     * Generate certificate
     */
    fun generateCertificate(userName: String, allProgress: List<LessonProgress>): Certificate {
        return Certificate(
            userName = userName,
            completionDate = System.currentTimeMillis(),
            averageScore = allProgress.map { it.quizScore }.average().toInt(),
            totalLessons = allProgress.size
        )
    }
}
