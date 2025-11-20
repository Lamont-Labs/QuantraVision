package com.lamontlabs.quantravision.devbot.ui

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.BuildConfig
import com.lamontlabs.quantravision.devbot.ai.DevBotEngine
import com.lamontlabs.quantravision.devbot.ai.DiagnosticChatMessage
import com.lamontlabs.quantravision.devbot.diagnostics.ComponentHealth
import com.lamontlabs.quantravision.devbot.diagnostics.ComponentHealthMonitor
import com.lamontlabs.quantravision.devbot.diagnostics.ModelDiagnostics
import com.lamontlabs.quantravision.devbot.diagnostics.StartupDiagnosticCollector
import com.lamontlabs.quantravision.devbot.engine.DiagnosticEngine
import com.lamontlabs.quantravision.intelligence.llm.ModelImportController
import com.lamontlabs.quantravision.intelligence.llm.ModelManager
import com.lamontlabs.quantravision.intelligence.llm.ModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DevBotViewModel(application: Application) : AndroidViewModel(application) {
    private val devBotEngine = DevBotEngine(application.applicationContext)
    private val modelManager = ModelManager(application.applicationContext)
    
    val modelImportController = ModelImportController(application.applicationContext)
    
    val messages: StateFlow<List<DiagnosticChatMessage>> = devBotEngine.messages
    val isProcessing: StateFlow<Boolean> = devBotEngine.isProcessing
    val hasModel: StateFlow<Boolean> = devBotEngine.hasModel
    
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _errorStats = MutableStateFlow(ErrorStats())
    val errorStats: StateFlow<ErrorStats> = _errorStats.asStateFlow()
    
    // Build Info
    val buildFingerprint: String = BuildConfig.BUILD_FINGERPRINT
    val buildTimestamp: String = BuildConfig.BUILD_TIMESTAMP
    val gitHash: String = BuildConfig.GIT_HASH
    val buildId: String = BuildConfig.BUILD_ID
    
    // Component Health Flow
    private val _componentHealth = MutableStateFlow<Map<String, ComponentHealth>>(emptyMap())
    val componentHealth: StateFlow<Map<String, ComponentHealth>> = _componentHealth.asStateFlow()
    
    // Startup Timeline Flow
    private val _startupTimeline = MutableStateFlow<com.lamontlabs.quantravision.devbot.diagnostics.StartupTimeline?>(null)
    val startupTimeline: StateFlow<com.lamontlabs.quantravision.devbot.diagnostics.StartupTimeline?> = _startupTimeline.asStateFlow()
    
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
        
        viewModelScope.launch {
            modelManager.modelStateFlow.collect { state ->
                when (state) {
                    is ModelState.Ready, is ModelState.Downloaded -> {
                        refreshModelState()
                    }
                    else -> {}
                }
            }
        }
        
        // Monitor component health updates
        viewModelScope.launch {
            ComponentHealthMonitor.healthStates.collect { health ->
                _componentHealth.value = health
            }
        }
        
        // Monitor startup timeline updates
        viewModelScope.launch {
            StartupDiagnosticCollector.timeline.collect { timeline ->
                _startupTimeline.value = timeline
            }
        }
    }
    
    // Suspend function to get model diagnostics
    suspend fun getModelDiagnostics() = withContext(Dispatchers.IO) {
        ModelDiagnostics.diagnose(getApplication())
    }
    
    override fun onCleared() {
        super.onCleared()
        modelImportController.dispose()
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
    
    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus.asStateFlow()
    
    fun requestExport() {
        _exportStatus.value = ExportStatus.ConfirmationRequired
    }
    
    fun exportDiagnostics() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    _exportStatus.value = ExportStatus.Exporting
                }
                
                val result = withContext(Dispatchers.IO) {
                    cleanupOldExports()
                    
                    val jsonData = DiagnosticEngine.exportDiagnostics(getApplication())
                    
                    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                    val filename = "devbot_diagnostics_${dateFormat.format(Date())}.json"
                    
                    val exportDir = File(getApplication<Application>().cacheDir, "exports")
                    exportDir.mkdirs()
                    
                    val file = File(exportDir, filename)
                    file.writeText(jsonData)
                    
                    val uri = FileProvider.getUriForFile(
                        getApplication(),
                        "${getApplication<Application>().packageName}.fileprovider",
                        file
                    )
                    
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "QuantraVision DevBot Diagnostics")
                        putExtra(Intent.EXTRA_TEXT, buildExportMessage())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addCategory(Intent.CATEGORY_DEFAULT)
                    }
                    
                    val chooserIntent = Intent.createChooser(sendIntent, "Share Diagnostics").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    Pair(chooserIntent, filename)
                }
                
                withContext(Dispatchers.Main) {
                    if (getApplication<Application>().packageManager.queryIntentActivities(
                            result.first,
                            android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                        ).isNotEmpty()
                    ) {
                        _exportStatus.value = ExportStatus.Success(result.first, result.second)
                    } else {
                        _exportStatus.value = ExportStatus.Error("No app available to share diagnostics")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("DevBotViewModel", "Error exporting diagnostics", e)
                withContext(Dispatchers.Main) {
                    _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
                }
            }
        }
    }
    
    private fun buildExportMessage(): String {
        val stats = _errorStats.value
        return """
            QuantraVision DevBot Diagnostic Report
            
            ⚠️ CONTAINS SENSITIVE DEBUG INFORMATION ⚠️
            This file contains stack traces and error details from your app.
            Only share with developers you trust for debugging purposes.
            
            Summary:
            - Total Events: ${stats.totalErrors}
            - Crashes: ${stats.crashes}
            - Performance Issues: ${stats.performanceIssues}
            - Network Errors: ${stats.networkErrors}
            - Database Issues: ${stats.databaseIssues}
        """.trimIndent()
    }
    
    private fun cleanupOldExports() {
        try {
            val exportDir = File(getApplication<Application>().cacheDir, "exports")
            if (!exportDir.exists()) return
            
            val files = exportDir.listFiles() ?: return
            val sortedFiles = files.sortedByDescending { it.lastModified() }
            
            sortedFiles.drop(5).forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            Log.w("DevBotViewModel", "Failed to cleanup old exports", e)
        }
    }
    
    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }
    
    fun refreshModelState() {
        viewModelScope.launch {
            devBotEngine.initialize()
        }
    }
}

data class ErrorStats(
    val totalErrors: Int = 0,
    val crashes: Int = 0,
    val performanceIssues: Int = 0,
    val networkErrors: Int = 0,
    val databaseIssues: Int = 0
)

sealed class ExportStatus {
    data object Idle : ExportStatus()
    data object ConfirmationRequired : ExportStatus()
    data object Exporting : ExportStatus()
    data class Success(val intent: Intent, val filename: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}
