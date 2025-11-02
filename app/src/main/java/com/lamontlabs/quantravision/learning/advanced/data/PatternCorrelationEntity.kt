package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pattern_correlations")
data class PatternCorrelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pattern1: String,
    val pattern2: String,
    val correlation: Float,
    val cooccurrenceCount: Int,
    val lastUpdated: Long
)

@Entity(tableName = "pattern_sequences")
data class PatternSequenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sequencePatterns: String,
    val frequency: Int,
    val avgSuccessRate: Float,
    val avgTimeSpan: Long,
    val lastSeen: Long
)
