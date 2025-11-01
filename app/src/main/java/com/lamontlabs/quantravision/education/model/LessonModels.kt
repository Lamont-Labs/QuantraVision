package com.lamontlabs.quantravision.education.model

data class Lesson(
    val id: Int,
    val title: String,
    val category: String,
    val duration: String,
    val content: String,
    val quiz: Quiz
)

data class Quiz(
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)
