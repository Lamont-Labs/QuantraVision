package com.lamontlabs.quantravision.learning.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sessionId: String,
    val patternsDetected: String,
    val confidences: String,
    val timeframe: String,
    val scanDurationMs: Long,
    val chartHash: String
)

@Entity(tableName = "pattern_frequency")
data class PatternFrequencyEntity(
    @PrimaryKey
    val patternName: String,
    val totalScans: Long,
    val totalDetections: Long,
    val avgConfidence: Float,
    val lastSeen: Long
)

@Entity(tableName = "pattern_cooccurrence")
data class PatternCooccurrenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pattern1: String,
    val pattern2: String,
    val cooccurrenceCount: Long,
    val totalOpportunities: Long,
    val cooccurrenceRate: Float,
    val lastUpdated: Long
)
