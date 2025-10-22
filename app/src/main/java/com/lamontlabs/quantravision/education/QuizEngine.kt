package com.lamontlabs.quantravision.education

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * QuizEngine
 * Deterministic local quiz system for educational self-assessment.
 * Each quiz entry: question, options[], correctIndex.
 * Stored offline, results never uploaded.
 */
object QuizEngine {

    data class Question(val id: String, val question: String, val options: List<String>, val correctIndex: Int)
    data class Result(val correct: Int, val total: Int, val percentage: Double)

    private fun load(context: Context): List<Question> {
        val file = File(context.filesDir, "quizzes.json")
        if (!file.exists()) return emptyList()
        val arr = JSONArray(file.readText())
        val qList = mutableListOf<Question>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val opts = mutableListOf<String>()
            val a = o.getJSONArray("options")
            for (j in 0 until a.length()) opts.add(a.getString(j))
            qList.add(
                Question(
                    id = o.getString("id"),
                    question = o.getString("question"),
                    options = opts,
                    correctIndex = o.getInt("correctIndex")
                )
            )
        }
        return qList
    }

    fun ensureDefault(context: Context) {
        val file = File(context.filesDir, "quizzes.json")
        if (file.exists()) return
        val arr = JSONArray().apply {
            put(JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("question", "Which pattern indicates potential bullish reversal?")
                put("options", JSONArray(listOf("Head & Shoulders", "Double Bottom", "Rising Wedge", "Falling Channel")))
                put("correctIndex", 1)
            })
            put(JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("question", "A 'Doji' candle suggests:")
                put("options", JSONArray(listOf("Strong reversal", "Market indecision", "High volume breakout", "Uptrend continuation")))
                put("correctIndex", 1)
            })
        }
        file.writeText(arr.toString(2))
    }

    fun grade(context: Context, answers: Map<String, Int>): Result {
        val questions = load(context)
        var correct = 0
        for (q in questions) {
            val ans = answers[q.id]
            if (ans != null && ans == q.correctIndex) correct++
        }
        val total = questions.size
        val pct = if (total == 0) 0.0 else (correct.toDouble() / total) * 100.0
        return Result(correct, total, pct)
    }
}
