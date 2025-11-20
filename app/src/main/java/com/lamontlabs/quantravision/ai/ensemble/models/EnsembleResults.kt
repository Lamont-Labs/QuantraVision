package com.lamontlabs.quantravision.ai.ensemble.models

data class IntentResult(
    val intent: String,
    val confidence: Float
)

data class RetrievalResult(
    val answer: String,
    val confidence: Float,
    val matchedQuestion: String
)

data class QAResult(
    val answer: String,
    val confidence: Float,
    val fromModel: Boolean
)
