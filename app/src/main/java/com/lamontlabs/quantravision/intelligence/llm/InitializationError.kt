package com.lamontlabs.quantravision.intelligence.llm

/**
 * Structured errors for AI model initialization failures
 * 
 * Provides specific error types for different initialization scenarios:
 * - ModelNotFound: Required model files are missing
 * - PartialModels: Some but not all required models are available
 * - LoadFailed: Models exist but failed to load (memory, corruption, etc.)
 * - LoaderNotImplemented: Model loader component not available
 * 
 * Used by EnsembleEngine and PatternExplainer to enable graceful degradation
 * when AI models cannot be initialized.
 */
sealed class InitializationError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Required model files not found
     * User needs to download/import models
     */
    class ModelNotFound(message: String) : InitializationError(message)
    
    /**
     * Some models found but not all required ones
     * Includes counts and missing model names for error reporting
     * 
     * @param imported Number of models successfully imported
     * @param total Total number of required models
     * @param missing List of missing model names (e.g., ["sentence_embeddings.tflite", "mobilebert_qa.tflite"])
     */
    data class PartialModels(
        val imported: Int,
        val total: Int,
        val missing: List<String>
    ) : InitializationError("Only $imported/$total required models available. Missing: ${missing.joinToString()}")
    
    /**
     * Model files exist but failed to load
     * Could be due to memory, corruption, incompatible format, etc.
     */
    class LoadFailed(cause: Throwable) : InitializationError(
        "Failed to load AI models: ${cause.message}",
        cause
    )
    
    /**
     * Model loader component not implemented or unavailable
     * Fallback to non-AI explanations
     */
    class LoaderNotImplemented(reason: String = "Model loader not available") : InitializationError(reason)
}
