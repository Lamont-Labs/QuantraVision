package com.lamontlabs.quantravision.alerts

import androidx.compose.ui.graphics.Color

object PatternStrength {

    enum class StrengthLevel {
        WEAK,
        MODERATE,
        STRONG
    }
    
    data class StrengthInfo(
        val level: StrengthLevel,
        val color: Color,
        val emoji: String,
        val label: String,
        val description: String
    )
    
    fun calculateStrength(confidence: Double): StrengthLevel {
        return when {
            confidence >= 0.80 -> StrengthLevel.STRONG
            confidence >= 0.60 -> StrengthLevel.MODERATE
            else -> StrengthLevel.WEAK
        }
    }
    
    fun getStrengthInfo(confidence: Double): StrengthInfo {
        val level = calculateStrength(confidence)
        return when (level) {
            StrengthLevel.WEAK -> StrengthInfo(
                level = StrengthLevel.WEAK,
                color = Color(0xFFFF5252),
                emoji = "🔴",
                label = "Weak",
                description = "Watch closely - pattern may not complete"
            )
            StrengthLevel.MODERATE -> StrengthInfo(
                level = StrengthLevel.MODERATE,
                color = Color(0xFFFFC107),
                emoji = "🟡",
                label = "Moderate",
                description = "Likely forming - monitor development"
            )
            StrengthLevel.STRONG -> StrengthInfo(
                level = StrengthLevel.STRONG,
                color = Color(0xFF4CAF50),
                emoji = "🟢",
                label = "Strong",
                description = "High confidence - reliable signal"
            )
        }
    }
    
    fun getFormationPercent(confidence: Double): Int {
        return when {
            confidence >= 0.90 -> 100
            confidence >= 0.85 -> (85 + (confidence - 0.85) / 0.05 * 15).toInt()
            confidence >= 0.70 -> (70 + (confidence - 0.70) / 0.15 * 15).toInt()
            confidence >= 0.50 -> (50 + (confidence - 0.50) / 0.20 * 20).toInt()
            else -> (40 + confidence / 0.50 * 10).toInt().coerceIn(40, 100)
        }
    }
    
    fun shouldAnnounce(confidence: Double, previousConfidence: Double?): Boolean {
        if (previousConfidence == null) return confidence >= 0.60
        
        val currentLevel = calculateStrength(confidence)
        val previousLevel = calculateStrength(previousConfidence)
        
        return currentLevel != previousLevel || 
               (currentLevel == StrengthLevel.STRONG && previousLevel != StrengthLevel.STRONG)
    }
    
    fun getConfidenceGrade(confidence: Double): String {
        return when {
            confidence >= 0.95 -> "A+"
            confidence >= 0.90 -> "A"
            confidence >= 0.85 -> "A-"
            confidence >= 0.80 -> "B+"
            confidence >= 0.75 -> "B"
            confidence >= 0.70 -> "B-"
            confidence >= 0.65 -> "C+"
            confidence >= 0.60 -> "C"
            confidence >= 0.55 -> "C-"
            confidence >= 0.50 -> "D+"
            confidence >= 0.45 -> "D"
            else -> "F"
        }
    }
}
