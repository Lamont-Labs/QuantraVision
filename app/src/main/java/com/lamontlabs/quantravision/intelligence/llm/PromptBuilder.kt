package com.lamontlabs.quantravision.intelligence.llm

import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import com.lamontlabs.quantravision.intelligence.QuantraScorer

/**
 * Builds prompts for the LLM based on pattern detection results
 * 
 * Uses instruction-following format optimized for Gemma 2B Instruct:
 * <start_of_turn>user
 * {instruction}
 * <end_of_turn>
 * <start_of_turn>model
 */
class PromptBuilder {
    
    companion object {
        private const val SYSTEM_CONTEXT = """You are an expert trading pattern analyst and educator. 
Your role is to explain chart patterns in clear, simple language that helps traders learn.
Focus on:
- Why the pattern scored the way it did
- What indicators confirmed or contradicted it
- Educational insights about the pattern
- Keep responses under 200 words
- Use simple, everyday language"""
    }
    
    /**
     * Build prompt for explaining a detected pattern
     */
    fun buildPatternExplanation(
        pattern: PatternMatch,
        indicators: IndicatorContext?,
        scoreBreakdown: QuantraScorer.ScoreResult?,
        learningPhase: String? = null,
        totalScans: Int? = null
    ): String {
        val prompt = buildString {
            appendLine("<start_of_turn>user")
            appendLine(SYSTEM_CONTEXT)
            appendLine()
            appendLine("Explain this detected chart pattern:")
            appendLine("- Pattern: ${pattern.patternName}")
            appendLine("- QuantraScore: ${pattern.quantraScore}/100 (${getScoreGrade(pattern.quantraScore)})")
            appendLine("- Detection Confidence: ${(pattern.confidence * 100).toInt()}%")
            appendLine("- Timeframe: ${pattern.timeframe}")
            
            // Add indicator context if available
            indicators?.let {
                appendLine()
                appendLine("Technical Indicators:")
                it.rsi?.let { rsi -> appendLine("- RSI: ${rsi.toInt()}") }
                it.macd?.let { macd -> appendLine("- MACD: $macd") }
                it.volume?.let { vol -> appendLine("- Volume: $vol") }
                if (it.customIndicators.isNotEmpty()) {
                    it.customIndicators.take(3).forEach { custom ->
                        appendLine("- ${custom.name}: ${custom.value}")
                    }
                }
            }
            
            // Add score breakdown if available
            scoreBreakdown?.let {
                appendLine()
                appendLine("Score Components:")
                appendLine("- Base Confidence: ${it.baseScore.toInt()}")
                appendLine("- Confluence Boost: ${(it.confluenceBoost * 100).toInt()}%")
                if (it.bonusPoints != 0.0) {
                    appendLine("- Bonus: +${it.bonusPoints.toInt()}")
                }
                if (it.penalties != 0.0) {
                    appendLine("- Penalties: ${it.penalties.toInt()}")
                }
                if (it.adaptiveAdjustment != 0.0) {
                    appendLine("- Learning Adjustment: ${if (it.adaptiveAdjustment > 0) "+" else ""}${it.adaptiveAdjustment.toInt()}")
                }
            }
            
            // Add learning context if available
            if (learningPhase != null && totalScans != null) {
                appendLine()
                appendLine("Learning Status: $learningPhase phase ($totalScans scans recorded)")
            }
            
            appendLine()
            appendLine("Provide a brief explanation (under 200 words) covering:")
            appendLine("1. Why this pattern scored ${pattern.quantraScore}/100")
            appendLine("2. What indicators support or contradict it")
            appendLine("3. One key insight about this pattern type")
            appendLine("<end_of_turn>")
            appendLine("<start_of_turn>model")
        }
        
        return prompt
    }
    
    /**
     * Build prompt for answering educational questions
     */
    fun buildEducationalQuestion(question: String): String {
        return buildString {
            appendLine("<start_of_turn>user")
            appendLine(SYSTEM_CONTEXT)
            appendLine()
            appendLine("Question: $question")
            appendLine()
            appendLine("Provide a clear, educational answer in under 150 words.")
            appendLine("<end_of_turn>")
            appendLine("<start_of_turn>model")
        }
    }
    
    /**
     * Build prompt for weekly scan summary
     */
    fun buildWeeklySummary(
        totalScans: Int,
        patternsFound: Map<String, Int>,
        avgQuantraScore: Int,
        learningProgress: String
    ): String {
        return buildString {
            appendLine("<start_of_turn>user")
            appendLine(SYSTEM_CONTEXT)
            appendLine()
            appendLine("Summarize this week's pattern scanning activity:")
            appendLine("- Total scans: $totalScans")
            appendLine("- Average QuantraScore: $avgQuantraScore/100")
            appendLine("- Learning phase: $learningProgress")
            appendLine()
            appendLine("Patterns detected:")
            patternsFound.entries.take(5).forEach { (pattern, count) ->
                appendLine("- $pattern: $count times")
            }
            appendLine()
            appendLine("Provide insights about patterns, trends, and learning progress (under 150 words).")
            appendLine("<end_of_turn>")
            appendLine("<start_of_turn>model")
        }
    }
    
    /**
     * Get human-readable grade for score
     */
    private fun getScoreGrade(score: Int): String {
        return when {
            score >= 90 -> "Exceptional"
            score >= 80 -> "High Quality"
            score >= 60 -> "Good"
            score >= 40 -> "Fair"
            else -> "Low Quality"
        }
    }
}
