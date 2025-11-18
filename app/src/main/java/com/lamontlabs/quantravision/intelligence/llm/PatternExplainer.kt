package com.lamontlabs.quantravision.intelligence.llm

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import com.lamontlabs.quantravision.intelligence.QuantraScorer
import com.lamontlabs.quantravision.learning.adaptive.PatternLearningEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * High-level API for generating pattern explanations
 * 
 * Coordinates between:
 * - GemmaEngine (LLM inference)
 * - PromptBuilder (prompt engineering)
 * - FallbackExplanations (graceful degradation)
 * - ExplanationCache (performance optimization)
 * 
 * Usage:
 * ```
 * val explainer = PatternExplainer(context)
 * val explanation = explainer.explainPattern(patternMatch, indicators, scoreResult)
 * ```
 */
class PatternExplainer(private val context: Context) {
    
    private val gemmaEngine = GemmaEngine(context)
    private val promptBuilder = PromptBuilder()
    private val cache = ExplanationCache(context)
    
    private var initialized = false
    
    /**
     * Initialize the explainer (loads model if available)
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            gemmaEngine.initialize()
            initialized = true
            Timber.i("🧠 PatternExplainer initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "🧠 PatternExplainer initialization failed")
            Result.failure(e)
        }
    }
    
    /**
     * Explain a detected pattern
     * 
     * Returns AI-generated explanation if model is available,
     * otherwise falls back to template-based explanations.
     */
    suspend fun explainPattern(
        pattern: PatternMatch,
        indicators: IndicatorContext? = null,
        scoreResult: QuantraScorer.ScoreResult? = null,
        learningPhase: String? = null,
        totalScans: Int? = null
    ): ExplanationResult = withContext(Dispatchers.Default) {
        
        // Check cache first
        val cacheKey = buildCacheKey(pattern, indicators, scoreResult)
        cache.get(cacheKey)?.let { cached ->
            Timber.d("🧠 Explanation retrieved from cache")
            return@withContext ExplanationResult.Success(
                text = cached,
                tokensGenerated = 0,
                inferenceTimeMs = 0,
                fromCache = true
            )
        }
        
        // Try LLM generation if model is ready
        if (gemmaEngine.isReady()) {
            val prompt = promptBuilder.buildPatternExplanation(
                pattern, indicators, scoreResult, learningPhase, totalScans
            )
            
            when (val result = gemmaEngine.generate(prompt)) {
                is ExplanationResult.Success -> {
                    // Cache successful explanation
                    cache.put(cacheKey, result.text)
                    return@withContext result
                }
                is ExplanationResult.Failure -> {
                    Timber.w("🧠 LLM generation failed: ${result.error}, using fallback")
                }
                is ExplanationResult.Unavailable -> {
                    Timber.d("🧠 LLM unavailable: ${result.reason}, using fallback")
                }
            }
        }
        
        // Fallback to template-based explanations
        val fallbackText = FallbackExplanations.getPatternExplanation(
            patternName = pattern.patternName,
            quantraScore = pattern.quantraScore,
            hasRSI = indicators?.rsi != null,
            hasMACD = indicators?.macd != null,
            hasVolume = indicators?.volume != null
        )
        
        // Cache fallback too (avoid recomputing)
        cache.put(cacheKey, fallbackText)
        
        ExplanationResult.Unavailable(
            reason = "Model not available",
            fallbackText = fallbackText
        )
    }
    
    /**
     * Answer educational question
     */
    suspend fun answerQuestion(question: String): ExplanationResult = withContext(Dispatchers.Default) {
        
        // Try LLM if ready
        if (gemmaEngine.isReady()) {
            val prompt = promptBuilder.buildEducationalQuestion(question)
            when (val result = gemmaEngine.generate(prompt)) {
                is ExplanationResult.Success -> return@withContext result
                else -> { /* Fall through to fallback */ }
            }
        }
        
        // Fallback
        val fallbackText = FallbackExplanations.getEducationalAnswer(question)
        ExplanationResult.Unavailable(
            reason = "Model not available",
            fallbackText = fallbackText
        )
    }
    
    /**
     * Generate weekly scan summary
     */
    suspend fun generateWeeklySummary(
        totalScans: Int,
        patternsFound: Map<String, Int>,
        avgQuantraScore: Int,
        learningProgress: String
    ): ExplanationResult = withContext(Dispatchers.Default) {
        
        // Try LLM if ready
        if (gemmaEngine.isReady()) {
            val prompt = promptBuilder.buildWeeklySummary(
                totalScans, patternsFound, avgQuantraScore, learningProgress
            )
            when (val result = gemmaEngine.generate(prompt, maxTokens = 150)) {
                is ExplanationResult.Success -> return@withContext result
                else -> { /* Fall through to fallback */ }
            }
        }
        
        // Fallback
        val fallbackText = FallbackExplanations.getWeeklySummary(totalScans, avgQuantraScore)
        ExplanationResult.Unavailable(
            reason = "Model not available",
            fallbackText = fallbackText
        )
    }
    
    /**
     * Get model download progress
     */
    suspend fun downloadModel(
        onProgress: (ModelState.Downloading) -> Unit
    ): Result<Boolean> {
        return try {
            gemmaEngine.downloadModel(onProgress)
            initialize()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if LLM is ready
     */
    fun isLLMReady(): Boolean = gemmaEngine.isReady()
    
    /**
     * Get current model state
     */
    fun getModelState(): ModelState = gemmaEngine.getState()
    
    /**
     * Build cache key from pattern data
     */
    private fun buildCacheKey(
        pattern: PatternMatch,
        indicators: IndicatorContext?,
        scoreResult: QuantraScorer.ScoreResult?
    ): String {
        return buildString {
            append(pattern.patternName)
            append(":")
            append(pattern.quantraScore)
            append(":")
            append(indicators?.rsi?.toInt() ?: "none")
            append(":")
            append(scoreResult?.grade?.label ?: "none")
        }
    }
}
