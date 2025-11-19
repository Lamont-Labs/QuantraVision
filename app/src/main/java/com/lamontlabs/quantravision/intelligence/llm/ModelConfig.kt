package com.lamontlabs.quantravision.intelligence.llm

/**
 * Configuration for Gemma 3 1B model with MediaPipe LLM Inference API
 * 
 * Model Details:
 * - Name: Gemma 3 1B Instruct (INT4 Quantized)
 * - Format: MediaPipe .task file (includes model + tokenizer + metadata)
 * - Size: ~529MB on disk (MUCH smaller than Gemma 2B!)
 * - License: Apache 2.0 (commercial use allowed)
 * - Context: 8192 tokens
 * - Optimized for: Mobile GPU inference via MediaPipe
 * 
 * Download Instructions:
 * See app/src/main/assets/models/DOWNLOAD_INSTRUCTIONS.md for complete setup guide
 */
object ModelConfig {
    
    // Model file configuration
    const val MODEL_NAME = "gemma-3-1b-it-int4.task"
    const val MODEL_URL = "https://huggingface.co/litert-community/Gemma3-1B-IT"  // Manual download required
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
