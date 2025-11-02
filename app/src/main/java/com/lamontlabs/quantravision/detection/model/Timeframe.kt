package com.lamontlabs.quantravision.detection.model

enum class Timeframe(val displayName: String, val durationMinutes: Int) {
    M1("1 Minute", 1),
    M5("5 Minutes", 5),
    M15("15 Minutes", 15),
    H1("1 Hour", 60),
    H4("4 Hours", 240),
    DAILY("Daily", 1440);

    val durationMs: Long get() = durationMinutes * 60L * 1000L

    companion object {
        fun fromString(value: String): Timeframe? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
        
        fun fromDisplayName(displayName: String): Timeframe? {
            return values().find { it.displayName == displayName }
        }
    }
}
