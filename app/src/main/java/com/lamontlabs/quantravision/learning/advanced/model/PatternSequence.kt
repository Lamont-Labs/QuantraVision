package com.lamontlabs.quantravision.learning.advanced.model

data class PatternSequence(
    val patterns: List<String>,
    val frequency: Int,
    val avgSuccessRate: Float,
    val timeSpan: Long
)

data class PredictionWithConfidence(
    val patternType: String,
    val probability: Float,
    val sampleSize: Int
)
