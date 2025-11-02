package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "strategy_metrics")
data class StrategyMetricsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val portfolioPatterns: String,
    val winRate: Float,
    val sharpeRatio: Float,
    val diversification: Float,
    val sampleSize: Int,
    val lastUpdated: Long
)
