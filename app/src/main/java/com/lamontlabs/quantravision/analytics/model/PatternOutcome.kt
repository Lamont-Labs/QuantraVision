package com.lamontlabs.quantravision.analytics.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PatternOutcome(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternMatchId: Int,
    val patternName: String,
    val outcome: Outcome,
    val timestamp: Long,
    val userFeedback: String = "",
    val profitLossPercent: Double? = null,
    val timeframe: String = "unknown"
)

enum class Outcome {
    WIN,
    LOSS,
    NEUTRAL
}
