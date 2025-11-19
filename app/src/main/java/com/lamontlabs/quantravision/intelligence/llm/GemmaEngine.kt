package com.lamontlabs.quantravision.intelligence.llm

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceOptions
import com.google.mediapipe.tasks.core.BaseOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Granular initialization error types for precise failure reporting
 */
sealed class InitializationError(message: String) : Exception(message) {
    /**
     * Model file not found at expected location
     */
    class ModelNotFound(path: String) : InitializationError("Gemma model file not found at $path")
    
    /**
     * MediaPipe initialization failed
     */
    class MediaPipeLoadFailed(cause: Throwable) : InitializationError("MediaPipe LlmInference initialization failed: ${cause.message}")
    
    /**
     * Model loading failed with an error
     */
    class LoadFailed(cause: Throwable) : InitializationError("Failed to load model: ${cause.message}")
    
    /**
     * Model loader not implemented
     */
    class LoaderNotImplemented : InitializationError("Model loader not implemented for current state")
}

/**
 * MediaPipe LLM Inference engine for Gemma 2B
 * 
 * # STATE MANAGEMENT
 * 
 * This engine truthfully reports model availability through ModelState:
 * - **Ready**: MediaPipe model is loaded in memory and ready for LLM inference
 * - **Downloaded**: Model file exists but not loaded into memory
 * - **NotDownloaded**: No model file present
 * 
 * ## Initialization Contract (CRITICAL FOR VALIDATION)
 * 
 * ### initialize() returns Result.failure when:
 * - Model file does not exist (enables validation workflows to detect missing models)
 * - Model file exists but MediaPipe initialization fails
 * - Any error occurs during initialization
 * 
 * ### initialize() returns Result.success ONLY when:
 * - MediaPipe model is successfully loaded and ready for inference
 * 
 * ## Behavior by Model Availability
 * 
 * ### When NO model file exists (typical case without manual download):
 * - `initialize()` **fails** with InitializationError.ModelNotFound, state set to NotDownloaded
 * - `generate()` returns `ExplanationResult.Unavailable` with fallback text
 * - `isReady()` returns `false` (no model available)
 * - PatternExplainer uses FallbackExplanations for template-based responses
 * - **Validation workflows detect missing model and can block production**
 * 
 * ### When model file downloaded but not loaded:
 * - User must call `initialize()` to load model into memory
 * - Until then, state is `Downloaded` and `isReady()` returns false
 * - Fallback explanations still work
 * 
 * ### When MediaPipe model loaded (after initialize() succeeds):
 * - `initialize()` loads MediaPipe LlmInference and sets state to `Ready`
 * - `generate()` uses actual LLM inference for personalized explanations
 * - `isReady()` returns `true` (model loaded and ready for inference)
 * - UI receives `ExplanationResult.Success` with AI-generated content
 * 
 * ## Design Principle
 * **Fail Fast, Fall Back Gracefully**: initialize() fails when the model is missing,
 * enabling validation workflows to detect issues. However, PatternExplainer handles
 * this gracefully, allowing fallback explanations to work even when initialization fails.
 * 
 * ## Memory Efficiency
 * - Shared instance pattern: Both DevBot and QuantraBot share ONE model instance
 * - Model is loaded lazily (only when first requested)
 * - Can be unloaded to free memory when not in use
 * 
 * ## Error Handling
 * - Initialization errors are properly surfaced via Result.failure()
 * - When model unavailable, generate() returns Unavailable with fallback text
 * - State transitions are logged for debugging
 * - Errors propagate to validation workflows and UI for proper feedback
 * 
 * @see ModelState for all possible states
 * @see ExplanationResult for generation outcomes
 */
class GemmaEngine(private val context: Context) {
    
    private val modelManager = ModelManager(context)
    private var modelState: ModelState = ModelState.NotDownloaded
    
    // MediaPipe LlmInference instance (initialized when model is loaded)
    private var llmInference: LlmInference? = null
    
    init {
        modelState = modelManager.getModelState()
        Timber.i("🧠 GemmaEngine initialized - State: $modelState")
    }
    
