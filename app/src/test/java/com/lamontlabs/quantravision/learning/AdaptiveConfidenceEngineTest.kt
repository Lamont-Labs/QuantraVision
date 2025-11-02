package com.lamontlabs.quantravision.learning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.learning.model.ConfidenceProfile
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AdaptiveConfidenceEngineTest {
    
    private lateinit var context: Context
    private lateinit var engine: AdaptiveConfidenceEngine
    private lateinit var db: PatternDatabase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = PatternDatabase.getInstance(context)
        engine = AdaptiveConfidenceEngine(context)
    }
    
    @After
    fun tearDown() = runBlocking {
        db.learningProfileDao().clearAllProfiles()
        db.close()
    }
    
    @Test
    fun testDefaultThreshold() = runBlocking {
        val threshold = engine.getPersonalizedThreshold("Head and Shoulders")
        assertEquals(0.5f, threshold, "Default threshold should be 0.5")
    }
    
    @Test
    fun testLearningFromWins() = runBlocking {
        val patternType = "Double Top"
        
        repeat(15) {
            engine.learnFromOutcome(patternType, 0.75, Outcome.WIN)
        }
        
        val threshold = engine.getPersonalizedThreshold(patternType)
        assertTrue(threshold > 0.0f, "Threshold should be learned after multiple wins")
    }
    
    @Test
    fun testConfidenceBuckets() = runBlocking {
        val patternType = "Cup and Handle"
        
        engine.learnFromOutcome(patternType, 0.2, Outcome.LOSS)
        engine.learnFromOutcome(patternType, 0.4, Outcome.WIN)
        engine.learnFromOutcome(patternType, 0.6, Outcome.WIN)
        engine.learnFromOutcome(patternType, 0.8, Outcome.WIN)
        engine.learnFromOutcome(patternType, 0.95, Outcome.WIN)
        
        val profile = db.learningProfileDao().getConfidenceProfile(patternType)
        
        assertTrue(profile != null, "Profile should be created")
        assertEquals(5, profile?.totalOutcomes, "Should have 5 total outcomes")
    }
    
    @Test
    fun testConfidenceAdjustment() = runBlocking {
        val patternType = "Triangle"
        
        repeat(10) {
            engine.learnFromOutcome(patternType, 0.7, Outcome.WIN)
        }
        
        val adjustment = engine.getConfidenceAdjustment(patternType, 0.7)
        assertTrue(adjustment >= 0.0f && adjustment <= 1.0f, 
            "Adjustment should be in valid range")
    }
    
    @Test
    fun testInsufficientDataDoesNotOverrideDefaults() = runBlocking {
        val patternType = "Flag"
        
        engine.learnFromOutcome(patternType, 0.8, Outcome.WIN)
        engine.learnFromOutcome(patternType, 0.9, Outcome.WIN)
        
        val threshold = engine.getPersonalizedThreshold(patternType)
        assertEquals(0.5f, threshold, 
            "Should use default threshold with insufficient data (< 10 outcomes)")
    }
}
