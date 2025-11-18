package com.lamontlabs.quantravision.ai.quantrabot

import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.intelligence.IndicatorContext

/**
 * Builds expert-level prompts for Gemma 2B using pattern knowledge base.
 * 
 * Instead of expensive fine-tuning, this class feeds the LLM with expert
 * knowledge about each pattern, making it perform like a trading specialist.
 */
class PatternPromptBuilder {
    
    /**
     * Build a validation prompt for a detected pattern.
     * This prompt asks the LLM to validate the pattern based on expert rules.
     */
    fun buildValidationPrompt(
        pattern: PatternMatch,
        knowledge: PatternKnowledge,
        indicatorContext: IndicatorContext?
    ): String {
        val indicators = buildIndicatorSummary(indicatorContext)
        
        return """
You are an expert technical analyst. Validate this pattern detection.

PATTERN DETECTED: ${knowledge.patternName}
Confidence: ${String.format("%.1f", pattern.confidence * 100)}%
Category: ${knowledge.category.name.lowercase().capitalize()}
Expected Bias: ${knowledge.bias.name.lowercase().capitalize()}

PATTERN DESCRIPTION:
${knowledge.description}

EXPERT VALIDATION RULES:

Structure Requirements:
${knowledge.validationRules.structure.joinToString("\n") { "• $it" }}

Volume Requirements:
${knowledge.validationRules.volume.joinToString("\n") { "• $it" }}

Indicator Requirements:
${knowledge.validationRules.indicators.joinToString("\n") { "• $it" }}

INVALIDATION SIGNALS (These indicate FALSE POSITIVE):
${knowledge.invalidationSignals.joinToString("\n") { "• $it" }}

DETECTED INDICATORS:
$indicators

TASK:
Based on the expert rules above and the detected indicators, is this a VALID ${knowledge.patternName} pattern?

Provide your analysis in this format:
VERDICT: [VALID/INVALID/UNCERTAIN]
CONFIDENCE_ADJUSTMENT: [number from -30 to +30, where negative reduces confidence and positive increases it]
REASONING: [2-3 sentences explaining why, referencing specific rules and indicators]
KEY_CONCERN: [Main issue if invalid, or strongest validation signal if valid]
        """.trimIndent()
    }
    
    /**
     * Build an explanation prompt for a detected pattern.
     * This prompt asks the LLM to explain what the pattern means and how to trade it.
     */
    fun buildExplanationPrompt(
        pattern: PatternMatch,
        knowledge: PatternKnowledge,
        indicatorContext: IndicatorContext?
    ): String {
        val indicators = buildIndicatorSummary(indicatorContext)
        
        return """
You are a helpful trading assistant explaining patterns to retail traders.

PATTERN: ${knowledge.patternName}
Confidence: ${String.format("%.1f", pattern.confidence * 100)}%
QuantraScore: ${pattern.quantraScore}/100

PATTERN BASICS:
${knowledge.description}

This is a ${knowledge.category.name.lowercase()} pattern with ${knowledge.bias.name.lowercase()} bias.

DETECTED INDICATORS:
$indicators

TASK:
Explain this pattern to the user in simple, everyday language. Include:

1. What this pattern means (1-2 sentences)
2. Why it appeared in this context (reference the indicators)
3. What typically happens next
4. One practical trading tip

Keep it conversational and helpful. Avoid jargon. Be encouraging but honest about risks.
        """.trimIndent()
    }
    
    /**
     * Build a comparison prompt when user asks about pattern differences.
     */
    fun buildComparisonPrompt(
        pattern1: PatternKnowledge,
        pattern2: PatternKnowledge
    ): String {
        return """
You are a trading educator explaining pattern differences.

PATTERNS TO COMPARE:
1. ${pattern1.patternName} (${pattern1.category.name.lowercase()}, ${pattern1.bias.name.lowercase()})
2. ${pattern2.patternName} (${pattern2.category.name.lowercase()}, ${pattern2.bias.name.lowercase()})

PATTERN 1 DESCRIPTION:
${pattern1.description}

PATTERN 2 DESCRIPTION:
${pattern2.description}

TASK:
Explain the key differences between these patterns in simple language:

1. Visual differences (how they look on a chart)
2. Market context (when each appears)
3. Trading implications (how to use each)
4. Which is generally more reliable and why

Keep it practical and easy to understand.
        """.trimIndent()
    }
    
    /**
     * Build a general Q&A prompt for trading questions.
     */
    fun buildGeneralQuestionPrompt(
        question: String,
        recentPatterns: List<PatternMatch> = emptyList()
    ): String {
        val contextSection = if (recentPatterns.isNotEmpty()) {
            """
            
RECENT PATTERNS DETECTED:
${recentPatterns.take(5).joinToString("\n") { "• ${it.patternName} (${String.format("%.0f", it.confidence * 100)}%)" }}
            """.trimIndent()
        } else {
            ""
        }
        
        return """
You are QuantraBot, a helpful AI trading assistant for retail traders.

USER QUESTION:
$question
$contextSection

TASK:
Answer the question in simple, everyday language. Be helpful, practical, and encouraging.
If the question relates to their recent scans, reference that context.
Keep responses concise (2-4 sentences unless more detail is needed).
        """.trimIndent()
    }
    
    /**
     * Build QuantraScore explanation prompt.
     */
    fun buildScoreExplanationPrompt(
        pattern: PatternMatch,
        knowledge: PatternKnowledge?,
        indicatorContext: IndicatorContext?
    ): String {
        val indicators = buildIndicatorSummary(indicatorContext)
        
        return """
You are QuantraBot explaining why a pattern received its quality score.

PATTERN: ${pattern.patternName}
QuantraScore: ${pattern.quantraScore}/100
Confidence: ${String.format("%.1f", pattern.confidence * 100)}%

DETECTED INDICATORS:
$indicators

TASK:
Explain in simple language why this pattern scored ${pattern.quantraScore}/100.

Include:
1. What contributed to the score (positive factors)
2. What might have lowered the score (concerns)
3. Whether this is a high-quality setup or needs caution

Keep it practical and actionable.
        """.trimIndent()
    }
    
    /**
     * Helper: Build indicator summary from IndicatorContext.
     */
    private fun buildIndicatorSummary(indicatorContext: IndicatorContext?): String {
        if (indicatorContext == null) {
            return "No indicator data available for this scan."
        }
        
        val parts = mutableListOf<String>()
        
        // Add available indicators
        indicatorContext.rsi?.let { parts.add("RSI: ${String.format("%.1f", it)}") }
        indicatorContext.macd?.let { parts.add("MACD: ${String.format("%.4f", it)}") }
        indicatorContext.macdSignal?.let { parts.add("MACD Signal: ${String.format("%.4f", it)}") }
        indicatorContext.volume?.let { parts.add("Volume: ${String.format("%.0f", it)}") }
        
        // Add context analysis
        parts.add("Context: ${indicatorContext.context.name.lowercase().capitalize()}")
        parts.add("Confluence: ${indicatorContext.confluence}")
        
        return if (parts.isEmpty()) {
            "Limited indicator data available."
        } else {
            parts.joinToString("\n")
        }
    }
}
