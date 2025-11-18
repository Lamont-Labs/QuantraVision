package com.lamontlabs.quantravision.ai.quantrabot

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.ai.gemma.GemmaEngine
import com.lamontlabs.quantravision.ai.PatternExplainer
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * QuantraBot - AI Trading Assistant
 * 
 * Provides:
 * 1. Pattern validation (improves scan accuracy)
 * 2. Pattern explanations (educational)
 * 3. General trading Q&A
 * 4. QuantraScore breakdown analysis
 * 
 * Uses pattern knowledge base + Gemma 2B LLM for expert-level analysis
 * without requiring expensive fine-tuning.
 */
class QuantraBotEngine(private val context: Context) {
    
    private val knowledgeLoader = PatternKnowledgeLoader(context)
    private val promptBuilder = PatternPromptBuilder()
    private val gemmaEngine = GemmaEngine(context)
    private val fallbackExplainer = PatternExplainer(context)
    
    // Track initialization status
    @Volatile
    private var isInitialized = false
    
    @Volatile
    private var hasModel = false
    
    /**
     * Initialize QuantraBot.
     * Loads pattern knowledge and checks if Gemma model is available.
     * 
     * Returns Result.success if ready, Result.failure if issues found.
     * Fallback explanations will still work even if model unavailable.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Timber.i("🤖 Initializing QuantraBot...")
            
            // Load pattern knowledge base
            val knowledge = knowledgeLoader.loadAll()
            Timber.i("✅ Pattern knowledge loaded: ${knowledge.size} patterns")
            
            // Initialize Gemma engine
            val gemmaResult = gemmaEngine.initialize()
            hasModel = gemmaResult.isSuccess
            
            if (hasModel) {
                Timber.i("✅ Gemma 2B model available - QuantraBot fully operational")
            } else {
                Timber.w("⚠️ Gemma model not available - using fallback explanations")
            }
            
            // Initialize fallback explainer
            fallbackExplainer.initialize()
            
            isInitialized = true
            Timber.i("🤖 QuantraBot initialization complete")
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize QuantraBot")
            Result.failure(e)
        }
    }
    
    /**
     * Validate a detected pattern using AI analysis.
     * 
     * Returns validation result with:
     * - isValid: true/false/uncertain
     * - confidenceAdjustment: -30 to +30 points
     * - reasoning: explanation
     * 
     * If no knowledge or model available, returns neutral result.
     */
    suspend fun validatePattern(
        pattern: PatternMatch,
        indicatorContext: IndicatorContext?
    ): ValidationResult = withContext(Dispatchers.Default) {
        try {
            // Check if we have knowledge for this pattern
            val knowledge = knowledgeLoader.getKnowledgeByName(pattern.patternName)
            if (knowledge == null) {
                Timber.d("No knowledge available for pattern: ${pattern.patternName}")
                return@withContext ValidationResult.neutral("No expert knowledge for this pattern")
            }
            
            // If no model available, use rule-based validation
            if (!hasModel) {
                return@withContext performRuleBasedValidation(pattern, knowledge, indicatorContext)
            }
            
            // Build validation prompt
            val prompt = promptBuilder.buildValidationPrompt(pattern, knowledge, indicatorContext)
            
            // Get LLM validation
            val response = gemmaEngine.generate(prompt)
            
            // Parse response
            parseValidationResponse(response)
            
        } catch (e: Exception) {
            Timber.e(e, "Error validating pattern: ${pattern.patternName}")
            ValidationResult.neutral("Validation error: ${e.message}")
        }
    }
    
    /**
     * Generate explanation for a detected pattern.
     * 
     * Returns plain English explanation suitable for retail traders.
     * Falls back to template-based explanation if model unavailable.
     */
    suspend fun explainPattern(
        pattern: PatternMatch,
        indicatorContext: IndicatorContext?
    ): String = withContext(Dispatchers.Default) {
        try {
            val knowledge = knowledgeLoader.getKnowledgeByName(pattern.patternName)
            
            // If model available and we have knowledge, use LLM
            if (hasModel && knowledge != null) {
                val prompt = promptBuilder.buildExplanationPrompt(pattern, knowledge, indicatorContext)
                return@withContext gemmaEngine.generate(prompt)
            }
            
            // Fallback to template-based explanation
            fallbackExplainer.explainPattern(pattern.patternName)
            
        } catch (e: Exception) {
            Timber.e(e, "Error explaining pattern: ${pattern.patternName}")
            fallbackExplainer.explainPattern(pattern.patternName)
        }
    }
    
