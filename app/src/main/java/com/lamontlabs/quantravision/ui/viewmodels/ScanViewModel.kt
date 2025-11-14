package com.lamontlabs.quantravision.ui.viewmodels

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.quota.HighlightQuota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanViewModel(private val context: Context) : ViewModel() {
    
    data class UiState(
        val isOverlayActive: Boolean = false,
        val hasOverlayPermission: Boolean = false,
        val detectionCount: Int = 0,
        val scanLearningProgress: Int = 0,
        val highlightsUsedToday: Int = 0,
        val highlightsRemaining: Int = 0,
        val currentTier: SubscriptionTier = SubscriptionTier.FREE
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val database = PatternDatabase.getInstance(context)
    
    init {
        checkOverlayPermission()
        loadScanStats()
        observeTierChanges()
    }
    
    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val hasPermission = Settings.canDrawOverlays(context)
            _uiState.update { it.copy(hasOverlayPermission = hasPermission) }
        } else {
            _uiState.update { it.copy(hasOverlayPermission = true) }
        }
    }
    
    private fun loadScanStats() {
        viewModelScope.launch {
            try {
                val allDetections = database.patternDao().getAll()
                val detectionCount = allDetections.size
                
                val quotaState = HighlightQuota.state(context)
                val quotaRemaining = HighlightQuota.remaining(context)
                
                val hasScanLearning = EntitlementManager.hasFeatureAccess(Feature.SCAN_LEARNING)
                val scanLearningProgress = if (hasScanLearning) {
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.getInt("scan_learning_progress", 0)
                } else {
                    0
                }
                
                val currentTier = EntitlementManager.currentTier.value
                
                _uiState.update {
                    it.copy(
                        detectionCount = detectionCount,
                        scanLearningProgress = scanLearningProgress,
                        highlightsUsedToday = quotaState.count,
                        highlightsRemaining = quotaRemaining,
                        currentTier = currentTier
                    )
                }
            } catch (e: Exception) {
            }
        }
    }
    
    private fun observeTierChanges() {
        viewModelScope.launch {
            EntitlementManager.currentTier.collect { tier ->
                _uiState.update { it.copy(currentTier = tier) }
                loadScanStats()
            }
        }
    }
    
    fun startOverlay() {
        _uiState.update { it.copy(isOverlayActive = true) }
    }
    
    fun stopOverlay() {
        _uiState.update { it.copy(isOverlayActive = false) }
    }
    
    fun refreshStats() {
        checkOverlayPermission()
        loadScanStats()
    }
}
