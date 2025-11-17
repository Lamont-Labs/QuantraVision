package com.lamontlabs.quantravision.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.overlay.OverlayService
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
        val currentTier: SubscriptionTier = SubscriptionTier.FREE,
        val mediaProjectionIntent: Intent? = null
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val mediaProjectionManager by lazy {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    
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
    
    fun requestMediaProjectionPermission() {
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        _uiState.update { it.copy(mediaProjectionIntent = intent) }
    }
    
    fun onMediaProjectionResult(result: ActivityResult) {
        _uiState.update { it.copy(mediaProjectionIntent = null) }
        
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            startOverlayService(result.resultCode, result.data!!)
        }
    }
    
    private fun startOverlayService(resultCode: Int, data: Intent) {
        // Store permission result in companion object (can't pass Intent through Intent extras)
        OverlayService.setMediaProjectionResult(resultCode, data)
        
        val serviceIntent = Intent(context, OverlayService::class.java).apply {
            action = "ACTION_START_WITH_PROJECTION"
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        
        _uiState.update { it.copy(isOverlayActive = true) }
        
        // Minimize app to background so overlay is visible
        (context as? Activity)?.moveTaskToBack(true)
    }
    
    fun stopOverlay() {
        val serviceIntent = Intent(context, OverlayService::class.java)
        context.stopService(serviceIntent)
        _uiState.update { it.copy(isOverlayActive = false) }
    }
    
    fun refreshStats() {
        checkOverlayPermission()
        loadScanStats()
    }
}
