package com.lamontlabs.quantravision.ui.viewmodels

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(private val context: Context) : ViewModel() {
    
    data class UiState(
        val currentTier: SubscriptionTier = SubscriptionTier.FREE,
        val notificationsEnabled: Boolean = true,
        val hapticsEnabled: Boolean = true,
        val voiceAlertsEnabled: Boolean = false,
        val appVersion: String = "",
        val hasOverlayPermission: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
        observeTierChanges()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                
                val notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
                val hapticsEnabled = prefs.getBoolean("haptic_alerts_enabled", true)
                val voiceAlertsEnabled = prefs.getBoolean("voice_alerts_enabled", false)
                
                val appVersion = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (e: Exception) {
                    "Unknown"
                }
                
                val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else {
                    true
                }
                
                val currentTier = EntitlementManager.currentTier.value
                
                _uiState.update {
                    it.copy(
                        currentTier = currentTier,
                        notificationsEnabled = notificationsEnabled,
                        hapticsEnabled = hapticsEnabled,
                        voiceAlertsEnabled = voiceAlertsEnabled,
                        appVersion = appVersion,
                        hasOverlayPermission = hasOverlayPermission
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
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }
    
    fun toggleHaptics(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("haptic_alerts_enabled", enabled).apply()
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }
    
    fun toggleVoiceAlerts(enabled: Boolean) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("voice_alerts_enabled", enabled).apply()
        _uiState.update { it.copy(voiceAlertsEnabled = enabled) }
    }
}
