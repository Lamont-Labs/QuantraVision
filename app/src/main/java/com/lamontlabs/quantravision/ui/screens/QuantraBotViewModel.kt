package com.lamontlabs.quantravision.ui.screens

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.ai.quantrabot.QuantraBotEngine
import com.lamontlabs.quantravision.intelligence.llm.ModelImportController
import com.lamontlabs.quantravision.intelligence.llm.ModelManager
import com.lamontlabs.quantravision.intelligence.llm.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for QuantraBot chat screen.
 * 
 * Manages:
 * - Chat message history
 * - User input
 * - QuantraBotEngine integration
 * - Recent scan context
 */
class QuantraBotViewModel(application: Application) : AndroidViewModel(application) {
    
    private val modelManager = ModelManager(application.applicationContext)
    
    val modelImportController = ModelImportController(application.applicationContext)
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _hasModel = MutableStateFlow(false)
    val hasModel: StateFlow<Boolean> = _hasModel.asStateFlow()
    
    private var quantraBotEngine: QuantraBotEngine? = null
    private var recentPatterns: List<PatternMatch> = emptyList()
    
    /**
     * Initialize QuantraBot engine.
     * CRITICAL: Uses applicationContext to avoid memory leaks (ViewModels outlive Activities)
     */
    fun initialize(context: Context) {
        if (_isReady.value) return // Already initialized
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.i("🤖 Initializing QuantraBotViewModel...")
                
                // CRITICAL: Use applicationContext to avoid Activity memory leak
                // ViewModels outlive Activities, so we must never hold Activity context
                val engine = QuantraBotEngine(context.applicationContext)
                quantraBotEngine = engine
                
                // Initialize
                val result = engine.initialize()
                
                if (result.isSuccess) {
                    _hasModel.value = engine.hasAIModel()
                    _isReady.value = true
                    Timber.i("✅ QuantraBotViewModel ready (Model: ${_hasModel.value})")
                    
                    // Load recent patterns for context
                    loadRecentPatterns(context)
                } else {
                    Timber.w("⚠️ QuantraBot initialization had issues: ${result.exceptionOrNull()?.message}")
                    _isReady.value = true // Still set ready to allow fallback mode
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize QuantraBotViewModel")
                _isReady.value = true // Allow fallback mode
            }
        }
        
