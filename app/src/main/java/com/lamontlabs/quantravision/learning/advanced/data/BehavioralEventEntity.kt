package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lamontlabs.quantravision.analytics.model.Outcome

@Entity(tableName = "behavioral_events")
data class BehavioralEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val patternType: String,
    val outcome: Outcome,
    val timestamp: Long,
    val sessionStartTime: Long,
    val patternCountInSession: Int,
    val timeSinceLastPattern: Long,
    val isAfterLoss: Boolean
)
