package com.lamontlabs.quantravision.learning.model

data class DifficultyDetails(
    val patternType: String,
    val difficulty: Difficulty,
    val winRate: Double,
    val consistency: Double,
    val sampleSize: Int,
    val recommendedAction: String
) {
    companion object {
        fun calculate(
            patternType: String,
            winRate: Double,
            outcomes: List<Boolean>,
            sampleSize: Int
        ): DifficultyDetails {
            val consistency = if (outcomes.size >= 3) {
                calculateConsistency(outcomes)
            } else {
                0.0
            }
            
            val difficulty = when {
                sampleSize < 5 -> Difficulty.UNKNOWN
                winRate >= 0.70 && consistency >= 0.6 -> Difficulty.EASY
                winRate >= 0.50 && consistency >= 0.4 -> Difficulty.MEDIUM
                else -> Difficulty.HARD
            }
            
            val action = when (difficulty) {
                Difficulty.EASY -> 
                    "This pattern works well for you! Keep using it in your educational practice."
                Difficulty.MEDIUM -> 
                    "This pattern has moderate success. Focus on higher confidence detections to improve results."
                Difficulty.HARD -> 
                    "This pattern is challenging. Practice more with demo charts and review educational lessons."
                Difficulty.UNKNOWN -> 
                    "Not enough data yet. Continue practicing to get personalized insights."
            }
            
            return DifficultyDetails(
                patternType = patternType,
                difficulty = difficulty,
                winRate = winRate,
                consistency = consistency,
                sampleSize = sampleSize,
                recommendedAction = action
            )
        }
        
        private fun calculateConsistency(outcomes: List<Boolean>): Double {
            if (outcomes.size < 3) return 0.0
            
            val winRate = outcomes.count { it }.toDouble() / outcomes.size
            val variance = outcomes.map { outcome ->
                val value = if (outcome) 1.0 else 0.0
                (value - winRate) * (value - winRate)
            }.average()
            
            return 1.0 - variance.coerceIn(0.0, 1.0)
        }
    }
}

enum class Difficulty {
    EASY,       // 🟢 >70% win rate, high consistency
    MEDIUM,     // 🟡 50-70% win rate, moderate consistency
    HARD,       // 🔴 <50% win rate or low consistency
    UNKNOWN     // ⚪ Not enough data (<5 outcomes)
}
