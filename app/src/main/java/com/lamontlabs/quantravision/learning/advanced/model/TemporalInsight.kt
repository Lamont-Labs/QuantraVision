package com.lamontlabs.quantravision.learning.advanced.model

import java.time.DayOfWeek

data class TemporalInsight(
    val timeRange: TimeRange,
    val winRate: Float,
    val sampleSize: Int,
    val pValue: Float,
    val recommendation: String
)

data class TimeRange(
    val startHour: Int,
    val endHour: Int
) {
    override fun toString(): String = "$startHour:00-$endHour:00"
}

data class HeatmapData(
    val hourDayGrid: Map<Int, Map<DayOfWeek, HeatmapCell>>
)

data class HeatmapCell(
    val winRate: Float,
    val sampleSize: Int,
    val isSignificant: Boolean
)
