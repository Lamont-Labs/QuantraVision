package com.lamontlabs.quantravision.learning

import android.content.Context
import com.lamontlabs.quantravision.analytics.model.Outcome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

object LearningIntegration {
    
    private lateinit var adaptiveEngine: AdaptiveConfidenceEngine
    private lateinit var suppressor: FalsePositiveSuppressor
    private var initialized = false
    
    fun initialize(context: Context) {
        if (initialized) return
        
        adaptiveEngine = AdaptiveConfidenceEngine(context)
        suppressor = FalsePositiveSuppressor(context)
        initialized = true
        
        Timber.i("Learning module initialized")
    }
    
    suspend fun onPatternOutcomeRecorded(
        patternType: String,
        confidence: Double,
        outcome: Outcome
    ) {
        if (!initialized) {
            Timber.w("Learning module not initialized")
            return
        }
        
        try {
            adaptiveEngine.learnFromOutcome(patternType, confidence, outcome)
            
            val wasCorrect = outcome == Outcome.WIN
            suppressor.learnFromOutcome(patternType, wasCorrect)
            
            Timber.d("Learning updated for $patternType: $outcome")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update learning for $patternType")
        }
    }
    
    suspend fun shouldSuppressPattern(patternType: String, confidence: Double): Boolean {
        if (!initialized) return false
        
        return try {
            suppressor.shouldSuppress(patternType, confidence)
        } catch (e: Exception) {
            Timber.e(e, "Failed to check suppression for $patternType")
            false
        }
    }
    
    suspend fun getAdjustedConfidence(patternType: String, rawConfidence: Double): Float {
        if (!initialized) return rawConfidence.toFloat()
        
        return try {
            adaptiveEngine.getConfidenceAdjustment(patternType, rawConfidence)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get confidence adjustment for $patternType")
            rawConfidence.toFloat()
        }
    }
}
