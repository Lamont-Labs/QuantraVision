package com.lamontlabs.quantravision.ui.screens.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.export.CsvReportGenerator
import com.lamontlabs.quantravision.export.PDFReportGenerator
import com.lamontlabs.quantravision.export.model.PatternReport
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * ViewModel for export functionality.
 * Manages report generation and sharing.
 */
class ExportViewModel(private val context: Context) : ViewModel() {
    
    private val database: PatternDatabase = PatternDatabase.getInstance(context)
    
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    
    init {
        checkProAccess()
        loadPatternCount()
    }
    
    fun onFormatChanged(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(selectedFormat = format)
    }
    
    fun onDateRangeChanged(range: DateRange) {
        _uiState.value = _uiState.value.copy(selectedDateRange = range)
        loadPatternCount()
    }
    
    fun onPatternTypeFilterChanged(types: Set<String>) {
        _uiState.value = _uiState.value.copy(selectedPatternTypes = types)
        loadPatternCount()
    }
    
    fun onMinConfidenceChanged(confidence: Float) {
        _uiState.value = _uiState.value.copy(minConfidence = confidence)
        loadPatternCount()
    }
    
    fun generateReport() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isGenerating = true,
                    error = null,
                    progress = 0f
                )
                
                // Check Pro access
                if (!ProFeatureGate.isActive(context)) {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = "Export is a Pro feature. Please upgrade to continue."
                    )
                    return@launch
                }
                
                // Load patterns based on filters
                _uiState.value = _uiState.value.copy(progress = 0.2f)
                val patterns = loadFilteredPatterns()
                
                if (patterns.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = "No patterns found matching the selected criteria."
                    )
                    return@launch
                }
                
                // Create report
                _uiState.value = _uiState.value.copy(progress = 0.4f)
                val dateRange = getDateRangeMillis(_uiState.value.selectedDateRange)
                val filterCriteria = PatternReport.FilterCriteria(
                    patternTypes = _uiState.value.selectedPatternTypes.takeIf { it.isNotEmpty() }?.toList(),
                    minConfidence = _uiState.value.minConfidence.toDouble(),
                    timeframes = null
                )
                
                val report = PatternReport.create(
                    patterns = patterns,
                    dateRange = dateRange,
                    filterCriteria = filterCriteria
                )
                
                // Generate file
                _uiState.value = _uiState.value.copy(progress = 0.6f)
                val file = withContext(Dispatchers.IO) {
                    when (_uiState.value.selectedFormat) {
                        ExportFormat.PDF -> PDFReportGenerator.generate(context, report.patterns)
                        ExportFormat.CSV -> CsvReportGenerator.generate(context, report)
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    progress = 1f,
                    generatedFile = file
                )
                
                Timber.i("Report generated successfully: ${file.absolutePath}")
                
            } catch (e: Exception) {
                Timber.e(e, "Report generation failed")
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "Failed to generate report: ${e.message}"
                )
            }
        }
    }
    
    fun clearGeneratedFile() {
        _uiState.value = _uiState.value.copy(generatedFile = null, progress = 0f)
    }
    
    private fun checkProAccess() {
        _uiState.value = _uiState.value.copy(
            hasProAccess = ProFeatureGate.isActive(context)
        )
    }
    
    private fun loadPatternCount() {
        viewModelScope.launch {
            try {
                val count = withContext(Dispatchers.IO) {
                    val patterns = loadFilteredPatterns()
                    patterns.size
                }
                _uiState.value = _uiState.value.copy(patternCount = count)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load pattern count")
            }
        }
    }
    
    private suspend fun loadFilteredPatterns() = withContext(Dispatchers.IO) {
        val dao = database.patternDao()
        val dateRange = getDateRangeMillis(_uiState.value.selectedDateRange)
        
        // Load patterns within date range
        val patterns = if (dateRange != null) {
            dao.getAll().filter { it.timestamp >= dateRange.startDate && it.timestamp <= dateRange.endDate }
        } else {
            dao.getAll()
        }
        
        // Apply filters
        patterns.filter { pattern ->
            // Pattern type filter
            val typeMatch = _uiState.value.selectedPatternTypes.isEmpty() ||
                    _uiState.value.selectedPatternTypes.contains(pattern.patternName)
            
            // Confidence filter
            val confidenceMatch = pattern.confidence >= _uiState.value.minConfidence
            
            typeMatch && confidenceMatch
        }
    }
    
    private fun getDateRangeMillis(range: DateRange): PatternReport.DateRange? {
        val now = System.currentTimeMillis()
        return when (range) {
            DateRange.LAST_7_DAYS -> {
                PatternReport.DateRange(
                    startDate = now - (7 * 24 * 60 * 60 * 1000L),
                    endDate = now
                )
            }
            DateRange.LAST_30_DAYS -> {
                PatternReport.DateRange(
                    startDate = now - (30 * 24 * 60 * 60 * 1000L),
                    endDate = now
                )
            }
            DateRange.ALL_TIME -> null
        }
    }
}

data class ExportUiState(
    val selectedFormat: ExportFormat = ExportFormat.PDF,
    val selectedDateRange: DateRange = DateRange.LAST_30_DAYS,
    val selectedPatternTypes: Set<String> = emptySet(),
    val minConfidence: Float = 0.5f,
    val patternCount: Int = 0,
    val isGenerating: Boolean = false,
    val progress: Float = 0f,
    val generatedFile: File? = null,
    val error: String? = null,
    val hasProAccess: Boolean = false
)

enum class ExportFormat {
    PDF, CSV
}

enum class DateRange {
    LAST_7_DAYS, LAST_30_DAYS, ALL_TIME
}
