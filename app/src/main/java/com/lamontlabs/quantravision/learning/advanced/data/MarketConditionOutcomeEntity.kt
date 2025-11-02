package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.advanced.model.MarketCondition

@Entity(tableName = "market_condition_outcomes")
data class MarketConditionOutcomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternType: String,
    val marketCondition: MarketCondition,
    val outcome: Outcome,
    val timestamp: Long,
    val volatilityLevel: String,
    val trendStrength: String
)