    /**
     * Initialize MediaPipe LlmInference model
     * 
     * ## Initialization Contract
     * 
     * Returns **Result.failure** when:
     * - Model file does not exist (enables validation workflows to detect missing models)
     * - Model file exists but MediaPipe initialization fails
     * - Any error occurs during initialization
     * 
     * Returns **Result.success** ONLY when:
     * - MediaPipe model is successfully loaded and ready for inference
     * 
     * ## State Transitions
     * 
     * - No model file: Sets state to NotDownloaded, returns Result.failure
     * - Model file exists: Attempts MediaPipe load, sets state to Loading
     * - MediaPipe load succeeds: Sets state to Ready, returns Result.success
     * - MediaPipe load fails: Sets state to Error, returns Result.failure
     * 
     * ## Expected Behavior
     * 
     * This contract enables:
     * - Validation workflows to detect missing models and block production deployments
     * - Fallback explanations to still work (PatternExplainer handles failure gracefully)
     * - Proper error propagation to UI for user feedback
     * 
     * Fallbacks remain operational even when initialization fails, ensuring
     * users still receive template-based explanations.
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            modelState = modelManager.getModelState()
            
            when (modelState) {
                is ModelState.NotDownloaded -> {
                    return@withContext Result.failure(InitializationError.ModelNotFound("Model file not downloaded"))
                }
                is ModelState.Downloaded -> {
                    val modelFile = modelManager.getModelFile()
                        ?: return@withContext Result.failure(InitializationError.ModelNotFound("Model file missing"))
                    
                    Timber.i("🧠 Loading Gemma model with MediaPipe: ${modelFile.absolutePath}")
                    modelState = ModelState.Loading
                    
                    try {
                        // BUILD BaseOptions with model path
                        val baseOptions = BaseOptions.builder()
                            .setModelAssetPath(modelFile.absolutePath)
                            .setDelegate(if (ModelConfig.USE_GPU) BaseOptions.Delegate.GPU else BaseOptions.Delegate.CPU)
                            .build()
                        
                        // Build LlmInferenceOptions with BaseOptions
                        val options = LlmInferenceOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setMaxTokens(ModelConfig.MAX_OUTPUT_TOKENS)
                            .setTemperature(ModelConfig.TEMPERATURE)
                            .setTopK(ModelConfig.TOP_K)
                            .setTopP(ModelConfig.TOP_P)
                            .setRandomSeed(ModelConfig.RANDOM_SEED)
                            .build()
                        
                        // Create MediaPipe instance with proper options
                        llmInference = LlmInference.createFromOptions(context, options)
                        modelState = ModelState.Ready
                        Timber.i("🧠 GemmaEngine ready - MediaPipe model loaded and ready for inference")
                        return@withContext Result.success(Unit)
                        
                    } catch (loadError: Exception) {
                        llmInference = null
                        modelState = ModelState.Error(loadError.message ?: "MediaPipe load failed", recoverable = true)
                        val error = InitializationError.MediaPipeLoadFailed(loadError)
                        Timber.e(error, "🧠 MediaPipe initialization failed")
                        return@withContext Result.failure(error)
                    }
                }
                else -> {
                    return@withContext Result.failure(InitializationError.LoaderNotImplemented())
                }
            }
        } catch (e: Exception) {
            modelState = ModelState.Error(e.message ?: "Initialization failed", recoverable = false)
            return@withContext Result.failure(InitializationError.MediaPipeLoadFailed(e))
        }
    }
    
    /**
     * Generate text from prompt using MediaPipe LlmInference
     * 
     * Returns ExplanationResult.Unavailable when model is not loaded,
     * allowing PatternExplainer to use FallbackExplanations for template responses.
     * 
     * When model is Ready, uses MediaPipe for real AI-generated explanations.
     */
    suspend fun generate(
        prompt: String,
        maxTokens: Int = ModelConfig.MAX_OUTPUT_TOKENS
    ): ExplanationResult = withContext(Dispatchers.Default) {
        
        try {
            // Check if MediaPipe model is loaded and ready
            if (modelState !is ModelState.Ready || llmInference == null) {
                val reason = when (modelState) {
                    is ModelState.NotDownloaded -> "Model not downloaded"
                    is ModelState.Downloaded -> "Model not loaded into memory"
                    is ModelState.Loading -> "Model currently loading"
                    is ModelState.Error -> "Model error: ${(modelState as ModelState.Error).error}"
                    else -> "Model not ready"
                }
                
                return@withContext ExplanationResult.Unavailable(
                    reason = reason,
                    fallbackText = "Model not available. Using template response."
                )
            }
            
            // Model is Ready - perform MediaPipe inference
            val startTime = System.currentTimeMillis()
            val previousState = modelState
            modelState = ModelState.Generating
            
            try {
                // Generate response using MediaPipe LlmInference
                Timber.d("🧠 Generating response for prompt: ${prompt.take(100)}...")
                val response = llmInference!!.generateResponse(prompt)
                
                // Restore Ready state
                modelState = previousState
                val inferenceTime = System.currentTimeMillis() - startTime
                
                // Count tokens (approximate - count words as rough estimate)
                val tokensGenerated = response.split("\\s+".toRegex()).size
                
                Timber.i("🧠 Generated response in ${inferenceTime}ms (~$tokensGenerated tokens)")
                
                return@withContext ExplanationResult.Success(
                    text = response,
                    tokensGenerated = tokensGenerated,
                    inferenceTimeMs = inferenceTime,
                    fromCache = false
                )
                
            } catch (inferenceError: Exception) {
                // Restore previous state on error
                modelState = previousState
                Timber.e(inferenceError, "🧠 MediaPipe inference failed")
                
                return@withContext ExplanationResult.Failure(
                    error = inferenceError.message ?: "Inference failed",
                    fallbackText = null
                )
            }
            
        } catch (e: Exception) {
            modelState = ModelState.Error(e.message ?: "Generation failed", recoverable = true)
            Timber.e(e, "🧠 Text generation failed")
            
            ExplanationResult.Failure(
                error = e.message ?: "Unknown error",
                fallbackText = null
            )
        }
    }
    
