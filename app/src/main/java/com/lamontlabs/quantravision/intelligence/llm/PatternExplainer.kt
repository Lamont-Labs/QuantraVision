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
 * - GemmaEngine (TFLite LLM inference when model loaded)
 * - PromptBuilder (prompt engineering)
 * - FallbackExplanations (graceful template-based fallbacks)
 * - ExplanationCache (performance optimization)
 * 
 * ## Behavior
 * 
 * When TFLite model is loaded and ready:
 * - Returns `ExplanationResult.Success` with AI-generated explanations
 * - UI can display these as "AI-powered" explanations
 * 
 * When model is unavailable (typical current case):
 * - Returns `ExplanationResult.Unavailable` with template-based fallback text
 * - UI can display these as standard pattern explanations (not AI-generated)
 * - Fallback quality is high - uses curated educational content
 * 
 * The result type allows UI to accurately distinguish between AI-generated
 * and template-based explanations, ensuring truthful presentation to users.
 * 
 * Usage:
 * ```
 * val explainer = PatternExplainer(context)
 * when (val result = explainer.explainPattern(pattern, indicators, score)) {
 *     is ExplanationResult.Success -> displayAIExplanation(result.text)
 *     is ExplanationResult.Unavailable -> displayTemplateExplanation(result.fallbackText)
 *     is ExplanationResult.Failure -> handleError(result.error)
 * }
 * ```
 */
class PatternExplainer(private val context: Context) {
    
    private val gemmaEngine = GemmaEngine.getInstance(context)
    private val promptBuilder = PromptBuilder()
    private val cache = ExplanationCache(context)
    
    private var initialized = false
    private var initializationErrorReason: String = "Model not initialized"
    
    /**
     * Initialize the explainer
     * 
     * ## Initialization Contract
     * 
     * Returns **Result.failure** when:
     * - GemmaEngine.initialize() fails (model missing or not loaded)
     * - This enables validation workflows to detect missing models
     * 
     * Returns **Result.success** ONLY when:
     * - GemmaEngine successfully loads TFLite model (future implementation)
     * 
     * ## Graceful Degradation
     * 
     * Even when initialization fails:
     * - explainPattern() still works using FallbackExplanations
     * - Users receive high-quality template-based explanations
     * - No crashes or error dialogs
     * 
     * This contract enables validation to detect missing models while ensuring
     * the user experience remains seamless through fallback explanations.
     * 
     * @return Result.success when TFLite model is loaded and ready
     * @return Result.failure when model is missing or cannot be loaded
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            val result = gemmaEngine.initialize()
            
            // Only set initialized if engine initialization succeeded
            if (result.isSuccess) {
                initialized = true
                initializationErrorReason = ""  // Clear any previous error
                Timber.i("🧠 PatternExplainer initialized successfully - TFLite model ready")
                Result.success(Unit)
            } else {
                // Initialization failed, but fallbacks will still work
                initialized = false
                val error = result.exceptionOrNull() ?: Exception("Engine initialization failed")
                // Capture the specific error message for use in explanations
                initializationErrorReason = error.message ?: "Unknown initialization error"
                Timber.w(error, "🧠 GemmaEngine initialization failed: ${error.message}. Fallback explanations will be used.")
                Result.failure(error)
            }
        } catch (e: Exception) {
            initialized = false
            initializationErrorReason = e.message ?: "Unknown initialization error"
            Timber.e(e, "🧠 PatternExplainer initialization failed with exception")
            Result.failure(e)
        }
    }
    
    /**
     * Explain a detected pattern
     * 
     * Returns different result types based on model availability:
     * 
     * - **ExplanationResult.Success**: When TFLite model is loaded and generates
     *   an AI-powered explanation. UI should present this as AI-generated content.
     * 
     * - **ExplanationResult.Unavailable**: When model is not loaded. Contains
     *   high-quality template-based fallback text. UI should present this as
     *   standard educational content (not AI-generated).
     * 
     * - **ExplanationResult.Failure**: When generation fails with an error.
     *   May include fallback text for graceful degradation.
     * 
     * The result type enables the UI to accurately distinguish between
     * AI-generated and template-based explanations.
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
        
        // Return with specific initialization error reason
        ExplanationResult.Unavailable(
            reason = initializationErrorReason,
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
            reason = initializationErrorReason,
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
            reason = initializationErrorReason,
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
     * Check if TFLite LLM model is loaded and ready for inference
     * 
     * Returns true ONLY when a TFLite model is loaded in memory.
     * Returns false when model is not downloaded, downloaded but not loaded,
     * or in an error state.
     * 
     * This can be used by UI to show model status or prompt model download.
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
