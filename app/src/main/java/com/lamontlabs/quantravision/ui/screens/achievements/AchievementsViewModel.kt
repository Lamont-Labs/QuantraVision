package com.lamontlabs.quantravision.ui.screens.achievements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.achievements.AchievementManager
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.achievements.model.AchievementCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AchievementsUiState(
    val achievements: List<Achievement> = emptyList(),
    val selectedCategory: AchievementCategory? = null,
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

class AchievementsViewModel(context: Context) : ViewModel() {
    
    private val achievementManager = AchievementManager.getInstance(context)
    
    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()
    
    init {
        loadAchievements()
    }
    
    fun loadAchievements(category: AchievementCategory? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val allAchievements = if (category != null) {
                achievementManager.getAchievementsByCategory(category)
            } else {
                achievementManager.getAllAchievements()
            }
            
            val unlockedCount = achievementManager.getUnlockedCount()
            
            _uiState.value = _uiState.value.copy(
                achievements = allAchievements,
                selectedCategory = category,
                unlockedCount = unlockedCount,
                totalCount = allAchievements.size,
                isLoading = false
            )
        }
    }
    
    fun filterByCategory(category: AchievementCategory?) {
        loadAchievements(category)
    }
    
    fun sortByUnlockDate() {
        val sorted = _uiState.value.achievements.sortedWith(
            compareByDescending<Achievement> { it.isUnlocked }
                .thenByDescending { it.unlockedAt ?: 0L }
        )
        _uiState.value = _uiState.value.copy(achievements = sorted)
    }
    
    fun sortAlphabetically() {
        val sorted = _uiState.value.achievements.sortedBy { it.title }
        _uiState.value = _uiState.value.copy(achievements = sorted)
    }
}
