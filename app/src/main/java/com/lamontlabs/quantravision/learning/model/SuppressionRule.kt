package com.lamontlabs.quantravision.learning.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppression_rules")
data class SuppressionRule(
    @PrimaryKey
    val patternType: String,
    val suppressionLevel: SuppressionLevel,
    val reason: String,
    val winRate: Double,
    val totalOutcomes: Int,
    val isUserOverridden: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class SuppressionLevel {
    NONE,       // No suppression - pattern performs well
    LOW,        // Suggest caution - 30-40% win rate
    MEDIUM,     // Auto-suppress low confidence (<70%) - 20-30% win rate
    HIGH        // Suggest disabling - <20% win rate
}
