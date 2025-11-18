package com.lamontlabs.quantravision.intelligence.llm

/**
 * Represents the current state of the LLM model
 */
sealed class ModelState {
    /**
     * Model not yet downloaded
     */
    data object NotDownloaded : ModelState()
    
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
     * Model downloaded but not loaded into memory
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