    /**
     * Answer a general trading question.
     * 
     * Can optionally provide context from recent scans.
     */
    suspend fun answerQuestion(
        question: String,
        recentPatterns: List<PatternMatch> = emptyList()
    ): String = withContext(Dispatchers.Default) {
        try {
            if (!hasModel) {
                return@withContext "QuantraBot requires the AI model to answer questions. The model is not currently available."
            }
            
            val prompt = promptBuilder.buildGeneralQuestionPrompt(question, recentPatterns)
            gemmaEngine.generate(prompt)
            
        } catch (e: Exception) {
            Timber.e(e, "Error answering question")
            "I'm having trouble answering that right now. Please try again later."
        }
    }
    
    /**
     * Explain why a pattern received its QuantraScore.
     */
    suspend fun explainQuantraScore(
        pattern: PatternMatch,
        indicatorContext: IndicatorContext?
    ): String = withContext(Dispatchers.Default) {
        try {
            val knowledge = knowledgeLoader.getKnowledgeByName(pattern.patternName)
            
            if (hasModel) {
                val prompt = promptBuilder.buildScoreExplanationPrompt(pattern, knowledge, indicatorContext)
                return@withContext gemmaEngine.generate(prompt)
            }
            
            // Fallback to simple explanation
            buildSimpleScoreExplanation(pattern, indicatorContext)
            
        } catch (e: Exception) {
            Timber.e(e, "Error explaining QuantraScore")
            buildSimpleScoreExplanation(pattern, indicatorContext)
        }
    }
    
    /**
     * Compare two patterns (educational feature).
     */
    suspend fun comparePatterns(
        pattern1Name: String,
        pattern2Name: String
    ): String = withContext(Dispatchers.Default) {
        try {
            val knowledge1 = knowledgeLoader.getKnowledgeByName(pattern1Name)
            val knowledge2 = knowledgeLoader.getKnowledgeByName(pattern2Name)
            
            if (knowledge1 == null || knowledge2 == null) {
                return@withContext "I don't have detailed knowledge about one or both of these patterns yet."
            }
            
            if (!hasModel) {
                return@withContext buildSimpleComparison(knowledge1, knowledge2)
            }
            
            val prompt = promptBuilder.buildComparisonPrompt(knowledge1, knowledge2)
            gemmaEngine.generate(prompt)
            
        } catch (e: Exception) {
            Timber.e(e, "Error comparing patterns")
            "I'm having trouble comparing those patterns right now."
        }
    }
    
    /**
     * Check if QuantraBot is ready to use.
     */
    fun isReady(): Boolean = isInitialized
    
    /**
     * Check if AI model is available (vs fallback mode).
     */
    fun hasAIModel(): Boolean = hasModel
    
    /**
     * Get number of patterns with expert knowledge.
     */
    fun getKnowledgeCount(): Int = knowledgeLoader.getAvailablePatternIds().size
    
    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================
    
    /**
     * Rule-based validation when LLM not available.
     * Uses expert rules from knowledge base.
     */
    private fun performRuleBasedValidation(
        pattern: PatternMatch,
        knowledge: PatternKnowledge,
        indicatorContext: IndicatorContext?
    ): ValidationResult {
        // Simple rule-based checks
        val issues = mutableListOf<String>()
        var adjustment = 0
        
        // Check volume if available
        indicatorContext?.volume?.let { vol ->
            // Volume validation would go here based on pattern type
            // For now, keep it simple
        }
        
        // Check RSI divergence for reversal patterns
        if (knowledge.category == PatternCategory.REVERSAL) {
            indicatorContext?.rsi?.let { rsi ->
                if (knowledge.bias == MarketBias.BEARISH && rsi < 30) {
                    issues.add("RSI already oversold - reversal less likely")
                    adjustment -= 10
                } else if (knowledge.bias == MarketBias.BULLISH && rsi > 70) {
                    issues.add("RSI already overbought - reversal less likely")
                    adjustment -= 10
                }
            }
        }
        
        return if (issues.isEmpty()) {
            ValidationResult.valid(adjustment, "Basic validation passed")
        } else {
            ValidationResult.uncertain(adjustment, issues.joinToString("; "))
        }
    }
    
