package com.lamontlabs.quantravision.overlay

import com.lamontlabs.quantravision.detection.DetectionResult
import kotlinx.coroutines.*
import timber.log.Timber

class PatternResultController(
    private val scope: CoroutineScope,
    private val autoClearTimeoutMs: Long = 10_000L
) {
    
    private var autoClearJob: Job? = null
    private var currentResults: List<DetectionResult> = emptyList()
    private var isCleared: Boolean = false
    
    var onResultsCleared: (() -> Unit)? = null
    
    fun showResults(results: List<DetectionResult>) {
        Timber.d("Showing ${results.size} pattern results")
        currentResults = results
        isCleared = false
        
        autoClearJob?.cancel()
        
        if (results.isNotEmpty()) {
            autoClearJob = scope.launch {
                delay(autoClearTimeoutMs)
                Timber.d("Auto-clear timeout expired after ${autoClearTimeoutMs}ms")
                performAutoClear()
            }
        }
    }
    
    private fun performAutoClear() {
        if (isCleared) {
            Timber.d("Auto-clear skipped: results already cleared manually")
            return
        }
        isCleared = true
        Timber.d("Auto-clearing results after timeout")
        currentResults = emptyList()
        onResultsCleared?.invoke()
    }
    
    fun manualClear() {
        if (isCleared) {
            Timber.d("Manual clear skipped: results already cleared")
            return
        }
        isCleared = true
        Timber.d("Manually clearing results")
        autoClearJob?.cancel()
        autoClearJob = null
        currentResults = emptyList()
        onResultsCleared?.invoke()
        isCleared = false
    }
    
    @Deprecated("Use manualClear() instead", ReplaceWith("manualClear()"))
    fun clearResults() {
        manualClear()
    }
    
    fun getCurrentResults(): List<DetectionResult> = currentResults
    
    fun hasResults(): Boolean = currentResults.isNotEmpty()
    
    fun cleanup() {
        autoClearJob?.cancel()
        autoClearJob = null
        currentResults = emptyList()
        isCleared = false
    }
}
