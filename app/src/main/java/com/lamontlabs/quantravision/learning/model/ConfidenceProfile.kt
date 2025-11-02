package com.lamontlabs.quantravision.learning.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "confidence_profiles")
data class ConfidenceProfile(
    @PrimaryKey
    val patternType: String,
    val bucket0_30WinRate: Double = 0.0,
    val bucket30_50WinRate: Double = 0.0,
    val bucket50_70WinRate: Double = 0.0,
    val bucket70_90WinRate: Double = 0.0,
    val bucket90_100WinRate: Double = 0.0,
    val bucket0_30Count: Int = 0,
    val bucket30_50Count: Int = 0,
    val bucket50_70Count: Int = 0,
    val bucket70_90Count: Int = 0,
    val bucket90_100Count: Int = 0,
    val recommendedThreshold: Double = 0.5,
    val totalOutcomes: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class ConfidenceBucket(
    val range: String,
    val winRate: Double,
    val sampleCount: Int
)
