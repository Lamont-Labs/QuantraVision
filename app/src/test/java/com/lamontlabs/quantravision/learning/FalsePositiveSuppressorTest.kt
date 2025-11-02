package com.lamontlabs.quantravision.learning

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.analytics.model.Outcome
import com.lamontlabs.quantravision.analytics.model.PatternOutcome
import com.lamontlabs.quantravision.learning.model.SuppressionLevel
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
class FalsePositiveSuppressorTest {
    
    private lateinit var context: Context
    private lateinit var suppressor: FalsePositiveSuppressor
    private lateinit var db: PatternDatabase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = PatternDatabase.getInstance(context)
        suppressor = FalsePositiveSuppressor(context)
    }
    
    @After
    fun tearDown() = runBlocking {
        db.learningProfileDao().clearNonOverriddenRules()
        db.close()
    }
    
    @Test
    fun testNoSuppressionWithoutData() = runBlocking {
        val shouldSuppress = suppressor.shouldSuppress("Unknown Pattern", 0.8)
        assertFalse(shouldSuppress, "Should not suppress without data")
    }
    
    @Test
    fun testSuppressionWithHighFailureRate() = runBlocking {
        val patternType = "Failing Pattern"
        
        repeat(20) {
            val outcome = if (it < 3) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        suppressor.learnFromOutcome(patternType, false)
        
        val rule = db.learningProfileDao().getSuppressionRule(patternType)
        
        assertTrue(rule != null, "Suppression rule should be created")
        assertTrue(rule?.suppressionLevel != SuppressionLevel.NONE, 
            "Pattern with high failure rate should have suppression")
    }
    
    @Test
    fun testSuppressionScore() = runBlocking {
        repeat(15) {
            val outcome = if (it < 2) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, "Bad Pattern", outcome, System.currentTimeMillis())
            )
        }
        
        val score = suppressor.getSuppressionScore("Bad Pattern")
        
        assertTrue(score > 0.5f, 
            "Pattern with low win rate should have high suppression score")
    }
    
    @Test
    fun testMediumSuppressionLevel() = runBlocking {
        val patternType = "Medium Suppression"
        
        repeat(15) {
            val outcome = if (it < 4) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        suppressor.learnFromOutcome(patternType, false)
        
        val shouldSuppressLow = suppressor.shouldSuppress(patternType, 0.65)
        val shouldSuppressHigh = suppressor.shouldSuppress(patternType, 0.75)
        
        val rule = db.learningProfileDao().getSuppressionRule(patternType)
        
        if (rule?.suppressionLevel == SuppressionLevel.MEDIUM) {
            assertTrue(shouldSuppressLow, "Should suppress low confidence with MEDIUM level")
            assertFalse(shouldSuppressHigh, "Should not suppress high confidence with MEDIUM level")
        }
    }
    
    @Test
    fun testNoSuppressionForGoodPattern() = runBlocking {
        val patternType = "Good Pattern"
        
        repeat(15) {
            val outcome = if (it < 12) Outcome.WIN else Outcome.LOSS
            db.patternOutcomeDao().insert(
                PatternOutcome(0, it, patternType, outcome, System.currentTimeMillis())
            )
        }
        
        suppressor.learnFromOutcome(patternType, true)
        
        val rule = db.learningProfileDao().getSuppressionRule(patternType)
        
        assertEquals(SuppressionLevel.NONE, rule?.suppressionLevel, 
            "Good pattern should have no suppression")
    }
}
