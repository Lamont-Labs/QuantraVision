package com.lamontlabs.quantravision.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.intelligence.IndicatorContext
import com.lamontlabs.quantravision.intelligence.QuantraScorer
import com.lamontlabs.quantravision.intelligence.llm.ExplanationReceiver
import kotlinx.coroutines.*
import timber.log.Timber

/**
 * Manages pattern detection notifications with expandable InboxStyle.
 * 
 * Features:
 * - Two priority channels: HIGH_CONFIDENCE (>0.85) and NORMAL
 * - Expandable notification showing all detected patterns
 * - Auto-dismisses after 10 seconds
 * - Sound/vibration for high confidence patterns only
 * - Tap to launch MainActivity
 */
class PatternNotificationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "PatternNotificationManager"
        private const val NOTIFICATION_ID = 2  // ID 1 is used by foreground service
        
        private const val CHANNEL_ID_HIGH_CONFIDENCE = "PatternDetection_HighConfidence"
        private const val CHANNEL_ID_NORMAL = "PatternDetection_Normal"
        
        private const val AUTO_DISMISS_DELAY_MS = 10_000L  // 10 seconds
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.85
    }
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    private var autoDismissJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        createNotificationChannels()
    }
    
    /**
     * Creates two notification channels with different priority levels.
     * HIGH_CONFIDENCE: IMPORTANCE_HIGH for patterns with confidence > 0.85
     * NORMAL: IMPORTANCE_DEFAULT for other patterns
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // High confidence channel (IMPORTANCE_HIGH)
                val highConfidenceChannel = NotificationChannel(
                    CHANNEL_ID_HIGH_CONFIDENCE,
                    "High Confidence Patterns",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for high confidence pattern detections (>85%)"
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                }
                
                // Normal channel (IMPORTANCE_DEFAULT)
                val normalChannel = NotificationChannel(
                    CHANNEL_ID_NORMAL,
                    "Pattern Detections",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for pattern detections"
                    enableVibration(false)
                    enableLights(false)
                    setShowBadge(true)
                }
                
                notificationManager.createNotificationChannel(highConfidenceChannel)
                notificationManager.createNotificationChannel(normalChannel)
                
                Timber.d("Notification channels created successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to create notification channels")
            }
        } else {
            Timber.d("Notification channels not supported on API < 26, using legacy notifications")
        }
    }
    
    /**
     * Displays pattern detection results in an expandable notification.
     * 
     * @param patterns List of detected patterns to display
     */
    fun showPatterns(patterns: List<PatternMatch>) {
        if (patterns.isEmpty()) {
            Timber.d("No patterns detected, showing feedback notification")
            showNoPatternsFeedback()
            return
        }
        
        try {
            // Determine if any pattern is high confidence
            val hasHighConfidence = patterns.any { it.confidence > HIGH_CONFIDENCE_THRESHOLD }
            val channelId = if (hasHighConfidence) {
                CHANNEL_ID_HIGH_CONFIDENCE
            } else {
                CHANNEL_ID_NORMAL
            }
            
            // Create PendingIntent to launch MainActivity on tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                pendingIntentFlags
            )
            
            // Build expandable notification using InboxStyle
            val inboxStyle = NotificationCompat.InboxStyle()
            
            // Add each pattern as a line in expanded view with QuantraScore
            patterns.forEach { pattern ->
                val confidencePercent = (pattern.confidence * 100).toInt()
                
                // Get score grade emoji
                val scoreEmoji = when {
                    pattern.quantraScore >= QuantraScorer.THRESHOLD_EXCEPTIONAL -> "🔥"
                    pattern.quantraScore >= QuantraScorer.THRESHOLD_GOOD -> "⭐"
                    pattern.quantraScore >= QuantraScorer.THRESHOLD_FAIR -> "✅"
                    else -> "⚠️"
                }
                
                // Build pattern line with QuantraScore
                val scorePart = if (pattern.quantraScore > 0) {
                    " • $scoreEmoji${pattern.quantraScore}"
                } else {
                    ""
                }
                
                // Add indicator summary if available
                val indicators = IndicatorContext.fromJson(pattern.indicatorsJson)
                val indicatorSummary = indicators?.let {
                    if (it.hasAnyIndicators()) " • ${it.getSummary()}" else ""
                } ?: ""
                
                val line = "${pattern.patternName} (${confidencePercent}%$scorePart)$indicatorSummary"
                inboxStyle.addLine(line)
            }
            
            // Set summary line for expanded view with average score
            val patternCount = patterns.size
            val avgScore = patterns.map { it.quantraScore }.average().toInt()
            val avgScoreText = if (avgScore > 0) " • Avg: $avgScore/100" else ""
            
            inboxStyle.setBigContentTitle("$patternCount pattern${if (patternCount == 1) "" else "s"} detected$avgScoreText")
            inboxStyle.setSummaryText("Tap to view details")
            
            // Create "Explain" action for first pattern (most relevant)
            val firstPattern = patterns.first()
            val explainIntent = Intent(context, ExplanationReceiver::class.java).apply {
                action = ExplanationReceiver.ACTION_EXPLAIN
                putExtra(ExplanationReceiver.EXTRA_PATTERN_NAME, firstPattern.patternName)
                putExtra(ExplanationReceiver.EXTRA_QUANTRA_SCORE, firstPattern.quantraScore)
                putExtra(ExplanationReceiver.EXTRA_CONFIDENCE, firstPattern.confidence)
                putExtra(ExplanationReceiver.EXTRA_INDICATORS_JSON, firstPattern.indicatorsJson)
                putExtra(ExplanationReceiver.EXTRA_PATTERN_ID, firstPattern.id)
            }
            
            val explainPendingIntent = PendingIntent.getBroadcast(
                context,
                firstPattern.id.toInt(),
                explainIntent,
                pendingIntentFlags
            )
            
            // Build notification with AI Explain action
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_overlay_marker)
                .setContentTitle("$patternCount pattern${if (patternCount == 1) "" else "s"} detected")
                .setContentText("Tap to view pattern details")
                .setStyle(inboxStyle)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)  // Dismiss when user swipes away
                .setPriority(
                    if (hasHighConfidence) {
                        NotificationCompat.PRIORITY_HIGH
                    } else {
                        NotificationCompat.PRIORITY_DEFAULT
                    }
                )
                .addAction(
                    R.drawable.ic_overlay_marker,  // Icon for action button
                    "🧠 Explain",  // Action button text
                    explainPendingIntent
                )
                .apply {
                    // Only add sound/vibration for high confidence patterns
                    if (hasHighConfidence) {
                        setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
                    }
                }
                .build()
            
            // Show notification
            notificationManager.notify(NOTIFICATION_ID, notification)
            
            Timber.d("Notification shown: $patternCount patterns (highConfidence=$hasHighConfidence)")
            
            // Schedule auto-dismiss after 10 seconds
            scheduleAutoDismiss()
            
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException showing notification - notification permission not granted")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show pattern notification")
        }
    }
    
    /**
     * Shows feedback notification when no patterns are detected.
     */
    private fun showNoPatternsFeedback() {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                pendingIntentFlags
            )
            
            val notification = NotificationCompat.Builder(context, CHANNEL_ID_NORMAL)
                .setSmallIcon(R.drawable.ic_overlay_marker)
                .setContentTitle("No Patterns Detected")
                .setContentText("Chart scanned successfully — no recognizable patterns found")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
            Timber.d("No-patterns feedback notification shown")
            
            // Cancel any existing auto-dismiss job and schedule new one for 5 seconds
            autoDismissJob?.cancel()
            autoDismissJob = scope.launch {
                delay(5000L)
                dismiss()
                Timber.d("No-patterns notification auto-dismissed after 5s")
            }
            
        } catch (e: SecurityException) {
            Timber.e(e, "SecurityException showing notification - notification permission not granted")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show no-patterns notification")
        }
    }
    
    /**
     * Schedules automatic dismissal of notification after 10 seconds.
     */
    private fun scheduleAutoDismiss() {
        // Cancel any existing auto-dismiss job
        autoDismissJob?.cancel()
        
        autoDismissJob = scope.launch {
            delay(AUTO_DISMISS_DELAY_MS)
            dismiss()
            Timber.d("Notification auto-dismissed after ${AUTO_DISMISS_DELAY_MS}ms")
        }
    }
    
    /**
     * Manually dismisses the pattern notification.
     */
    fun dismiss() {
        try {
            notificationManager.cancel(NOTIFICATION_ID)
            autoDismissJob?.cancel()
            Timber.d("Notification dismissed")
        } catch (e: Exception) {
            Timber.e(e, "Failed to dismiss notification")
        }
    }
    
    /**
     * Cleans up resources. Call when done using this manager.
     */
    fun cleanup() {
        autoDismissJob?.cancel()
        scope.cancel()
        Timber.d("PatternNotificationManager cleaned up")
    }
}
