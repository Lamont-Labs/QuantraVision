package com.lamontlabs.quantravision.overlay

import com.lamontlabs.quantravision.detection.DetectionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

sealed class OverlayState {
    object Idle : OverlayState()
    object Capturing : OverlayState()
    data class ShowingResult(val patterns: List<DetectionResult>) : OverlayState()
}

class OverlayStateMachine {
    
    private val _state = MutableStateFlow<OverlayState>(OverlayState.Idle)
    val state: StateFlow<OverlayState> = _state.asStateFlow()
    
    private val mutex = Mutex()
    private var lastTransitionTime = 0L
    private val debounceMs = 300L
    
    suspend fun transitionToCapturing(): Boolean = mutex.withLock {
        val now = System.currentTimeMillis()
        if (now - lastTransitionTime < debounceMs) {
            Timber.d("Transition debounced")
            return false
        }
        
        when (_state.value) {
            is OverlayState.Idle, is OverlayState.ShowingResult -> {
                _state.value = OverlayState.Capturing
                lastTransitionTime = now
                Timber.d("State: Idle/ShowingResult → Capturing")
                true
            }
            is OverlayState.Capturing -> {
                Timber.w("Already capturing, ignoring transition")
                false
            }
        }
    }
    
    suspend fun transitionToShowingResult(patterns: List<DetectionResult>) = mutex.withLock {
        when (_state.value) {
            is OverlayState.Capturing -> {
                _state.value = OverlayState.ShowingResult(patterns)
                lastTransitionTime = System.currentTimeMillis()
                Timber.d("State: Capturing → ShowingResult (${patterns.size} patterns)")
            }
            else -> {
                Timber.w("Cannot transition to ShowingResult from ${_state.value::class.simpleName}")
            }
        }
    }
    
    suspend fun transitionToIdle() = mutex.withLock {
        _state.value = OverlayState.Idle
        lastTransitionTime = System.currentTimeMillis()
        Timber.d("State: ${_state.value::class.simpleName} → Idle")
    }
    
    fun getCurrentState(): OverlayState = _state.value
    
    fun isCapturing(): Boolean = _state.value is OverlayState.Capturing
    
    fun isShowingResults(): Boolean = _state.value is OverlayState.ShowingResult
}
