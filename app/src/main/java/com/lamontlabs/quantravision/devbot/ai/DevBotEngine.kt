package com.lamontlabs.quantravision.devbot.ai

import android.content.Context
import android.util.Log
import com.lamontlabs.quantravision.ai.ensemble.EnsembleEngine
import com.lamontlabs.quantravision.intelligence.llm.ExplanationResult
import com.lamontlabs.quantravision.devbot.data.DiagnosticEvent
import com.lamontlabs.quantravision.devbot.engine.DiagnosticEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class DiagnosticChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedEvents: List<DiagnosticEvent> = emptyList()
)

class DevBotEngine(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var ensembleEngine: EnsembleEngine? = null
    private val knowledgeLoader = DiagnosticKnowledgeLoader(context)
    private val promptBuilder = DiagnosticPromptBuilder()
    
    private val _messages = MutableStateFlow<List<DiagnosticChatMessage>>(emptyList())
    val messages: StateFlow<List<DiagnosticChatMessage>> = _messages.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _hasModel = MutableStateFlow(false)
    val hasModel: StateFlow<Boolean> = _hasModel.asStateFlow()
    
    private var isInitialized = false
    
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext Result.success(Unit)
        
        try {
            knowledgeLoader.loadKnowledge()
            
            ensembleEngine = EnsembleEngine.getInstance(context)
            val initResult = ensembleEngine!!.initialize()
            
            _hasModel.value = initResult.isSuccess
            isInitialized = true
            
            Log.d("DevBotEngine", "Initialized - Model available: ${_hasModel.value}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DevBotEngine", "Initialization failed", e)
            isInitialized = true
            _hasModel.value = false
            Result.success(Unit)
        }
    }
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext Result.failure(Exception("DevBot not initialized"))
        }
        
        _isProcessing.value = true
        
        val userChatMessage = DiagnosticChatMessage(
            text = userMessage,
            isUser = true
        )
        _messages.value += userChatMessage
        
        try {
            val response = if (_hasModel.value && ensembleEngine != null) {
                generateAIResponse(userMessage)
            } else {
                generateFallbackResponse(userMessage)
            }
            
            val botChatMessage = DiagnosticChatMessage(
                text = response,
                isUser = false
            )
            _messages.value += botChatMessage
            
            _isProcessing.value = false
            Result.success(response)
            
        } catch (e: Exception) {
            _isProcessing.value = false
            Log.e("DevBotEngine", "Error generating response", e)
            
            val errorMessage = DiagnosticChatMessage(
                text = "Sorry, I encountered an error processing your request.",
                isUser = false
            )
            _messages.value += errorMessage
            
            Result.failure(e)
        }
    }
    
    private suspend fun generateAIResponse(userMessage: String): String {
        val recentErrors = DiagnosticEngine.getRecentErrors(20)
        
        val prompt = promptBuilder.buildDiagnosticPrompt(
            userQuery = userMessage,
            recentEvents = recentErrors,
            errorKnowledge = knowledgeLoader.getRelevantKnowledge(userMessage, recentErrors)
        )
        
        return ensembleEngine?.generate(prompt)?.let { result ->
            when (result) {
                is ExplanationResult.Success -> result.text
                is ExplanationResult.Failure -> result.fallbackText ?: generateFallbackResponse(userMessage)
                is ExplanationResult.Unavailable -> result.fallbackText
            }
        } ?: generateFallbackResponse(userMessage)
    }
    
    private fun generateFallbackResponse(userMessage: String): String {
        val lowerQuery = userMessage.lowercase()
        
        return when {
            lowerQuery.contains("crash") -> {
                val recentCrashes = DiagnosticEngine.getErrorsByType("Crash")
                if (recentCrashes.isEmpty()) {
                    "No crashes detected recently. Your app is running smoothly!"
                } else {
                    val lastCrash = recentCrashes.last()
                    """
                        |I detected ${recentCrashes.size} crash(es). Most recent:
                        |
                        |${lastCrash.message}
                        |
                        |💡 This typically happens when the app tries to access something that doesn't exist. Check for null values or invalid array indices.
                    """.trimMargin()
                }
            }
            
            lowerQuery.contains("memory") || lowerQuery.contains("leak") -> {
                val memoryIssues = DiagnosticEngine.getErrorsByType("Performance")
                    .filterIsInstance<DiagnosticEvent.Performance>()
                    .filter { it.metricType.name.contains("MEMORY") }
                
                if (memoryIssues.isEmpty()) {
                    "Memory usage looks healthy. No memory leaks or pressure detected."
                } else {
                    """
                        |I found ${memoryIssues.size} memory-related issue(s).
                        |
                        |Common causes:
                        |• Large bitmaps not being recycled
                        |• Static references to Activity/Context
                        |• Listeners not being removed
                        |• Collections growing unbounded
                        |
                        |💡 Use Android Profiler to identify specific leaks.
                    """.trimMargin()
                }
            }
            
            lowerQuery.contains("slow") || lowerQuery.contains("performance") -> {
                """
                    |Performance optimization tips:
                    |
                    |• Move heavy work off the main thread
                    |• Use coroutines for async operations
                    |• Cache expensive computations
                    |• Optimize RecyclerView with ViewHolders
                    |• Reduce overdraw in layouts
                    |
                    |Ask me about specific performance metrics for detailed analysis.
                """.trimMargin()
            }
            
            lowerQuery.contains("error") || lowerQuery.contains("issue") -> {
                val recentErrors = DiagnosticEngine.getRecentErrors(5)
                if (recentErrors.isEmpty()) {
                    "No errors detected recently. Everything looks good!"
                } else {
                    """
                        |Recent errors (${recentErrors.size}):
                        |
                        |${recentErrors.take(3).joinToString("\n") { "• ${it.message}" }}
                        |
                        |Ask me about a specific error for detailed help.
                    """.trimMargin()
                }
            }
            
            lowerQuery.contains("help") || lowerQuery.contains("what can you") -> {
                """
                    |I'm DevBot, your diagnostic assistant! I can help with:
                    |
                    |🔍 Crash Analysis - "Why did the app crash?"
                    |⚡ Performance - "Why is the scan slow?"
                    |💾 Memory - "Do I have memory leaks?"
                    |🌐 Network - "Why is the API failing?"
                    |🗄️ Database - "Why are queries slow?"
                    |
                    |I monitor your app in real-time and explain errors in plain English!
                """.trimMargin()
            }
            
            else -> {
                """
                    |I'm analyzing your app in real-time. Try asking:
                    |
                    |• "Show me recent errors"
                    |• "Why did the app crash?"
                    |• "Check memory usage"
                    |• "Are there performance issues?"
                    |
                    |I'm in fallback mode (AI model not loaded). Answers are template-based but still helpful!
                """.trimMargin()
            }
        }
    }
    
    fun clearChat() {
        _messages.value = emptyList()
    }
    
    fun getRecentErrorSummary(): String {
        val errors = DiagnosticEngine.getRecentErrors(50)
        if (errors.isEmpty()) return "No errors detected"
        
        val crashes = errors.filterIsInstance<DiagnosticEvent.Crash>().size
        val perfIssues = errors.filterIsInstance<DiagnosticEvent.Performance>().size
        val networkErrors = errors.filterIsInstance<DiagnosticEvent.Network>().size
        val dbIssues = errors.filterIsInstance<DiagnosticEvent.Database>().size
        
        return """
            |📊 Error Summary (last 50 events):
            |• Crashes: $crashes
            |• Performance: $perfIssues
            |• Network: $networkErrors
            |• Database: $dbIssues
        """.trimMargin()
    }
}
