package com.lamontlabs.quantravision.learning.model

data class PatternRecommendation(
    val patternType: String,
    val winRate: Double,
    val totalOutcomes: Int,
    val confidenceLevel: Double,
    val recommendationStrength: RecommendationStrength,
    val educationalMessage: String
) {
    companion object {
        fun create(
            patternType: String,
            winRate: Double,
            totalOutcomes: Int,
            avgConfidence: Double = 0.0
        ): PatternRecommendation {
            val strength = when {
                winRate >= 0.75 && totalOutcomes >= 15 -> RecommendationStrength.HIGH
                winRate >= 0.65 && totalOutcomes >= 10 -> RecommendationStrength.MEDIUM
                winRate >= 0.60 && totalOutcomes >= 5 -> RecommendationStrength.LOW
                else -> RecommendationStrength.LOW
            }
            
            val message = when (strength) {
                RecommendationStrength.HIGH -> 
                    "You have ${(winRate * 100).toInt()}% success with $patternType! This is an excellent pattern for your educational practice."
                RecommendationStrength.MEDIUM -> 
                    "You have ${(winRate * 100).toInt()}% success with $patternType. Continue practicing this pattern."
                RecommendationStrength.LOW -> 
                    "You have ${(winRate * 100).toInt()}% success with $patternType (educational data only)."
            }
            
            return PatternRecommendation(
                patternType = patternType,
                winRate = winRate,
                totalOutcomes = totalOutcomes,
                confidenceLevel = avgConfidence,
                recommendationStrength = strength,
                educationalMessage = message
            )
        }
    }
}

enum class RecommendationStrength {
    HIGH, MEDIUM, LOW
}
