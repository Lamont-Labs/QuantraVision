package com.lamontlabs.quantravision.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.education.LessonRepository
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LearnViewModel(private val context: Context) : ViewModel() {
    
    data class Lesson(
        val id: String,
        val title: String,
        val description: String,
        val completed: Boolean,
        val requiredTier: SubscriptionTier
    )
    
    data class UiState(
        val lessons: List<Lesson> = emptyList(),
        val hasBookAccess: Boolean = false,
        val completedLessonCount: Int = 0,
        val totalLessonCount: Int = 0,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    init {
        checkBookAccess()
        loadLessons()
    }
    
    private fun checkBookAccess() {
        val hasAccess = EntitlementManager.hasFeatureAccess(Feature.TRADING_BOOK)
        _uiState.update { it.copy(hasBookAccess = hasAccess) }
    }
    
    private fun loadLessons() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val allLessons = LessonRepository.getAllLessons()
                
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val completedLessonIds = prefs.getStringSet("completed_lessons", emptySet()) ?: emptySet()
                
                val lessons = allLessons.map { lesson ->
                    Lesson(
                        id = lesson.id.toString(),
                        title = lesson.title,
                        description = lesson.category,
                        completed = completedLessonIds.contains(lesson.id.toString()),
                        requiredTier = when {
                            lesson.id <= 5 -> SubscriptionTier.FREE
                            lesson.id <= 15 -> SubscriptionTier.STARTER
                            lesson.id <= 20 -> SubscriptionTier.STANDARD
                            else -> SubscriptionTier.PRO
                        }
                    )
                }
                
                val completedCount = lessons.count { it.completed }
                val totalCount = lessons.size
                
                _uiState.update {
                    it.copy(
                        lessons = lessons,
                        completedLessonCount = completedCount,
                        totalLessonCount = totalCount,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load lessons"
                    )
                }
            }
        }
    }
    
    fun markLessonComplete(lessonId: String) {
        viewModelScope.launch {
            try {
                val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val completedLessons = prefs.getStringSet("completed_lessons", emptySet())?.toMutableSet() ?: mutableSetOf()
                completedLessons.add(lessonId)
                prefs.edit().putStringSet("completed_lessons", completedLessons).apply()
                
                loadLessons()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to mark lesson complete")
                }
            }
        }
    }
    
    fun refresh() {
        checkBookAccess()
        loadLessons()
    }
    
    fun refreshLessons() {
        checkBookAccess()
        loadLessons()
    }
}
