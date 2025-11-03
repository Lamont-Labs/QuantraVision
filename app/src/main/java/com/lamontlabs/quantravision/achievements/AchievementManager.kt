package com.lamontlabs.quantravision.achievements

import android.content.Context
import android.util.Log
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.achievements.data.AchievementEntity
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.achievements.model.AchievementCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AchievementManager private constructor(private val context: Context) {
    
    private val TAG = "AchievementManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val database = PatternDatabase.getInstance(context)
    private val achievementDao = database.achievementDao()
    
    private val _achievementUnlocked = MutableSharedFlow<Achievement>()
    val achievementUnlocked: SharedFlow<Achievement> = _achievementUnlocked.asSharedFlow()
    
    private var achievementDefinitions: List<Achievement> = emptyList()
    
    init {
        loadAchievementDefinitions()
        scope.launch {
            initializeDatabase()
        }
    }
    
    private fun loadAchievementDefinitions() {
        try {
            val jsonString = context.assets.open("achievements.json")
                .bufferedReader()
                .use { it.readText() }
            
            val jsonObject = JSONObject(jsonString)
            val achievementsArray = jsonObject.getJSONArray("achievements")
            
            achievementDefinitions = (0 until achievementsArray.length()).map { i ->
                val achJson = achievementsArray.getJSONObject(i)
                val map = mapOf(
                    "id" to achJson.getString("id"),
                    "title" to achJson.getString("title"),
                    "description" to achJson.getString("description"),
                    "category" to achJson.getString("category"),
                    "icon" to achJson.getString("icon"),
                    "totalRequired" to achJson.getInt("totalRequired")
                )
                Achievement.fromJson(map)
            }
            
            Log.d(TAG, "Loaded ${achievementDefinitions.size} achievement definitions")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load achievement definitions", e)
            achievementDefinitions = emptyList()
        }
    }
    
    private suspend fun initializeDatabase() {
        withContext(Dispatchers.IO) {
            try {
                val existingIds = achievementDao.getAll().map { it.id }.toSet()
                val newAchievements = achievementDefinitions
                    .filter { !existingIds.contains(it.id) }
                    .map { achievement ->
                        AchievementEntity(
                            id = achievement.id,
                            isUnlocked = false,
                            unlockedAt = null,
                            progress = 0,
                            totalRequired = achievement.totalRequired
                        )
                    }
                
                if (newAchievements.isNotEmpty()) {
                    achievementDao.insertAll(newAchievements)
                    Log.d(TAG, "Initialized ${newAchievements.size} new achievements in database")
                } else {
                    // No new achievements to initialize
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize achievement database", e)
            }
        }
    }
    
    suspend fun getAllAchievements(): List<Achievement> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = achievementDao.getAll()
                val entityMap = entities.associateBy { it.id }
                
                achievementDefinitions.map { def ->
                    val entity = entityMap[def.id]
                    def.copy(
                        isUnlocked = entity?.isUnlocked ?: false,
                        unlockedAt = entity?.unlockedAt,
                        progress = entity?.progress ?: 0
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get all achievements", e)
                emptyList()
            }
        }
    }
    
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        return getAllAchievements().filter { it.category == category }
    }
    
    suspend fun getUnlockedCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                achievementDao.getUnlockedCount()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get unlocked count", e)
                0
            }
        }
    }
    
    suspend fun getProgress(achievementId: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                achievementDao.getById(achievementId)?.progress ?: 0
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get progress for $achievementId", e)
                0
            }
        }
    }
    
    suspend fun incrementProgress(achievementId: String, amount: Int = 1) {
        withContext(Dispatchers.IO) {
            try {
                val entity = achievementDao.getById(achievementId)
                if (entity != null && !entity.isUnlocked) {
                    val newProgress = (entity.progress + amount).coerceAtMost(entity.totalRequired)
                    achievementDao.updateProgress(achievementId, newProgress)
                    
                    if (newProgress >= entity.totalRequired) {
                        unlock(achievementId)
                    } else {
                        // Progress updated but not yet unlocked
                    }
                    
                    Log.d(TAG, "Updated progress for $achievementId: $newProgress/${entity.totalRequired}")
                } else {
                    // Entity not found or already unlocked
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to increment progress for $achievementId", e)
            }
        }
    }
    
    suspend fun unlock(achievementId: String) {
        withContext(Dispatchers.IO) {
            try {
                val entity = achievementDao.getById(achievementId)
                if (entity != null && !entity.isUnlocked) {
                    val timestamp = System.currentTimeMillis()
                    achievementDao.unlock(achievementId, timestamp)
                    
                    val achievement = achievementDefinitions.find { it.id == achievementId }
                    if (achievement != null) {
                        val unlockedAchievement = achievement.copy(
                            isUnlocked = true,
                            unlockedAt = timestamp
                        )
                        _achievementUnlocked.emit(unlockedAchievement)
                        Log.d(TAG, "Unlocked achievement: ${achievement.title}")
                    } else {
                        // Achievement definition not found
                    }
                } else {
                    // Entity not found or already unlocked
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to unlock achievement $achievementId", e)
            }
        }
    }
    
    suspend fun checkAndUnlock(achievementId: String, condition: Boolean) {
        if (condition) {
            unlock(achievementId)
        }
    }
    
    fun onPatternDetected(patternName: String, confidence: Double) {
        scope.launch {
            incrementProgress("first_pattern")
            incrementProgress("pattern_rookie")
            incrementProgress("pattern_expert")
            incrementProgress("pattern_master")
            incrementProgress("pattern_legend")
            
            if (confidence > 0.9) {
                incrementProgress("high_confidence")
            }
            
            when {
                patternName.contains("head_and_shoulders", ignoreCase = true) ||
                patternName.contains("inverse_head_and_shoulders", ignoreCase = true) -> {
                    incrementProgress("head_shoulders_hunter")
                }
                patternName.contains("triangle", ignoreCase = true) -> {
                    incrementProgress("triangle_tracker")
                }
                patternName.contains("double_top", ignoreCase = true) ||
                patternName.contains("double_bottom", ignoreCase = true) -> {
                    incrementProgress("double_trouble")
                }
                patternName.contains("flag", ignoreCase = true) -> {
                    incrementProgress("flag_finder")
                }
                patternName.contains("cup", ignoreCase = true) -> {
                    incrementProgress("cup_collector")
                }
            }
        }
    }
    
    fun onLessonCompleted(lessonId: String) {
        scope.launch {
            incrementProgress("first_lesson")
            incrementProgress("lesson_learner")
            incrementProgress("education_enthusiast")
            incrementProgress("scholar")
        }
    }
    
    fun onBookPageRead(pageNumber: Int) {
        scope.launch {
            incrementProgress("book_reader")
            incrementProgress("bookworm")
            incrementProgress("page_turner")
        }
    }
    
    fun onIntelligenceFeatureUsed(featureName: String) {
        scope.launch {
            when (featureName.lowercase()) {
                "regime_navigator" -> incrementProgress("regime_navigator")
                "pattern_planner" -> incrementProgress("pattern_planner")
                "behavioral_guardrails" -> incrementProgress("behavioral_guardian")
                "proof_capsules" -> incrementProgress("proof_creator")
            }
            incrementProgress("intelligence_explorer")
        }
    }
    
    fun onOnboardingCompleted() {
        scope.launch {
            unlock("welcome_aboard")
        }
    }
    
    fun onProUpgrade() {
        scope.launch {
            unlock("pro_upgrade")
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AchievementManager? = null
        
        fun getInstance(context: Context): AchievementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AchievementManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
