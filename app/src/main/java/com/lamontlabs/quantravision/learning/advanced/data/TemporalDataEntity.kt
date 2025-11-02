package com.lamontlabs.quantravision.learning.advanced.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lamontlabs.quantravision.analytics.model.Outcome

@Entity(tableName = "temporal_outcomes")
data class TemporalDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patternType: String,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val outcome: Outcome,
    val timestamp: Long
)