        viewModelScope.launch {
            modelManager.modelStateFlow.collect { state ->
                when (state) {
                    is ModelState.Ready, is ModelState.Downloaded -> {
                        refreshModelState(getApplication())
                    }
                    else -> {}
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        modelImportController.dispose()
    }
    
    /**
     * Update input text.
     */
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    /**
     * Send user message and get bot response.
     */
    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank() || _isLoading.value) return
        
        // Add user message
        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMessage
        
        // Clear input
        _inputText.value = ""
        
        // Show loading
        _isLoading.value = true
        
        // Get bot response
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val engine = quantraBotEngine
                if (engine == null) {
                    addBotMessage("QuantraBot is not initialized yet. Please try again in a moment.")
                    return@launch
                }
                
                val response = engine.answerQuestion(text, recentPatterns)
                addBotMessage(response)
                
            } catch (e: Exception) {
                Timber.e(e, "Error getting bot response")
                addBotMessage("Sorry, I encountered an error processing your question. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Explain the last detected pattern.
     */
    fun explainLastPattern(context: Context) {
        if (_isLoading.value) return
        
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get last pattern from database (use applicationContext)
                val db = PatternDatabase.getInstance(context.applicationContext)
                val lastPattern = db.patternDao().getAll()
                    .maxByOrNull { it.timestamp }
                
                if (lastPattern == null) {
                    addBotMessage("You haven't scanned any patterns yet. Tap the Q logo to scan a chart!")
                    return@launch
                }
                
                // Get indicator context if available
                val indicatorContext = lastPattern.indicatorsJson?.let {
                    // Would parse JSON to IndicatorContext here
                    // For now, pass null
                    null
                }
                
                val engine = quantraBotEngine
                if (engine == null) {
                    addBotMessage("QuantraBot is not initialized yet.")
                    return@launch
                }
                
                // Add user message (simulated)
                viewModelScope.launch(Dispatchers.Main) {
                    val userMessage = ChatMessage(
                        text = "Explain my last pattern detection",
                        isUser = true
                    )
                    _messages.value = _messages.value + userMessage
                }
                
                // Get explanation
                val explanation = engine.explainPattern(lastPattern, indicatorContext)
                addBotMessage(explanation)
                
            } catch (e: Exception) {
                Timber.e(e, "Error explaining last pattern")
                addBotMessage("Sorry, I couldn't explain the last pattern. Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Explain a specific QuantraScore.
     */
    fun explainQuantraScore(pattern: PatternMatch) {
        if (_isLoading.value) return
        
        _isLoading.value = true
        
        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Add user message (simulated)
                viewModelScope.launch(Dispatchers.Main) {
                    val userMessage = ChatMessage(
                        text = "Why did ${pattern.patternName} score ${pattern.quantraScore}/100?",
                        isUser = true
                    )
                    _messages.value = _messages.value + userMessage
                }
                
                val engine = quantraBotEngine
                if (engine == null) {
                    addBotMessage("QuantraBot is not initialized yet.")
                    return@launch
                }
                
                // Get indicator context if available
                val indicatorContext = pattern.indicatorsJson?.let {
                    // Would parse JSON to IndicatorContext here
                    null
                }
                
                val explanation = engine.explainQuantraScore(pattern, indicatorContext)
                addBotMessage(explanation)
                
            } catch (e: Exception) {
                Timber.e(e, "Error explaining QuantraScore")
                addBotMessage("Sorry, I couldn't explain the score. Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear chat history.
     */
    fun clearChat() {
        _messages.value = emptyList()
    }
    
    // ============================================================================
    // PRIVATE HELPERS
    // ============================================================================
    
    /**
     * Add bot message to chat.
     */
    private fun addBotMessage(text: String) {
        viewModelScope.launch(Dispatchers.Main) {
            val botMessage = ChatMessage(text = text, isUser = false)
            _messages.value = _messages.value + botMessage
        }
    }
    
    /**
     * Load recent patterns for context.
     */
    private fun loadRecentPatterns(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = PatternDatabase.getInstance(context.applicationContext)
                val allPatterns = db.patternDao().getAll()
                
                // Get last 10 patterns
                recentPatterns = allPatterns
                    .sortedByDescending { it.timestamp }
                    .take(10)
                
                Timber.d("Loaded ${recentPatterns.size} recent patterns for context")
                
            } catch (e: Exception) {
                Timber.w(e, "Failed to load recent patterns")
                recentPatterns = emptyList()
            }
        }
    }
    
    fun refreshModelState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.i("🤖 Refreshing model state after import...")
                
                // Re-initialize the singleton GemmaEngine first (forces it to reload the model)
                val gemmaEngine = com.lamontlabs.quantravision.intelligence.llm.GemmaEngine.getInstance(context.applicationContext)
                val gemmaResult = gemmaEngine.initialize()
                
                Timber.i("🤖 GemmaEngine re-initialization result: ${gemmaResult.isSuccess}, isReady: ${gemmaEngine.isReady()}")
                
                // Now create new QuantraBot engine (will use the updated singleton)
                val engine = QuantraBotEngine(context.applicationContext)
                quantraBotEngine = engine
                
                val result = engine.initialize()
                
                if (result.isSuccess) {
                    _hasModel.value = engine.hasAIModel()
                    _isReady.value = true
                    Timber.i("✅ Model state refreshed - Model available: ${_hasModel.value}")
                } else {
                    Timber.w("⚠️ Model refresh completed but model not available")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to refresh model state")
            }
        }
    }
}
