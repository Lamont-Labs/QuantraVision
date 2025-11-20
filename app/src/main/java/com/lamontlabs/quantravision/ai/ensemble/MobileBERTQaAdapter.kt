package com.lamontlabs.quantravision.ai.ensemble

import com.lamontlabs.quantravision.ai.ensemble.models.QAResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.tensorflow.lite.task.text.qa.BertQuestionAnswerer
import org.tensorflow.lite.task.text.qa.QaAnswer
import timber.log.Timber
import java.io.File

/**
 * MobileBERT Q&A adapter for generative question answering
 * 
 * Wraps TensorFlow Lite BertQuestionAnswerer to provide extractive Q&A
 * from given context (pattern information, indicators, etc.)
 * 
 * This serves as a fallback when:
 * - No high-confidence match found in knowledge base
 * - User asks novel questions requiring contextual reasoning
 * 
 * Thread-safe with lazy initialization using Mutex.
 */
class MobileBERTQaAdapter(
    private val modelFile: File?
) {
    private var answerer: BertQuestionAnswerer? = null
    private val initMutex = Mutex()
    private var isInitialized = false
    
    companion object {
        private const val MIN_CONFIDENCE = 0.3f
        private const val DEFAULT_ANSWER = "I don't have enough information to answer that question."
        private const val ERROR_ANSWER = "I encountered an error while processing your question."
    }
    
    /**
     * Initialize the Q&A answerer (thread-safe)
     * 
     * @return true if initialization successful, false otherwise
     */
    private suspend fun initialize(): Boolean = initMutex.withLock {
        if (isInitialized) {
            return@withLock answerer != null
        }
        
        try {
            if (modelFile == null || !modelFile.exists()) {
                Timber.w("🤖 MobileBERT Q&A model not found - generative Q&A disabled")
                isInitialized = true
                return@withLock false
            }
            
            Timber.i("🤖 Initializing MobileBERTQaAdapter from ${modelFile.absolutePath}")
            
            answerer = BertQuestionAnswerer.createFromFile(modelFile)
            isInitialized = true
            
            Timber.i("🤖 MobileBERTQaAdapter initialized successfully")
            return@withLock true
        } catch (e: Exception) {
            Timber.e(e, "🤖 Failed to initialize MobileBERTQaAdapter")
            isInitialized = true
            return@withLock false
        }
    }
    
    /**
     * Answer a question using the provided context
     * 
     * @param context Context text containing information to answer from
     *                (e.g., pattern description, indicator values, market conditions)
     * @param question The user's question
     * @return QAResult with answer, confidence, and source indicator
     */
    suspend fun answer(context: String, question: String): QAResult {
        if (question.isBlank()) {
            Timber.w("🤖 Empty question provided")
            return QAResult(
                answer = "Please ask a specific question.",
                confidence = 0f,
                fromModel = false
            )
        }
        
        if (context.isBlank()) {
            Timber.w("🤖 Empty context provided for question: $question")
            return QAResult(
                answer = DEFAULT_ANSWER,
                confidence = 0f,
                fromModel = false
            )
        }
        
        // Ensure answerer is initialized
        val initialized = initialize()
        
        if (!initialized || answerer == null) {
            Timber.w("🤖 Answerer not available for question: $question")
            return QAResult(
                answer = DEFAULT_ANSWER,
                confidence = 0f,
                fromModel = false
            )
        }
        
        return try {
            val results = answerer!!.answer(context, question)
            
            if (results.isEmpty()) {
                Timber.w("🤖 No answers generated for question: $question")
                return QAResult(
                    answer = DEFAULT_ANSWER,
                    confidence = 0f,
                    fromModel = false
                )
            }
            
            // Get top answer
            val topAnswer = results[0]
            val answerText = topAnswer.text
            val confidence = topAnswer.pos.logit // Confidence score from model
            
            // Check if confidence meets minimum threshold
            if (confidence < MIN_CONFIDENCE || answerText.isBlank()) {
                Timber.d("🤖 Low confidence answer ($confidence) for question: $question")
                return QAResult(
                    answer = DEFAULT_ANSWER,
                    confidence = confidence,
                    fromModel = true
                )
            }
            
            Timber.i("🤖 Generated answer with confidence $confidence for: $question")
            
            QAResult(
                answer = formatAnswer(answerText, confidence),
                confidence = confidence,
                fromModel = true
            )
        } catch (e: Exception) {
            Timber.e(e, "🤖 Error generating answer for question: $question")
            QAResult(
                answer = ERROR_ANSWER,
                confidence = 0f,
                fromModel = false
            )
        }
    }
    
    /**
     * Answer a question using multiple context sources
     * 
     * Combines multiple context strings and generates answer
     * Useful when answer may come from different sources
     * (e.g., pattern info + indicators + market conditions)
     * 
     * @param contexts List of context strings
     * @param question The user's question
     * @return QAResult with answer, confidence, and source indicator
     */
    suspend fun answerWithMultipleContexts(
        contexts: List<String>,
        question: String
    ): QAResult {
        if (contexts.isEmpty()) {
            return answer("", question)
        }
        
        // Combine contexts with clear separation
        val combinedContext = contexts
            .filter { it.isNotBlank() }
            .joinToString("\n\n")
        
        return answer(combinedContext, question)
    }
    
    /**
     * Format answer with confidence score
     * 
     * @param answer Raw answer text
     * @param confidence Confidence score
     * @return Formatted answer string
     */
    private fun formatAnswer(answer: String, confidence: Float): String {
        // Clean up the answer text
        val cleanAnswer = answer.trim()
        
        // Add confidence indicator for low-confidence answers
        return when {
            confidence >= 0.7f -> cleanAnswer
            confidence >= 0.5f -> "$cleanAnswer (Moderate confidence)"
            confidence >= MIN_CONFIDENCE -> "$cleanAnswer (Low confidence - verify this information)"
            else -> DEFAULT_ANSWER
        }
    }
    
    /**
     * Check if the answerer is ready to use
     * 
     * @return true if answerer is initialized and ready
     */
    fun isReady(): Boolean {
        return isInitialized && answerer != null
    }
    
    /**
     * Get answer candidates for debugging/transparency
     * 
     * Returns multiple answer candidates with their confidence scores
     * 
     * @param context Context text
     * @param question User's question
     * @param maxResults Maximum number of results to return
     * @return List of QA answers with positions and scores
     */
    suspend fun getAnswerCandidates(
        context: String,
        question: String,
        maxResults: Int = 3
    ): List<QaAnswer> {
        if (!isReady()) {
            return emptyList()
        }
        
        return try {
            val results = answerer!!.answer(context, question)
            results.take(maxResults)
        } catch (e: Exception) {
            Timber.e(e, "🤖 Error getting answer candidates")
            emptyList()
        }
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        try {
            answerer?.close()
            answerer = null
            isInitialized = false
            Timber.i("🤖 MobileBERTQaAdapter closed")
        } catch (e: Exception) {
            Timber.e(e, "🤖 Error closing MobileBERTQaAdapter")
        }
    }
}
