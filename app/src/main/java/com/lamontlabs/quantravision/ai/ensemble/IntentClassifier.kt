package com.lamontlabs.quantravision.ai.ensemble

import android.content.Context
import com.lamontlabs.quantravision.ai.ensemble.models.IntentResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.task.text.nlclassifier.NLClassifier
import timber.log.Timber
import java.io.File

/**
 * Intent Classifier for user questions using TensorFlow Lite NLClassifier
 * 
 * Classifies user questions into one of 6 intents:
 * - pattern_explanation: Questions about what a pattern means, how it works
 * - quantra_score: Questions about the Quantra Score metric
 * - trading_strategy: Questions about trading strategies, entry/exit points
 * - indicator: Questions about technical indicators (RSI, MACD, etc.)
 * - validation: Questions about pattern validation, confirmation
 * - general: General questions about trading, markets, app features
 * 
 * Thread-safe with lazy initialization using Mutex.
 */
class IntentClassifier(
    private val context: Context,
    private val modelFile: File?
) {
    private var classifier: NLClassifier? = null
    private val initMutex = Mutex()
    private var isInitialized = false
    
    companion object {
        private const val DEFAULT_INTENT = "general"
        private const val DEFAULT_CONFIDENCE = 0.5f
        
        // Known intent categories
        private val VALID_INTENTS = setOf(
            "pattern_explanation",
            "quantra_score",
            "trading_strategy",
            "indicator",
            "validation",
            "general"
        )
    }
    
    /**
     * Initialize the classifier (thread-safe)
     * 
     * @return true if initialization successful, false otherwise
     */
    private suspend fun initialize(): Boolean = initMutex.withLock {
        if (isInitialized) {
            return@withLock classifier != null
        }
        
        try {
            if (modelFile == null || !modelFile.exists()) {
                Timber.w("🎯 Intent classifier model not found - will use default intent")
                isInitialized = true
                return@withLock false
            }
            
            Timber.i("🎯 Initializing IntentClassifier from ${modelFile.absolutePath}")
            
            classifier = NLClassifier.createFromFile(modelFile)
            isInitialized = true
            
            Timber.i("🎯 IntentClassifier initialized successfully")
            return@withLock true
        } catch (e: Exception) {
            Timber.e(e, "🎯 Failed to initialize IntentClassifier")
            isInitialized = true
            return@withLock false
        }
    }
    
    /**
     * Classify a user question into an intent
     * 
     * @param question The user's question string
     * @return IntentResult with intent category and confidence score
     */
    suspend fun classify(question: String): IntentResult {
        if (question.isBlank()) {
            Timber.w("🎯 Empty question provided, returning default intent")
            return IntentResult(DEFAULT_INTENT, DEFAULT_CONFIDENCE)
        }
        
        // Ensure classifier is initialized
        val initialized = initialize()
        
        if (!initialized || classifier == null) {
            Timber.w("🎯 Classifier not available, returning default intent for: $question")
            return IntentResult(DEFAULT_INTENT, DEFAULT_CONFIDENCE)
        }
        
        return try {
            val results = classifier!!.classify(question)
            
            if (results.isEmpty()) {
                Timber.w("🎯 No classification results for question: $question")
                return IntentResult(DEFAULT_INTENT, DEFAULT_CONFIDENCE)
            }
            
            // Get top result
            val topResult = results[0]
            val intent = topResult.label
            val confidence = topResult.score
            
            // Validate intent is one of our known categories
            val validIntent = if (intent in VALID_INTENTS) {
                intent
            } else {
                Timber.w("🎯 Unknown intent '$intent', using default")
                DEFAULT_INTENT
            }
            
            Timber.d("🎯 Classified question as '$validIntent' with confidence $confidence")
            
            IntentResult(validIntent, confidence)
        } catch (e: Exception) {
            Timber.e(e, "🎯 Error classifying question: $question")
            IntentResult(DEFAULT_INTENT, DEFAULT_CONFIDENCE)
        }
    }
    
    /**
     * Check if the classifier is ready to use
     * 
     * @return true if classifier is initialized and ready
     */
    fun isReady(): Boolean {
        return isInitialized && classifier != null
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        try {
            classifier?.close()
            classifier = null
            isInitialized = false
            Timber.i("🎯 IntentClassifier closed")
        } catch (e: Exception) {
            Timber.e(e, "🎯 Error closing IntentClassifier")
        }
    }
}
