package com.lamontlabs.quantravision.ai.ensemble

import com.lamontlabs.quantravision.ai.ensemble.knowledge.KnowledgeBase
import com.lamontlabs.quantravision.ai.ensemble.models.QAEntry
import com.lamontlabs.quantravision.ai.ensemble.models.RetrievalResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt

/**
 * Embeddings-based retrieval for Q&A using sentence-transformers (all-MiniLM-L6-v2)
 * 
 * Uses semantic similarity to find the best matching answer from the knowledge base.
 * Pre-computes embeddings for all Q&A entries during initialization for fast retrieval.
 * 
 * Returns match only if cosine similarity > 0.75 threshold.
 * 
 * Thread-safe with lazy initialization using Mutex.
 */
class EmbeddingsRetriever(
    private val modelFile: File?,
    private val knowledgeBase: KnowledgeBase
) {
    private var interpreter: Interpreter? = null
    private val initMutex = Mutex()
    private var isInitialized = false
    
    // Cached embeddings for all Q&A entries
    private val embeddingsCache = mutableMapOf<String, FloatArray>()
    private var qaEntries: List<QAEntry> = emptyList()
    
    companion object {
        private const val SIMILARITY_THRESHOLD = 0.75f
        private const val EMBEDDING_DIM = 384 // all-MiniLM-L6-v2 output dimension
        private const val MAX_SEQUENCE_LENGTH = 128 // Maximum input tokens
    }
    
    /**
     * Initialize the retriever (thread-safe)
     * Pre-computes embeddings for all Q&A entries
     * 
     * @return true if initialization successful, false otherwise
     */
    private suspend fun initialize(): Boolean = initMutex.withLock {
        if (isInitialized) {
            return@withLock interpreter != null
        }
        
        try {
            if (modelFile == null || !modelFile.exists()) {
                Timber.w("🔍 Sentence embeddings model not found - retrieval disabled")
                isInitialized = true
                return@withLock false
            }
            
            Timber.i("🔍 Initializing EmbeddingsRetriever from ${modelFile.absolutePath}")
            
            // Initialize TFLite interpreter
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            interpreter = Interpreter(modelFile, options)
            
            // Load all Q&A entries from knowledge base
            qaEntries = withContext(Dispatchers.IO) {
                knowledgeBase.loadAll()
            }
            
            if (qaEntries.isEmpty()) {
                Timber.e("🔍 CRITICAL: Knowledge base is empty! Retrieval will retry on next call.")
                // Don't set isInitialized - allow retry on next retrieve() call
                return@withLock false
            }
            
            Timber.i("🔍 Pre-computing embeddings for ${qaEntries.size} Q&A entries...")
            
            // Pre-compute embeddings for all questions
            var successCount = 0
            for (entry in qaEntries) {
                try {
                    val embedding = generateEmbedding(entry.question)
                    if (embedding != null) {
                        embeddingsCache[entry.question] = embedding
                        successCount++
                    }
                } catch (e: Exception) {
                    Timber.w(e, "🔍 Failed to generate embedding for: ${entry.question}")
                }
            }
            
            if (successCount == 0) {
                Timber.e("🔍 CRITICAL: Failed to generate any embeddings! Retrieval will retry on next call.")
                // Don't set isInitialized - allow retry on next retrieve() call
                return@withLock false
            }
            
            isInitialized = true
            Timber.i("🔍 EmbeddingsRetriever initialized successfully ($successCount/${qaEntries.size} embeddings cached)")
            return@withLock true
        } catch (e: Exception) {
            Timber.e(e, "🔍 Failed to initialize EmbeddingsRetriever")
            isInitialized = true
            return@withLock false
        }
    }
    
    /**
     * Generate embedding for a text using the sentence-transformers model
     * 
     * @param text Input text
     * @return 384-dimensional embedding vector, or null if failed
     */
    private fun generateEmbedding(text: String): FloatArray? {
        if (interpreter == null || text.isBlank()) {
            return null
        }
        
        return try {
            // Tokenize and prepare input
            val inputTokens = tokenize(text)
            
            // Create input tensor (shape: [1, MAX_SEQUENCE_LENGTH])
            val inputBuffer = ByteBuffer.allocateDirect(4 * MAX_SEQUENCE_LENGTH).apply {
                order(ByteOrder.nativeOrder())
                inputTokens.forEach { putInt(it) }
            }
            
            // Create output tensor (shape: [1, EMBEDDING_DIM])
            val outputBuffer = ByteBuffer.allocateDirect(4 * EMBEDDING_DIM).apply {
                order(ByteOrder.nativeOrder())
            }
            
            // Run inference
            interpreter!!.run(inputBuffer, outputBuffer)
            
            // Extract embedding
            outputBuffer.rewind()
            val embedding = FloatArray(EMBEDDING_DIM) {
                outputBuffer.getFloat()
            }
            
            // Normalize embedding (L2 normalization)
            normalizeEmbedding(embedding)
            
            embedding
        } catch (e: Exception) {
            Timber.e(e, "🔍 Error generating embedding for text: $text")
            null
        }
    }
    
    /**
     * Simple tokenization for BERT-based models
     * 
     * NOTE: This is a simplified tokenizer. Production use should employ
     * proper BERT tokenization with WordPiece vocabulary.
     * 
     * @param text Input text
     * @return Array of token IDs (padded/truncated to MAX_SEQUENCE_LENGTH)
     */
    private fun tokenize(text: String): IntArray {
        // Simplified tokenization: convert to lowercase, split by whitespace
        val tokens = text.lowercase().trim().split(Regex("\\s+"))
            .take(MAX_SEQUENCE_LENGTH - 2) // Reserve space for [CLS] and [SEP]
        
        // Create token IDs (using character-based hashing as placeholder)
        // [CLS] = 101, [SEP] = 102, [PAD] = 0 (standard BERT tokens)
        val tokenIds = IntArray(MAX_SEQUENCE_LENGTH)
        tokenIds[0] = 101 // [CLS]
        
        tokens.forEachIndexed { index, token ->
            // Simple hash-based token ID (placeholder for real vocabulary lookup)
            tokenIds[index + 1] = (token.hashCode() % 30000).coerceAtLeast(103)
        }
        
        tokenIds[tokens.size + 1] = 102 // [SEP]
        // Rest are [PAD] = 0 (already initialized)
        
        return tokenIds
    }
    
    /**
     * Normalize embedding vector to unit length (L2 normalization)
     * 
     * @param embedding Embedding vector to normalize (modified in-place)
     */
    private fun normalizeEmbedding(embedding: FloatArray) {
        var sumSquares = 0.0
        for (value in embedding) {
            sumSquares += value * value
        }
        val norm = sqrt(sumSquares).toFloat()
        
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }
    }
    
    /**
     * Compute cosine similarity between two normalized embeddings
     * 
     * Since embeddings are normalized, this is just the dot product.
     * 
     * @param embedding1 First embedding (normalized)
     * @param embedding2 Second embedding (normalized)
     * @return Cosine similarity score [0, 1]
     */
    private fun cosineSimilarity(embedding1: FloatArray, embedding2: FloatArray): Float {
        if (embedding1.size != embedding2.size) {
            return 0f
        }
        
        var dotProduct = 0f
        for (i in embedding1.indices) {
            dotProduct += embedding1[i] * embedding2[i]
        }
        
        // Clamp to [0, 1] range (similarity, not distance)
        return dotProduct.coerceIn(0f, 1f)
    }
    
    /**
     * Retrieve best matching answer for a question using semantic similarity
     * 
     * @param question User's question
     * @return RetrievalResult if similarity > threshold, null otherwise
     */
    suspend fun retrieve(question: String): RetrievalResult? {
        if (question.isBlank()) {
            Timber.w("🔍 Empty question provided")
            return null
        }
        
        // Ensure retriever is initialized
        val initialized = initialize()
        
        if (!initialized || interpreter == null) {
            Timber.w("🔍 Retriever not available for question: $question")
            return null
        }
        
        if (embeddingsCache.isEmpty()) {
            Timber.w("🔍 No cached embeddings available")
            return null
        }
        
        return try {
            // Generate embedding for the question
            val queryEmbedding = generateEmbedding(question)
            
            if (queryEmbedding == null) {
                Timber.w("🔍 Failed to generate embedding for question: $question")
                return null
            }
            
            // Find best match using cosine similarity
            var bestMatch: QAEntry? = null
            var bestSimilarity = 0f
            var bestMatchedQuestion = ""
            
            for (entry in qaEntries) {
                val cachedEmbedding = embeddingsCache[entry.question] ?: continue
                
                val similarity = cosineSimilarity(queryEmbedding, cachedEmbedding)
                
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity
                    bestMatch = entry
                    bestMatchedQuestion = entry.question
                }
            }
            
            // Return only if similarity exceeds threshold
            if (bestSimilarity >= SIMILARITY_THRESHOLD && bestMatch != null) {
                Timber.i("🔍 Found match with similarity $bestSimilarity: ${bestMatch.question}")
                RetrievalResult(
                    answer = bestMatch.answer,
                    confidence = bestSimilarity,
                    matchedQuestion = bestMatchedQuestion
                )
            } else {
                Timber.d("🔍 No match above threshold ($bestSimilarity < $SIMILARITY_THRESHOLD)")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "🔍 Error retrieving answer for question: $question")
            null
        }
    }
    
    /**
     * Check if the retriever is ready to use
     * 
     * @return true if retriever is initialized and ready
     */
    fun isReady(): Boolean {
        return isInitialized && interpreter != null && embeddingsCache.isNotEmpty()
    }
    
    /**
     * Get number of cached embeddings
     * 
     * @return Count of cached embeddings
     */
    fun getCachedEmbeddingsCount(): Int {
        return embeddingsCache.size
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            embeddingsCache.clear()
            qaEntries = emptyList()
            isInitialized = false
            Timber.i("🔍 EmbeddingsRetriever closed")
        } catch (e: Exception) {
            Timber.e(e, "🔍 Error closing EmbeddingsRetriever")
        }
    }
}
