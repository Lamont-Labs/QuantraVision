package com.lamontlabs.quantravision.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.achievements.AchievementManager
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.quota.HighlightQuota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class HomeViewModel(private val context: Context) : ViewModel() {
    
    data class UiState(
        val userName: String = "Trader",
        val currentTier: SubscriptionTier = SubscriptionTier.FREE,
        val recentDetections: List<PatternMatch> = emptyList(),
        val todayHighlightCount: Int = 0,
        val highlightQuotaRemaining: Int = 0,
        val achievements: List<Achievement> = emptyList(),
        val showWelcomeMessage: Boolean = true,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val database = PatternDatabase.getInstance(context)
    private val achievementManager = AchievementManager.getInstance(context)
    
    init {
        loadHomeData()
        observeTierChanges()
    }
    
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val quotaState = HighlightQuota.state(context)
                val quotaRemaining = HighlightQuota.remaining(context)
                
                val todayMs = System.currentTimeMillis()
                val oneDayMs = TimeUnit.DAYS.toMillis(1)
                val since = todayMs - oneDayMs
                
                val recentDetections = database.patternDao().getRecent(since)
                
                val allAchievements = achievementManager.getAllAchievements()
                val recentAchievements = allAchievements
                    .filter { it.isUnlocked }
                    .sortedByDescending { it.unlockedAt ?: 0L }
                    .take(5)
                
                val currentTier = EntitlementManager.currentTier.value
                
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val showWelcome = prefs.getBoolean("show_welcome_message", true)
                
                _uiState.update {
                    it.copy(
                        currentTier = currentTier,
                        recentDetections = recentDetections.take(10),
                        todayHighlightCount = quotaState.count,
                        highlightQuotaRemaining = quotaRemaining,
                        achievements = recentAchievements,
                        showWelcomeMessage = showWelcome,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load dashboard data"
                    )
                }
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
    
    fun refresh() {
        loadHomeData()
    }
    
    fun refreshData() {
        loadHomeData()
    }
    
    fun dismissWelcome() {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("show_welcome_message", false).apply()
        _uiState.update { it.copy(showWelcomeMessage = false) }
    }
}
