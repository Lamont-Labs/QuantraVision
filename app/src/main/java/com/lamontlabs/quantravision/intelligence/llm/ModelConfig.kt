package com.lamontlabs.quantravision.intelligence.llm

/**
 * Configuration for ensemble AI models with MediaPipe LLM Inference API
 * 
 * Model Details:
 * - Intent Classifier: TFLite model for intent classification
 * - Sentence Embeddings: TFLite model for semantic similarity
 * - MobileBERT Q&A: TFLite model for question answering
 * - Format: TFLite files for mobile inference
 * - Combined Size: ~150MB on disk
 * - License: Apache 2.0 (commercial use allowed)
 * - Optimized for: Mobile CPU/GPU inference via TFLite
 * 
 * Download Instructions:
 * See app/src/main/assets/models/ENSEMBLE_MODEL_DOWNLOADS.md for complete setup guide
 */
object ModelConfig {
    
    // Model file configuration - 3 separate models for ensemble approach
    const val INTENT_CLASSIFIER_NAME = "intent_classifier.tflite"
    const val SENTENCE_EMBEDDINGS_NAME = "sentence_embeddings.tflite"
    const val MOBILEBERT_QA_NAME = "mobilebert_qa_squad.tflite"
    
    // Individual model sizes
    const val INTENT_CLASSIFIER_SIZE_BYTES = 15_000_000L  // ~15MB
    const val SENTENCE_EMBEDDINGS_SIZE_BYTES = 25_000_000L  // ~25MB
    const val MOBILEBERT_QA_SIZE_BYTES = 110_000_000L  // ~110MB
    
    // Legacy single model configuration (kept for backward compatibility)
    @Deprecated("Use individual model names instead", ReplaceWith("INTENT_CLASSIFIER_NAME, SENTENCE_EMBEDDINGS_NAME, MOBILEBERT_QA_NAME"))
    const val MODEL_NAME = "gemma-3-1b-it-int4.task"
    @Deprecated("No longer used with ensemble models")
    const val MODEL_URL = "https://huggingface.co/litert-community/Gemma3-1B-IT"
    @Deprecated("Use individual model sizes instead", ReplaceWith("INTENT_CLASSIFIER_SIZE_BYTES, SENTENCE_EMBEDDINGS_SIZE_BYTES, MOBILEBERT_QA_SIZE_BYTES"))
    const val MODEL_SIZE_BYTES = 529_000_000L  // ~529MB
    
    // MediaPipe LLM Inference parameters
    const val MAX_OUTPUT_TOKENS = 200  // Keep responses concise for speed
    const val TEMPERATURE = 0.7f       // Balanced creativity vs consistency
    const val TOP_K = 40               // Token sampling diversity
    const val TOP_P = 0.9f             // Nucleus sampling threshold
    const val RANDOM_SEED = 42         // Reproducible generation (0 = random)
    
    // Performance tuning for MediaPipe
    const val NUM_THREADS = 4          // CPU threads for inference
    const val USE_GPU = true           // Enable GPU acceleration (recommended)
    
    // Cache configuration
    const val CACHE_EXPLANATIONS = true
    const val MAX_CACHE_SIZE = 100     // Number of cached explanations
    const val CACHE_TTL_HOURS = 24     // Refresh cache daily
    
    // Model loading strategy
    const val LAZY_LOAD = true         // Only load when first requested
    const val PRELOAD_ON_WIFI = true   // Download model on WiFi
    
    // Tokenizer configuration
    const val VOCAB_SIZE = 256000      // Gemma vocab size
    const val PAD_TOKEN_ID = 0
    const val BOS_TOKEN_ID = 2         // Beginning of sequence
    const val EOS_TOKEN_ID = 1         // End of sequence
    
    // Response formatting
    const val INCLUDE_REASONING = true
    const val USE_MARKDOWN = false     // Plain text for notifications
    const val MAX_RESPONSE_LENGTH = 500 // Character limit for UI
}