    /**
     * Get current model state
     */
    fun getState(): ModelState = modelState
    
    /**
     * Check if MediaPipe model is loaded and ready for LLM inference
     * 
     * Returns true ONLY when modelState is Ready, indicating that MediaPipe model
     * is loaded in memory and ready to generate AI explanations.
     * 
     * Returns false when:
     * - No model downloaded (NotDownloaded)
     * - Model downloaded but not loaded (Downloaded)
     * - Model is loading (Loading)
     * - Model encountered an error (Error)
     */
    fun isReady(): Boolean {
        return modelState is ModelState.Ready && llmInference != null
    }
    
    /**
     * Download model (reserved for future automated download)
     * 
     * Currently delegates to ModelManager, which returns failure for Kaggle models.
     * Users must follow manual download instructions in DOWNLOAD_INSTRUCTIONS.md
     */
    suspend fun downloadModel(
        onProgress: (ModelState.Downloading) -> Unit
    ): Result<File> {
        return modelManager.downloadModel(onProgress)
    }
    
    /**
     * Unload MediaPipe model from memory to free resources
     * 
     * Useful for memory management when model is not actively needed.
     * Call initialize() again to reload the model.
     */
    fun unload() {
        llmInference?.close()
        llmInference = null
        
        // Update state to Downloaded if model file still exists
        modelState = if (modelManager.getModelFile() != null) {
            ModelState.Downloaded
        } else {
            ModelState.NotDownloaded
        }
        
        Timber.i("🧠 MediaPipe model unloaded from memory - State: $modelState")
    }
    
    companion object {
        private const val TAG = "GemmaEngine"
        
        /**
         * Shared instance for memory efficiency
         * Both DevBot and QuantraBot should share ONE GemmaEngine instance
         * to avoid loading the 1.5GB model multiple times in memory.
         */
        @Volatile
        private var sharedInstance: GemmaEngine? = null
        
        /**
         * Get or create shared GemmaEngine instance
         * 
         * This ensures only ONE model is loaded in memory, even when used by
         * both DevBot and QuantraBot simultaneously.
         */
        @Synchronized
        fun getInstance(context: Context): GemmaEngine {
            return sharedInstance ?: GemmaEngine(context.applicationContext).also {
                sharedInstance = it
            }
        }
    }
}
