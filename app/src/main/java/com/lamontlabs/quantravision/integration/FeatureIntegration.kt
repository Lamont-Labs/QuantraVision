package com.lamontlabs.quantravision.integration

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.analytics.PatternPerformanceTracker
import com.lamontlabs.quantravision.audit.DetectionAuditTrail
import com.lamontlabs.quantravision.gamification.AchievementSystem
import com.lamontlabs.quantravision.gamification.UserStats
import com.lamontlabs.quantravision.widget.QuantraVisionWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
