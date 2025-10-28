package com.lamontlabs.quantravision.integration

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.PredictedPattern
import com.lamontlabs.quantravision.analytics.PatternPerformanceTracker
import com.lamontlabs.quantravision.audit.DetectionAuditTrail
import com.lamontlabs.quantravision.gamification.AchievementSystem
import com.lamontlabs.quantravision.gamification.UserStats
import com.lamontlabs.quantravision.widget.QuantraVisionWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

/**
 * FeatureIntegration
 * Centralized integration point for all new features
 * Ensures features are properly invoked during detection lifecycle
 */
object FeatureIntegration {

    /**
     * Call this after every successful pattern detection
     * Updates all analytics, stats, and achievement systems
     */
    suspend fun onPatternDetected(
        context: Context,
        match: PatternMatch
    ) = withContext(Dispatchers.IO) {
        try {
            // Update performance analytics
            PatternPerformanceTracker.recordDetection(
                context,
                match.patternName,
                match.confidence,
                match.timeframe
            )

            // Update user stats and check achievements
            UserStats.incrementDetection(context, match.patternName, match.confidence)

            // Record audit trail with reasoning
            val reasoning = DetectionAuditTrail.explainDetection(match)
            DetectionAuditTrail.recordDetection(context, match, reasoning)

            // Update home screen widget
            QuantraVisionWidget.updateAllWidgets(context)

        } catch (e: Exception) {
            // Log but don't fail detection on analytics error
            timber.log.Timber.e(e, "Feature integration error")
        }
    }
    
    /**
     * Call this after a prediction is generated
     * Updates prediction statistics and analytics
     */
    suspend fun onPredictionGenerated(
        context: Context,
        prediction: PredictedPattern
    ) = withContext(Dispatchers.IO) {
        try {
            // Update prediction statistics
            val statsFile = File(context.filesDir, "prediction_stats.json")
            val stats = if (statsFile.exists()) {
                JSONObject(statsFile.readText())
            } else {
                JSONObject()
            }
            
            // Increment total predictions count
            val totalPredictions = stats.optInt("total_predictions", 0) + 1
            stats.put("total_predictions", totalPredictions)
            
            // Track predictions by stage
            val byStage = stats.optJSONObject("by_stage") ?: JSONObject()
            val stageCount = byStage.optInt(prediction.stage, 0) + 1
            byStage.put(prediction.stage, stageCount)
            stats.put("by_stage", byStage)
            
            // Track predictions by pattern name
            val byPattern = stats.optJSONObject("by_pattern") ?: JSONObject()
            val patternCount = byPattern.optInt(prediction.patternName, 0) + 1
            byPattern.put(prediction.patternName, patternCount)
            stats.put("by_pattern", byPattern)
            
            // Track average completion percentage
            val avgCompletion = stats.optDouble("avg_completion", 0.0)
            val newAvg = (avgCompletion * (totalPredictions - 1) + prediction.completionPercent) / totalPredictions
            stats.put("avg_completion", newAvg)
            
            // Update last prediction timestamp
            stats.put("last_prediction_timestamp", prediction.timestamp)
            
            // Save updated stats
            statsFile.writeText(stats.toString(2))
            
            // Update widget with high-confidence predictions
            if (prediction.confidence >= 0.7) {
                QuantraVisionWidget.updateAllWidgets(context)
            }
            
            timber.log.Timber.d("Prediction stats updated: ${prediction.patternName} (${prediction.stage})")
            
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Prediction tracking error")
        }
    }

    /**
     * Call this when app launches
     * Initializes all feature systems
     */
    fun onAppStartup(context: Context) {
        try {
            // Ensure default data exists
            com.lamontlabs.quantravision.education.GuidedTutorials.ensureDefault(context)
            
            // Update widget on startup
            QuantraVisionWidget.updateAllWidgets(context)
        } catch (e: Exception) {
            timber.log.Timber.e(e, "Startup integration error")
        }
    }

    /**
     * Check if user has access to Pro-only features
     */
    fun canAccessProFeature(context: Context): Boolean {
        return com.lamontlabs.quantravision.licensing.ProFeatureGate.isActive(context)
    }

    /**
     * Check if user has access to a specific feature
     */
    fun canAccessFeature(context: Context, feature: String): Boolean {
        return when (feature) {
            "predictions" -> canAccessProFeature(context)
            "pdf_reports_unwatermarked" -> canAccessProFeature(context)
            "backtesting_full" -> canAccessProFeature(context)
            "multi_chart_comparison" -> canAccessProFeature(context)
            "analytics_basic" -> true // Available to all
            "achievements" -> true // Available to all
            "voice_commands" -> true // Available to all
            "education_course" -> true // Available to all
            else -> false
        }
    }
}
