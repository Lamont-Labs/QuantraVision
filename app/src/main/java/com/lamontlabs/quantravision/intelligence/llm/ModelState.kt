package com.lamontlabs.quantravision.intelligence.llm

/**
 * Model type identifier for tracking individual model files
 */
enum class ModelType {
    INTENT_CLASSIFIER,      // Intent classification model
    SENTENCE_EMBEDDINGS,    // Sentence embeddings model
    MOBILEBERT_QA          // MobileBERT Q&A model
}

/**
 * Represents the current state of the LLM model
 */
sealed class ModelState {
    /**
     * Model not yet downloaded
     */
    data object NotDownloaded : ModelState()
    
    /**
     * Some but not all required models are downloaded
     * @param importedCount Number of models imported (1 or missing required models)
     * @param totalRequired Total number of models required (2: embeddings + mobilebert, intent classifier is optional)
     * @param importedModels Set of model types that are imported
     */
    data class PartiallyDownloaded(
        val importedCount: Int,
        val totalRequired: Int = 2,
        val importedModels: Set<ModelType>
    ) : ModelState()
    
    /**
     * Model is being downloaded
     * @param progress Download progress (0.0 to 1.0)
     * @param bytesDownloaded Bytes downloaded so far
     */
    data class Downloading(
        val progress: Float,
        val bytesDownloaded: Long
    ) : ModelState()
    
    /**
     * All models downloaded but not loaded into memory
     */
    data object Downloaded : ModelState()
    
    /**
     * Model is being loaded into memory
     */
    data object Loading : ModelState()
    
    /**
     * Model ready for inference
     */
    data object Ready : ModelState()
    
    /**
     * Model encountered an error
     * @param error Error description
     * @param recoverable Whether the error can be recovered from
     */
    data class Error(
        val error: String,
        val recoverable: Boolean = true
    ) : ModelState()
    
    /**
     * Model is currently generating a response
     */
    data object Generating : ModelState()
}

/**
 * Result of an explanation generation request
 */
sealed class ExplanationResult {
    /**
     * Successfully generated explanation
     * @param text The generated explanation text
     * @param tokensGenerated Number of tokens in response
     * @param inferenceTimeMs Time taken to generate (milliseconds)
     * @param fromCache Whether this was retrieved from cache
     */
    data class Success(
        val text: String,
        val tokensGenerated: Int,
        val inferenceTimeMs: Long,
        val fromCache: Boolean = false
    ) : ExplanationResult()
    
    /**
     * Generation failed
     * @param error Error description
     * @param fallbackText Optional fallback explanation
     */
    data class Failure(
        val error: String,
        val fallbackText: String? = null
    ) : ExplanationResult()
    
    /**
     * Model not available
     * @param reason Why model is unavailable
     * @param fallbackText Template-based fallback
     */
    data class Unavailable(
        val reason: String,
        val fallbackText: String
    ) : ExplanationResult()
}
