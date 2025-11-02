package com.lamontlabs.quantravision.learning.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_metadata")
data class LearningMetadata(
    @PrimaryKey
    val key: String,
    val value: String,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        const val KEY_TOTAL_FEEDBACK_COUNT = "total_feedback_count"
        const val KEY_LEARNING_ENABLED = "learning_enabled"
        const val KEY_LAST_RECOMMENDATION_UPDATE = "last_recommendation_update"
        const val KEY_DATA_QUALITY_SCORE = "data_quality_score"
    }
}
