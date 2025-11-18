package com.lamontlabs.quantravision.learning.adaptive

import android.content.Context
import com.lamontlabs.quantravision.PatternDao
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Pattern Learning Engine
 * 
 * Self-learning system that continuously improves pattern detection accuracy.
 * Learns from every scan to build statistical profiles of indicators for each pattern.
 * 
 * Architecture:
 * 1. HistoricalAnalyzer - Analyzes past scans to discover patterns
 * 2. ProfileBuilder - Builds statistical models for each pattern type
 * 3. AdaptiveScorer - Adjusts QuantraScore based on learned profiles
 * 
 * Learning Phases:
 * - BASELINE (0-19 scans): Collecting initial data, no adjustments
 * - LEARNING (20-99 scans): Building profiles, small adjustments
 * - ADAPTIVE (100-499 scans): Fully adaptive scoring
 * - EXPERT (500+ scans): High confidence, maximum adjustments
 */
class PatternLearningEngine(
    private val context: Context,
    private val patternDao: PatternDao,
    private val learningDao: PatternLearningDao
) {
    
    private val analyzer = HistoricalAnalyzer(patternDao)
    private val scope = CoroutineScope(Dispatchers.Default)
    
    companion object {
        private const val TAG = "PatternLearningEngine"
        
        // Learning phase thresholds
        private const val BASELINE_THRESHOLD = 20
        private const val LEARNING_THRESHOLD = 100
        private const val ADAPTIVE_THRESHOLD = 100
        private const val EXPERT_THRESHOLD = 500
        
        // Trigger thresholds
        private const val AUTO_LEARN_INTERVAL = 50  // Re-learn every N scans
    }
    
    /**
     * Initialize learning engine
     * Check if we should trigger learning on startup
     */
    suspend fun initialize() {
        try {
            val totalScans = patternDao.getAll().size
            val adaptiveCount = learningDao.getAdaptiveProfileCount()
            
            Timber.i("🧠 Pattern Learning Engine initialized: $totalScans total scans, $adaptiveCount adaptive profiles")
            
            // Trigger initial learning if we have enough data but no profiles
            if (totalScans >= BASELINE_THRESHOLD && adaptiveCount == 0) {
                Timber.i("🎓 Triggering initial learning phase")
                triggerLearning()
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize learning engine")
        }
    }
    
    /**
     * Record a new scan
     * Triggers learning if conditions met
     */
    suspend fun recordScan(match: PatternMatch) {
        try {
            // Update pattern profile scan count
            val profile = learningDao.getProfile(match.patternName)
            
            if (profile != null) {
                val updated = profile.copy(
                    totalScans = profile.totalScans + 1,
                    lastUpdated = System.currentTimeMillis()
                )
                learningDao.updateProfile(updated)
                
                // Check if we should trigger re-learning
                if (updated.totalScans % AUTO_LEARN_INTERVAL == 0) {
                    Timber.i("🎓 Auto-learning triggered for ${match.patternName} (${updated.totalScans} scans)")
                    scope.launch {
                        learnPattern(match.patternName)
                    }
                }
            } else {
                // Create initial profile
                val newProfile = PatternIndicatorProfile(
                    patternName = match.patternName,
                    totalScans = 1,
                    learningPhase = PatternIndicatorProfile.LearningPhase.BASELINE
                )
                learningDao.insertProfile(newProfile)
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to record scan for learning")
        }
    }
    
    /**
     * Trigger learning for all patterns
     * Runs in background
     */
    fun triggerLearning() {
        scope.launch {
            try {
                Timber.i("🧠 Starting pattern learning analysis...")
                
                val allStats = analyzer.analyzeAllPatterns()
                
                allStats.forEach { (patternName, stats) ->
                    updateProfile(patternName, stats)
                }
                
                val adaptiveCount = learningDao.getAdaptiveProfileCount()
                Timber.i("✅ Learning complete: $adaptiveCount patterns in adaptive phase")
                
            } catch (e: Exception) {
                Timber.e(e, "Learning failed")
            }
        }
    }
    
    /**
     * Learn from historical data for a specific pattern
     */
    suspend fun learnPattern(patternName: String) {
        try {
            val stats = analyzer.analyzePattern(patternName)
            if (stats != null) {
                updateProfile(patternName, stats)
                Timber.i("✅ Learned profile for $patternName")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to learn pattern: $patternName")
        }
    }
    
    /**
     * Update pattern profile with learned statistics
     */
    private suspend fun updateProfile(
        patternName: String,
        stats: Map<String, PatternIndicatorProfile.IndicatorStats>
    ) {
        val existingProfile = learningDao.getProfile(patternName)
        val totalScans = existingProfile?.totalScans ?: stats.values.firstOrNull()?.count ?: 0
        
        // Determine learning phase based on total scans
        val phase = when {
            totalScans < BASELINE_THRESHOLD -> PatternIndicatorProfile.LearningPhase.BASELINE
            totalScans < LEARNING_THRESHOLD -> PatternIndicatorProfile.LearningPhase.LEARNING
            totalScans < EXPERT_THRESHOLD -> PatternIndicatorProfile.LearningPhase.ADAPTIVE
            else -> PatternIndicatorProfile.LearningPhase.EXPERT
        }
        
        val profile = PatternIndicatorProfile(
            patternName = patternName,
            totalScans = totalScans,
            lastUpdated = System.currentTimeMillis(),
            learningPhase = phase
        ).setIndicatorStats(stats)
        
        learningDao.insertProfile(profile)
        
        Timber.d("📊 Updated profile for $patternName: $totalScans scans, phase=$phase")
    }
    
    /**
     * Get adaptive adjustment for a pattern+indicators combo
     * Returns score adjustment (-20 to +20)
     */
    suspend fun getAdaptiveAdjustment(
        patternName: String,
        indicators: IndicatorContext
    ): Double {
        try {
            val profile = learningDao.getProfile(patternName)
            if (profile == null || !profile.isAdaptive()) {
                return 0.0
            }
            
            val currentIndicators = indicators.toUnifiedMap()
            val adjustment = profile.calculateAdjustment(currentIndicators)
            
            if (adjustment != 0.0) {
                Timber.d("🎯 Adaptive adjustment for $patternName: ${adjustment.toInt()} points")
            }
            
            return adjustment
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to get adaptive adjustment")
            return 0.0
        }
    }
    
    /**
     * Get current learning status
     */
    suspend fun getLearningStatus(): LearningStatus {
        val profiles = learningDao.getAllProfiles()
        val adaptiveCount = profiles.count { it.isAdaptive() }
        
        return LearningStatus(
            totalPatterns = profiles.size,
            adaptivePatterns = adaptiveCount,
            baselinePatterns = profiles.count { it.learningPhase == PatternIndicatorProfile.LearningPhase.BASELINE },
            learningPatterns = profiles.count { it.learningPhase == PatternIndicatorProfile.LearningPhase.LEARNING },
            expertPatterns = profiles.count { it.learningPhase == PatternIndicatorProfile.LearningPhase.EXPERT }
        )
    }
    
    data class LearningStatus(
        val totalPatterns: Int,
        val adaptivePatterns: Int,
        val baselinePatterns: Int,
        val learningPatterns: Int,
        val expertPatterns: Int
    ) {
        val isActive: Boolean get() = adaptivePatterns > 0
        
        override fun toString(): String {
            return "Learning: $adaptivePatterns adaptive, $learningPatterns learning, $baselinePatterns baseline, $expertPatterns expert"
        }
    }
}
