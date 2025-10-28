package com.lamontlabs.quantravision.prediction

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * PatternPredictionEngine
 * Predicts forming patterns before they complete
 * Provides early warning system for emerging technical patterns
 */
object PatternPredictionEngine {

    data class FormingPattern(
        val patternName: String,
        val completionPercent: Double,
        val confidence: Double,
        val estimatedCompletion: String, // "1-2 bars", "3-5 bars", etc.
        val keyLevels: List<Double>,
        val stage: String // "early", "developing", "nearly_complete"
    )

    /**
     * Analyzes partial pattern matches to predict forming patterns
     */
    fun predictForming(
        context: Context,
        recentMatches: List<PatternMatch>,
        partialConfidenceThreshold: Double = 0.4
    ): List<FormingPattern> {
        val forming = mutableListOf<FormingPattern>()

        // Group matches by pattern name
        val byPattern = recentMatches.groupBy { it.patternName }

        byPattern.forEach { (patternName, matches) ->
            // Look for patterns with increasing confidence over time
            val sorted = matches.sortedBy { it.timestamp }
            if (sorted.size >= 2) {
                val latest = sorted.last()
                val previous = sorted[sorted.size - 2]

                // Check if confidence is increasing (pattern forming)
                if (latest.confidence > previous.confidence &&
                    latest.confidence >= partialConfidenceThreshold &&
                    latest.confidence < 0.85 // Not yet complete
                ) {
                    val stage = when {
                        latest.confidence < 0.5 -> "early"
                        latest.confidence < 0.7 -> "developing"
                        else -> "nearly_complete"
                    }

                    val completion = (latest.confidence / 0.85) * 100 // Assuming 85% = complete
                    val estimatedBars = estimateCompletionTime(latest.confidence, previous.confidence)

                    forming.add(
                        FormingPattern(
                            patternName = patternName,
                            completionPercent = completion.coerceAtMost(100.0),
                            confidence = latest.confidence,
                            estimatedCompletion = estimatedBars,
                            keyLevels = calculateKeyLevels(patternName, latest.confidence),
                            stage = stage
                        )
                    )
                }
            }
        }

        // Save predictions for tracking
        savePredictions(context, forming)

        return forming.sortedByDescending { it.completionPercent }
    }

    /**
     * Analyzes temporal stability to predict pattern completion
     */
    fun analyzeFormationVelocity(
        matches: List<PatternMatch>,
        timeWindowMs: Long = 30000L
    ): Map<String, Double> {
        val velocities = mutableMapOf<String, Double>()
        val byPattern = matches.groupBy { it.patternName }

        byPattern.forEach { (patternName, patternMatches) ->
            val recent = patternMatches.filter {
                System.currentTimeMillis() - it.timestamp < timeWindowMs
            }.sortedBy { it.timestamp }

            if (recent.size >= 2) {
                val first = recent.first()
                val last = recent.last()
                val timeDiff = (last.timestamp - first.timestamp).toDouble()
                val confDiff = last.confidence - first.confidence

                if (timeDiff > 0) {
                    velocities[patternName] = confDiff / (timeDiff / 1000.0) // per second
                }
            }
        }

        return velocities
    }

    /**
     * Predicts next likely patterns based on historical sequences
     */
    fun predictNextPattern(context: Context, currentPattern: String): List<String> {
        val sequences = loadPatternSequences(context)
        val following = mutableMapOf<String, Int>()

        // Find patterns that historically followed the current pattern
        sequences.forEach { seq ->
            val idx = seq.indexOf(currentPattern)
            if (idx >= 0 && idx < seq.size - 1) {
                val next = seq[idx + 1]
                following[next] = (following[next] ?: 0) + 1
            }
        }

        return following.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    private fun estimateCompletionTime(current: Double, previous: Double): String {
        val rate = current - previous
        if (rate <= 0) return "Unknown"

        val remaining = 0.85 - current
        val bars = (remaining / rate).toInt()

        return when {
            bars <= 2 -> "1-2 bars"
            bars <= 5 -> "3-5 bars"
            bars <= 10 -> "6-10 bars"
            else -> "10+ bars"
        }
    }

    private fun calculateKeyLevels(patternName: String, confidence: Double): List<Double> {
        // Simplified key level calculation
        // In production, this would analyze actual price data
        return listOf(
            confidence * 0.9,
            confidence * 1.0,
            confidence * 1.1
        )
    }

    private fun savePredictions(context: Context, predictions: List<FormingPattern>) {
        val json = JSONArray()
        predictions.forEach { pred ->
            json.put(JSONObject().apply {
                put("pattern", pred.patternName)
                put("completion", pred.completionPercent)
                put("confidence", pred.confidence)
                put("stage", pred.stage)
                put("timestamp", System.currentTimeMillis())
            })
        }

        File(context.filesDir, "pattern_predictions.json").writeText(json.toString(2))
    }

    private fun loadPatternSequences(context: Context): List<List<String>> {
        val file = File(context.filesDir, "pattern_sequences.json")
        if (!file.exists()) return emptyList()

        val sequences = mutableListOf<List<String>>()
        val json = JSONArray(file.readText())

        for (i in 0 until json.length()) {
            val seqJson = json.getJSONArray(i)
            val seq = mutableListOf<String>()
            for (j in 0 until seqJson.length()) {
                seq.add(seqJson.getString(j))
            }
            sequences.add(seq)
        }

        return sequences
    }

    fun recordPatternSequence(context: Context, patterns: List<String>) {
        val sequences = loadPatternSequences(context).toMutableList()
        sequences.add(patterns)

        // Keep only recent 100 sequences
        if (sequences.size > 100) {
            sequences.removeAt(0)
        }

        val json = JSONArray()
        sequences.forEach { seq ->
            val seqJson = JSONArray()
            seq.forEach { seqJson.put(it) }
            json.put(seqJson)
        }

        File(context.filesDir, "pattern_sequences.json").writeText(json.toString(2))
    }
}
