package com.lamontlabs.quantravision.intelligence.llm

/**
 * Configuration for Gemma 2B model
 * 
 * Model Details:
 * - Name: Gemma 2B Instruct (Quantized INT8)
 * - Size: ~1.5GB on disk
 * - License: Apache 2.0 (commercial use allowed)
 * - Context: 8192 tokens
 * - Optimized for: Mobile inference
 */
object ModelConfig {
    
    // Model file configuration
    const val MODEL_NAME = "gemma-2b-it-gpu-int8.tflite"
    const val MODEL_URL = "https://huggingface.co/google/gemma-2b-it/resolve/main/gemma-2b-it-gpu-int8.tflite"
    const val MODEL_SIZE_BYTES = 1_500_000_000L  // ~1.5GB
    
    // Inference parameters
    const val MAX_OUTPUT_TOKENS = 200  // Keep responses concise for speed
    const val TEMPERATURE = 0.7f       // Balanced creativity vs consistency
    const val TOP_K = 40               // Token sampling diversity
    const val TOP_P = 0.9f             // Nucleus sampling threshold
    
    // Performance tuning
    const val NUM_THREADS = 4          // CPU threads for inference
    const val USE_XNNPACK = true       // CPU acceleration delegate
    
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