    /**
     * Parse LLM validation response.
     */
    private fun parseValidationResponse(response: String): ValidationResult {
        try {
            // Extract verdict
            val verdictMatch = Regex("VERDICT:\\s*(VALID|INVALID|UNCERTAIN)", RegexOption.IGNORE_CASE)
                .find(response)
            val verdict = verdictMatch?.groupValues?.get(1)?.uppercase() ?: "UNCERTAIN"
            
            // Extract confidence adjustment
            val adjustmentMatch = Regex("CONFIDENCE_ADJUSTMENT:\\s*([+-]?\\d+)")
                .find(response)
            val adjustment = adjustmentMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0
            
            // Extract reasoning
            val reasoningMatch = Regex("REASONING:\\s*(.+?)(?=KEY_CONCERN:|$)", RegexOption.DOT_MATCHES_ALL)
                .find(response)
            val reasoning = reasoningMatch?.groupValues?.get(1)?.trim() ?: "No reasoning provided"
            
            return when (verdict) {
                "VALID" -> ValidationResult.valid(adjustment, reasoning)
                "INVALID" -> ValidationResult.invalid(adjustment, reasoning)
                else -> ValidationResult.uncertain(adjustment, reasoning)
            }
            
        } catch (e: Exception) {
            Timber.w(e, "Failed to parse validation response")
            return ValidationResult.neutral("Could not parse validation")
        }
    }
    
    /**
     * Build simple score explanation without LLM.
     */
    private fun buildSimpleScoreExplanation(
        pattern: PatternMatch,
        indicatorContext: IndicatorContext?
    ): String {
        val score = pattern.quantraScore
        val confidence = pattern.confidence
        
        return when {
            score >= 80 -> "This ${pattern.patternName} pattern scored ${score}/100, which is excellent. The pattern structure is strong (${String.format("%.0f", confidence * 100)}% confidence) and technical indicators align well."
            score >= 60 -> "This ${pattern.patternName} pattern scored ${score}/100, which is good. The pattern is valid but may have some minor concerns in the indicator context or structure."
            score >= 40 -> "This ${pattern.patternName} pattern scored ${score}/100, which is marginal. Consider waiting for better confirmation before acting on this signal."
            else -> "This ${pattern.patternName} pattern scored ${score}/100, which is low. This suggests conflicting signals or weak pattern structure. Use caution."
        }
    }
    
    /**
     * Build simple pattern comparison without LLM.
     */
    private fun buildSimpleComparison(
        pattern1: PatternKnowledge,
        pattern2: PatternKnowledge
    ): String {
        return """
${pattern1.patternName} vs ${pattern2.patternName}:

${pattern1.patternName} (${pattern1.bias.name.lowercase()}):
${pattern1.description}

${pattern2.patternName} (${pattern2.bias.name.lowercase()}):
${pattern2.description}

Key Difference: ${pattern1.patternName} is a ${pattern1.category.name.lowercase()} pattern, while ${pattern2.patternName} is a ${pattern2.category.name.lowercase()} pattern.
        """.trimIndent()
    }
}

/**
 * Result of pattern validation.
 */
data class ValidationResult(
    val isValid: Boolean,
    val isUncertain: Boolean,
    val confidenceAdjustment: Int,  // -30 to +30
    val reasoning: String
) {
    companion object {
        fun valid(adjustment: Int, reasoning: String) = ValidationResult(true, false, adjustment, reasoning)
        fun invalid(adjustment: Int, reasoning: String) = ValidationResult(false, false, adjustment, reasoning)
        fun uncertain(adjustment: Int, reasoning: String) = ValidationResult(false, true, adjustment, reasoning)
        fun neutral(reasoning: String) = ValidationResult(false, true, 0, reasoning)
    }
}
