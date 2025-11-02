package com.lamontlabs.quantravision.learning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.PatternOutcome
import com.lamontlabs.quantravision.learning.model.Difficulty
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
class PatternDifficultyScorerTest {
    
    private lateinit var context: Context
    private lateinit var scorer: PatternDifficultyScorer
    private lateinit var db: PatternDatabase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = PatternDatabase.getInstance(context)
        scorer = PatternDifficultyScorer(context)
    }
    
    @After
    fun tearDown() = runBlocking {
        db.close()
    }
    
    @Test
    fun testUnknownDifficultyWithInsufficientData() = runBlocking {
        db.patternOutcomeDao().insert(
            PatternOutcome(0, 1, "New Pattern", Outcome.WIN, System.currentTimeMillis())
        )
        
        val difficulty = scorer.getDifficulty("New Pattern")
        
        assertEquals(Difficulty.UNKNOWN, difficulty, 
            "Should return UNKNOWN with insufficient data (< 5 outcomes)")
    }
    
    @Test
    fun testEasyDifficulty() = runBlocking {
        val patternType = "Easy Pattern"
        
        repeat(15) {
            val outcome = if (it < 13) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        val difficulty = scorer.getDifficulty(patternType)
        
        assertTrue(difficulty == Difficulty.EASY || difficulty == Difficulty.MEDIUM, 
            "High win rate pattern should be EASY or MEDIUM")
    }
    
    @Test
    fun testHardDifficulty() = runBlocking {
        val patternType = "Hard Pattern"
        
        repeat(15) {
            val outcome = if (it < 5) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        val difficulty = scorer.getDifficulty(patternType)
        
        assertEquals(Difficulty.HARD, difficulty, 
            "Low win rate pattern should be HARD")
    }
    
    @Test
    fun testDifficultyDetails() = runBlocking {
        val patternType = "Test Pattern"
        
        repeat(10) {
            val outcome = if (it < 6) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        val details = scorer.getDifficultyDetails(patternType)
        
        assertEquals(patternType, details.patternType)
        assertTrue(details.winRate > 0.0, "Should have calculated win rate")
        assertTrue(details.sampleSize == 10, "Should have correct sample size")
        assertTrue(details.recommendedAction.isNotEmpty(), 
            "Should provide recommended action")
    }
    
    @Test
    fun testMediumDifficulty() = runBlocking {
        val patternType = "Medium Pattern"
        
        repeat(12) {
            val outcome = if (it < 7) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        val difficulty = scorer.getDifficulty(patternType)
        
        assertTrue(difficulty == Difficulty.MEDIUM || difficulty == Difficulty.EASY, 
            "Moderate win rate pattern should be MEDIUM or EASY")
    }
    
    @Test
    fun testGetAllDifficulties() = runBlocking {
        repeat(8) {
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Pattern A", Outcome.WIN, System.currentTimeMillis())
            )
        }
        repeat(6) {
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Pattern B", Outcome.LOSS, System.currentTimeMillis())
            )
        }
        
        val allDifficulties = scorer.getAllDifficulties()
        
        assertTrue(allDifficulties.containsKey("Pattern A"), 
            "Should include Pattern A")
        assertTrue(allDifficulties.containsKey("Pattern B"), 
            "Should include Pattern B")
    }
}
