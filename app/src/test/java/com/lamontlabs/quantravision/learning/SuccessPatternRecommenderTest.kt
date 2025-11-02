package com.lamontlabs.quantravision.learning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.PatternOutcome
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SuccessPatternRecommenderTest {
    
    private lateinit var context: Context
    private lateinit var recommender: SuccessPatternRecommender
    private lateinit var db: PatternDatabase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = PatternDatabase.getInstance(context)
        recommender = SuccessPatternRecommender(context)
    }
    
    @After
    fun tearDown() = runBlocking {
        db.close()
    }
    
    @Test
    fun testGetBestPatternsWithInsufficientData() = runBlocking {
        val outcomes = listOf(
            PatternOutcome(0, 1, "Pattern A", Outcome.WIN, System.currentTimeMillis()),
            PatternOutcome(0, 2, "Pattern A", Outcome.WIN, System.currentTimeMillis())
        )
        
        outcomes.forEach { db.patternOutcomeDao().insert(it) }
        
        val bestPatterns = recommender.getBestPatterns(5)
        
        assertTrue(bestPatterns.isEmpty(), 
            "Should not recommend patterns with insufficient data (< 5 outcomes)")
    }
    
    @Test
    fun testGetBestPatternsWithSufficientData() = runBlocking {
        repeat(8) {
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Good Pattern", Outcome.WIN, System.currentTimeMillis())
            )
        }
        repeat(2) {
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it + 10, "Good Pattern", Outcome.LOSS, System.currentTimeMillis())
            )
        }
        
        val bestPatterns = recommender.getBestPatterns(5)
        
        assertTrue(bestPatterns.isNotEmpty(), "Should find patterns with sufficient data")
        assertTrue(bestPatterns[0].winRate >= 0.6, "Best pattern should have >= 60% win rate")
    }
    
    @Test
    fun testPatternScore() = runBlocking {
        repeat(10) {
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Test Pattern", Outcome.WIN, System.currentTimeMillis())
            )
        }
        
        val score = recommender.getPatternScore("Test Pattern")
        
        assertTrue(score > 0.0f, "Pattern with outcomes should have positive score")
    }
    
    @Test
    fun testShouldRecommend() = runBlocking {
        repeat(12) {
            val outcome = if (it < 8) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Recommend Test", outcome, System.currentTimeMillis())
            )
        }
        
        val shouldRecommend = recommender.shouldRecommend("Recommend Test")
        
        assertTrue(shouldRecommend, 
            "Should recommend pattern with >= 60% win rate and 10+ outcomes")
    }
    
    @Test
    fun testShouldNotRecommendLowWinRate() = runBlocking {
        repeat(15) {
            val outcome = if (it < 5) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Poor Pattern", outcome, System.currentTimeMillis())
            )
        }
        
        val shouldRecommend = recommender.shouldRecommend("Poor Pattern")
        
        assertFalse(shouldRecommend, 
            "Should not recommend pattern with < 60% win rate")
    }
}
