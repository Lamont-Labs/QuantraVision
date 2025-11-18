package com.lamontlabs.quantravision.devbot.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.devbot.ai.DevBotEngine
import com.lamontlabs.quantravision.devbot.ai.DiagnosticChatMessage
import com.lamontlabs.quantravision.devbot.engine.DiagnosticEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DevBotViewModel(application: Application) : AndroidViewModel(application) {
    private val devBotEngine = DevBotEngine(application.applicationContext)
    
    val messages: StateFlow<List<DiagnosticChatMessage>> = devBotEngine.messages
    val isProcessing: StateFlow<Boolean> = devBotEngine.isProcessing
    val hasModel: StateFlow<Boolean> = devBotEngine.hasModel
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _errorStats = MutableStateFlow(ErrorStats())
    val errorStats: StateFlow<ErrorStats> = _errorStats.asStateFlow()
    
    init {
        viewModelScope.launch {
            devBotEngine.initialize()
            _isReady.value = true
        }
        
        viewModelScope.launch {
            DiagnosticEngine.events.collect {
                updateErrorStats()
            }
        }
    }
    
    fun sendMessage() {
        val message = _inputText.value.trim()
        if (message.isBlank()) return
        
        viewModelScope.launch {
            _inputText.value = ""
            devBotEngine.sendMessage(message)
        }
    }
    
    fun updateInputText(text: String) {
        _inputText.value = text
    }
    
    fun clearChat() {
        devBotEngine.clearChat()
    }
    
    fun clearErrorHistory() {
        DiagnosticEngine.clearHistory()
        updateErrorStats()
    }
    
    private fun updateErrorStats() {
        val recentErrors = DiagnosticEngine.getRecentErrors(100)
        
        _errorStats.value = ErrorStats(
            totalErrors = recentErrors.size,
            crashes = DiagnosticEngine.getErrorsByType("Crash").size,
            performanceIssues = DiagnosticEngine.getErrorsByType("Performance").size,
            networkErrors = DiagnosticEngine.getErrorsByType("Network").size,
            databaseIssues = DiagnosticEngine.getErrorsByType("Database").size
        )
    }
    
    fun getSuggestedQuestions(): List<String> {
        val stats = _errorStats.value
        val questions = mutableListOf<String>()
        
        if (stats.crashes > 0) {
            questions.add("Why did the app crash?")
        }
        if (stats.performanceIssues > 0) {
            questions.add("What are the performance issues?")
        }
        if (stats.networkErrors > 0) {
            questions.add("Why are network requests failing?")
        }
        if (stats.databaseIssues > 0) {
            questions.add("What database problems exist?")
        }
        
        if (questions.isEmpty()) {
            questions.addAll(
                listOf(
                    "Show me recent errors",
                    "Check memory usage",
                    "How is app performance?",
                    "Explain DevBot features"
                )
            )
        }
        
        return questions.take(4)
    }
}

data class ErrorStats(
    val totalErrors: Int = 0,
    val crashes: Int = 0,
    val performanceIssues: Int = 0,
    val networkErrors: Int = 0,
    val databaseIssues: Int = 0
)
