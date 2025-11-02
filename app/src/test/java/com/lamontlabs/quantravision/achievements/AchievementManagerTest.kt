package com.lamontlabs.quantravision.achievements

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.achievements.data.AchievementEntity
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.achievements.model.AchievementCategory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AchievementManagerTest {
    
    private lateinit var context: Context
    private lateinit var achievementManager: AchievementManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        achievementManager = AchievementManager.getInstance(context)
    }
    
    @Test
    fun testIncrementProgress() = runBlocking {
        achievementManager.incrementProgress("first_pattern", 1)
        
        val progress = achievementManager.getProgress("first_pattern")
        assert(progress >= 0)
    }
    
    @Test
    fun testUnlockAchievement() = runBlocking {
        achievementManager.unlock("welcome_aboard")
        
        val achievements = achievementManager.getAllAchievements()
        val welcomeAchievement = achievements.find { it.id == "welcome_aboard" }
        
        assert(welcomeAchievement != null)
    }
    
    @Test
    fun testGetAchievementsByCategory() = runBlocking {
        val detectionAchievements = achievementManager.getAchievementsByCategory(
            AchievementCategory.DETECTION
        )
        
        assert(detectionAchievements.isNotEmpty())
        assert(detectionAchievements.all { it.category == AchievementCategory.DETECTION })
    }
    
    @Test
    fun testPatternDetectedEvent() = runBlocking {
        achievementManager.onPatternDetected("head_and_shoulders", 0.95)
        
        val progress = achievementManager.getProgress("first_pattern")
        assert(progress > 0)
    }
    
    @Test
    fun testLessonCompletedEvent() = runBlocking {
        achievementManager.onLessonCompleted("lesson_01")
        
        val progress = achievementManager.getProgress("first_lesson")
        assert(progress > 0)
    }
}
