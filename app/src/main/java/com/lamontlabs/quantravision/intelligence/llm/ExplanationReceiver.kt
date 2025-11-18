package com.lamontlabs.quantravision.intelligence.llm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * BroadcastReceiver that handles "Explain Pattern" action from notifications
 * 
 * When user taps "Explain" on a pattern notification:
 * 1. Retrieves pattern data from intent extras
 * 2. Calls PatternExplainer to generate explanation
 * 3. Shows explanation in a new notification
 */
class ExplanationReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_EXPLAIN = "com.lamontlabs.quantravision.ACTION_EXPLAIN_PATTERN"
        const val EXTRA_PATTERN_NAME = "pattern_name"
        const val EXTRA_QUANTRA_SCORE = "quantra_score"
        const val EXTRA_CONFIDENCE = "confidence"
        const val EXTRA_INDICATORS_JSON = "indicators_json"
        const val EXTRA_PATTERN_ID = "pattern_id"
        
        private const val EXPLANATION_NOTIFICATION_ID_BASE = 1000
        private const val CHANNEL_ID_EXPLANATIONS = "PatternExplanations"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_EXPLAIN) return
        
        // Extract pattern data from intent
        val patternName = intent.getStringExtra(EXTRA_PATTERN_NAME) ?: return
        val quantraScore = intent.getIntExtra(EXTRA_QUANTRA_SCORE, 0)
        val confidence = intent.getDoubleExtra(EXTRA_CONFIDENCE, 0.0)
        val indicatorsJson = intent.getStringExtra(EXTRA_INDICATORS_JSON)
        val patternId = intent.getLongExtra(EXTRA_PATTERN_ID, 0L)
        
        Timber.i("🧠 Explain requested for: $patternName (score=$quantraScore)")
        
        // Show loading notification immediately
        showLoadingNotification(context, patternName, patternId)
        
        // Generate explanation in background
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            try {
                val explainer = PatternExplainer(context)
                explainer.initialize()
                
                // Build minimal PatternMatch for explanation
                val indicators = com.lamontlabs.quantravision.intelligence.IndicatorContext.fromJson(indicatorsJson)
                
                // For now, use simplified explanation without full PatternMatch
                // (We'd need to query database for full match, but fallback works fine)
                val result = explainer.explainPattern(
                    pattern = createMockPattern(patternName, quantraScore, confidence, indicatorsJson),
                    indicators = indicators,
                    scoreResult = null
                )
                
                // Show explanation notification
                when (result) {
                    is ExplanationResult.Success -> {
                        showExplanationNotification(
                            context, 
                            patternName, 
                            result.text,
                            patternId,
                            fromCache = result.fromCache
                        )
                    }
                    is ExplanationResult.Unavailable -> {
                        showExplanationNotification(
                            context,
                            patternName,
                            result.fallbackText,
                            patternId,
                            fromCache = false
                        )
                    }
                    is ExplanationResult.Failure -> {
                        showErrorNotification(context, patternName, result.error, patternId)
                    }
                }
                
            } catch (e: Exception) {
                Timber.e(e, "🧠 Failed to generate explanation")
                showErrorNotification(context, patternName, e.message ?: "Unknown error", patternId)
            }
        }
    }
    
    /**
     * Show loading notification while explanation is being generated
     */
    private fun showLoadingNotification(context: Context, patternName: String, patternId: Long) {
        createExplanationChannel(context)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPLANATIONS)
            .setSmallIcon(R.drawable.ic_overlay_marker)
            .setContentTitle("🧠 Analyzing $patternName...")
            .setContentText("Generating explanation...")
            .setProgress(0, 0, true)  // Indeterminate progress
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(getNotificationId(patternId), notification)
    }
    
    /**
     * Show explanation in notification
     */
    private fun showExplanationNotification(
        context: Context,
        patternName: String,
        explanation: String,
        patternId: Long,
        fromCache: Boolean
    ) {
        createExplanationChannel(context)
        
        // Create intent to launch app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)
        
        // Build notification with big text style
        val title = if (fromCache) "🧠 $patternName" else "🧠 AI Insight: $patternName"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPLANATIONS)
            .setSmallIcon(R.drawable.ic_overlay_marker)
            .setContentTitle(title)
            .setContentText(explanation)
            .setStyle(NotificationCompat.BigTextStyle().bigText(explanation))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(getNotificationId(patternId), notification)
        
        Timber.i("🧠 Explanation shown for $patternName (fromCache=$fromCache)")
    }
    
    /**
     * Show error notification
     */
    private fun showErrorNotification(context: Context, patternName: String, error: String, patternId: Long) {
        createExplanationChannel(context)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPLANATIONS)
            .setSmallIcon(R.drawable.ic_overlay_marker)
            .setContentTitle("❌ Explanation unavailable")
            .setContentText("Could not generate explanation for $patternName")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Error: $error"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(getNotificationId(patternId), notification)
    }
    
    /**
     * Create notification channel for explanations
     */
    private fun createExplanationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EXPLANATIONS,
                "Pattern Explanations",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "AI-generated explanations for detected patterns"
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Get unique notification ID for each pattern
     */
    private fun getNotificationId(patternId: Long): Int {
        return (EXPLANATION_NOTIFICATION_ID_BASE + (patternId % 1000)).toInt()
    }
    
    /**
     * Create minimal PatternMatch for fallback explanations
     */
    private fun createMockPattern(
        patternName: String,
        quantraScore: Int,
        confidence: Double,
        indicatorsJson: String?
    ): com.lamontlabs.quantravision.PatternMatch {
        return com.lamontlabs.quantravision.PatternMatch(
            patternName = patternName,
            confidence = confidence,
            timestamp = System.currentTimeMillis(),
            timeframe = "unknown",
            scale = 1.0,
            consensusScore = confidence,
            windowMs = 0L,
            originPath = "notification",
            detectionBounds = null,
            quantraScore = quantraScore,
            indicatorsJson = indicatorsJson
        )
    }
}
