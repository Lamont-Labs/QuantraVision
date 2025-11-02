package com.lamontlabs.quantravision.psychology

import android.content.Context
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.alerts.VoiceAnnouncer
import com.lamontlabs.quantravision.licensing.AdvancedFeatureGate
import com.lamontlabs.quantravision.storage.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import kotlin.math.abs

/**
 * BehavioralGuardrails
 * 
 * Educational trading psychology tool that analyzes usage patterns and provides
 * evidence-based reminders about common psychological pitfalls.
 * 
 * ⚠️ LEGAL NOTICE ⚠️
 * This is NOT psychological counseling, therapy, or medical advice.
 * These are automated educational reminders based on general trading psychology
 * research. If you have mental health concerns, consult a licensed professional.
 * 
 * See legal/ADVANCED_FEATURES_DISCLAIMER.md for full legal terms.
 * 
 * Features:
 * - Cool-down timers (prevent emotional overtrading)
 * - Streak warnings (revenge trading detection)
 * - Activity burst detection (emotional trading patterns)
 * - Discipline coaching (educational reminders)
 * 
 * Data stored in: behavioral_state.json (100% offline)
 */
class BehavioralGuardrails(private val context: Context) {

    private val voiceAnnouncer: VoiceAnnouncer by lazy { VoiceAnnouncer(context) }
    private val stateFile = File(context.filesDir, "behavioral_state.json")
    
    companion object {
        private const val RAPID_VIEW_THRESHOLD = 3
        private const val RAPID_VIEW_WINDOW_MS = 10 * 60 * 1000L // 10 minutes
        private const val COOLDOWN_SUGGESTION_MS = 30 * 60 * 1000L // 30 minutes
        private const val LOSS_STREAK_THRESHOLD = 3
        private const val ACTIVITY_BURST_THRESHOLD = 5
        private const val ACTIVITY_BURST_WINDOW_MS = 5 * 60 * 1000L // 5 minutes
    }
    
    /**
     * Behavioral state
     */
    data class BehavioralState(
        val detectionViews: MutableList<ViewEvent> = mutableListOf(),
        val tradeResults: MutableList<TradeResult> = mutableListOf(),
        val cooldownSuggestions: MutableList<CooldownEvent> = mutableListOf(),
        val disciplineScore: Int = 100,
        val personalityProfile: PersonalityProfile = PersonalityProfile(),
        val lastUpdateTimestamp: Long = System.currentTimeMillis()
    )
    
    data class PersonalityProfile(
        var impulsivenessFactor: Double = 0.5,    // 0.0 = patient, 1.0 = very impulsive
        var emotionalTradingIndicator: Double = 0.5,  // 0.0 = disciplined, 1.0 = emotional
        var avgSessionDurationMs: Long = 0,
        var preferredTimeOfDay: Int = 12  // Hour of day (0-23)
    )
    
    data class ViewEvent(
        val timestamp: Long,
        val patternName: String,
        val confidence: Double
    )
    
    data class TradeResult(
        val timestamp: Long,
        val patternName: String,
        val isWin: Boolean,
        val profitLoss: Double
    )
    
    data class CooldownEvent(
        val timestamp: Long,
        val reason: String,
        val suggestedDurationMs: Long
    )
    
    /**
     * Guardrail warning
     */
    data class GuardrailWarning(
        val type: WarningType,
        val severity: Severity,
        val message: String,
        val voiceMessage: String,
        val suggestedAction: String,
        val disclaimer: String = "⚠️ Educational reminder - NOT psychological advice"
    )
    
    enum class WarningType {
        RAPID_VIEWING,
        LOSS_STREAK,
        ACTIVITY_BURST,
        OVERTRADING,
        EMOTIONAL_PATTERN
    }
    
    enum class Severity {
        INFO,
        CAUTION,
        WARNING
    }
    
    /**
     * Record pattern detection view and update discipline score
     * 
     * @param patternMatch Pattern that was viewed
     * @return Guardrail warning if triggered, null otherwise
     */
    suspend fun recordView(patternMatch: PatternMatch): GuardrailWarning? = withContext(Dispatchers.IO) {
        
        // CRITICAL LEGAL GATE: Enforce disclaimer acceptance
        AdvancedFeatureGate.requireAcceptance(context, "Behavioral Guardrails")
        
        try {
            val state = loadState()
            val now = System.currentTimeMillis()
            
            state.detectionViews.add(ViewEvent(
                timestamp = now,
                patternName = patternMatch.patternName,
                confidence = patternMatch.confidence
            ))
            
            updatePersonalityProfile(state, now)
            updateDisciplineScore(state)
            
            cleanOldData(state)
            saveState(state)
            
            checkForWarnings(state)
            
        } catch (e: Exception) {
            Timber.e(e, "Error recording view")
            null
        }
    }
    
    /**
     * Update personality profile based on recent activity
     */
    private fun updatePersonalityProfile(state: BehavioralState, currentTimestamp: Long) {
        val recentViews = state.detectionViews.filter {
            (currentTimestamp - it.timestamp) < RAPID_VIEW_WINDOW_MS
        }
        
        val impulsiveness = (recentViews.size.toDouble() / RAPID_VIEW_THRESHOLD).coerceIn(0.0, 1.0)
        state.personalityProfile.impulsivenessFactor = 
            (state.personalityProfile.impulsivenessFactor * 0.8 + impulsiveness * 0.2)
        
        val burstViews = state.detectionViews.filter {
            (currentTimestamp - it.timestamp) < ACTIVITY_BURST_WINDOW_MS
        }
        val emotionalIndicator = (burstViews.size.toDouble() / ACTIVITY_BURST_THRESHOLD).coerceIn(0.0, 1.0)
        state.personalityProfile.emotionalTradingIndicator =
            (state.personalityProfile.emotionalTradingIndicator * 0.8 + emotionalIndicator * 0.2)
    }
    
    /**
     * Update discipline score based on behavior
     */
    private fun updateDisciplineScore(state: BehavioralState) {
        var scoreAdjustment = 0
        val now = System.currentTimeMillis()
        
        val recentViews = state.detectionViews.filter {
            (now - it.timestamp) < RAPID_VIEW_WINDOW_MS
        }
        
        if (recentViews.size >= RAPID_VIEW_THRESHOLD) {
            scoreAdjustment -= 5
        }
        
        val burstViews = state.detectionViews.filter {
            (now - it.timestamp) < ACTIVITY_BURST_WINDOW_MS
        }
        if (burstViews.size >= ACTIVITY_BURST_THRESHOLD) {
            scoreAdjustment -= 10
        }
        
        val hoursSinceLastView = if (state.detectionViews.size > 1) {
            val last = state.detectionViews[state.detectionViews.size - 2].timestamp
            (now - last) / (60 * 60 * 1000.0)
        } else {
            0.0
        }
        
        if (hoursSinceLastView > 1.0) {
            scoreAdjustment += 2
        }
        
        if (state.cooldownSuggestions.any { (now - it.timestamp) < it.suggestedDurationMs }) {
            scoreAdjustment -= 15
        }
        
        state.disciplineScore = (state.disciplineScore + scoreAdjustment).coerceIn(0, 100)
    }
    
    /**
     * Record simulated trade result (for learning purposes)
     * 
     * @param patternName Pattern that was traded
     * @param isWin Whether trade was profitable
     * @param profitLoss Profit/loss amount
     * @return Guardrail warning if triggered, null otherwise
     */
    suspend fun recordTradeResult(
        patternName: String,
        isWin: Boolean,
        profitLoss: Double
    ): GuardrailWarning? = withContext(Dispatchers.IO) {
        
        // CRITICAL LEGAL GATE: Enforce disclaimer acceptance
        AdvancedFeatureGate.requireAcceptance(context, "Behavioral Guardrails")
        
        try {
            val state = loadState()
            
            state.tradeResults.add(TradeResult(
                timestamp = System.currentTimeMillis(),
                patternName = patternName,
                isWin = isWin,
                profitLoss = profitLoss
            ))
            
            cleanOldData(state)
            saveState(state)
            
            checkForStreakWarnings(state)
            
        } catch (e: Exception) {
            Timber.e(e, "Error recording trade result")
            null
        }
    }
    
    /**
     * Check if user should take a break (cooldown active)
     * 
     * @return true if cooldown is suggested, false otherwise
     */
    suspend fun shouldTakeBreak(): Boolean = withContext(Dispatchers.IO) {
        try {
            val state = loadState()
            val now = System.currentTimeMillis()
            
            state.cooldownSuggestions.any { cooldown ->
                (now - cooldown.timestamp) < cooldown.suggestedDurationMs
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking cooldown status")
            false
        }
    }
    
    /**
     * Get current behavioral statistics including discipline score
     * 
     * @return Behavioral statistics for UI display
     */
    suspend fun getStatistics(): BehavioralStatistics = withContext(Dispatchers.IO) {
        try {
            val state = loadState()
            val now = System.currentTimeMillis()
            
            val recentViews = state.detectionViews.count { 
                (now - it.timestamp) < 24 * 60 * 60 * 1000L 
            }
            
            val recentTrades = state.tradeResults.filter {
                (now - it.timestamp) < 7 * 24 * 60 * 60 * 1000L
            }
            
            val winRate = if (recentTrades.isNotEmpty()) {
                recentTrades.count { it.isWin }.toDouble() / recentTrades.size
            } else {
                0.0
            }
            
            val currentStreak = calculateCurrentStreak(state.tradeResults)
            val disciplineLevel = calculateDisciplineLevel(state.disciplineScore)
            val personalityInsight = generatePersonalityInsight(state)
            
            BehavioralStatistics(
                viewsLast24h = recentViews,
                tradesLast7d = recentTrades.size,
                winRateLast7d = winRate,
                currentStreak = currentStreak,
                activeCooldown = shouldTakeBreak(),
                disciplineScore = state.disciplineScore,
                disciplineLevel = disciplineLevel,
                personalityInsight = personalityInsight
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error calculating statistics")
            BehavioralStatistics()
        }
    }
    
    /**
     * Calculate discipline level from score
     */
    private fun calculateDisciplineLevel(score: Int): DisciplineLevel {
        return when {
            score >= 90 -> DisciplineLevel.EXCELLENT
            score >= 70 -> DisciplineLevel.GOOD
            score >= 50 -> DisciplineLevel.FAIR
            score >= 30 -> DisciplineLevel.POOR
            else -> DisciplineLevel.CRITICAL
        }
    }
    
    /**
     * Generate personalized insight based on behavioral patterns
     */
    private fun generatePersonalityInsight(state: BehavioralState): String {
        return buildString {
            val profile = state.personalityProfile
            
            when {
                profile.impulsivenessFactor > 0.7 -> {
                    append("📚 You tend to view patterns quickly. ")
                    append("Consider taking 5 minutes between detections to maintain objectivity. ")
                }
                profile.impulsivenessFactor < 0.3 -> {
                    append("✅ You maintain good pacing between pattern views. ")
                }
            }
            
            when {
                profile.emotionalTradingIndicator > 0.7 -> {
                    append("⚠️ Activity patterns suggest emotional decision-making. ")
                    append("Review your trading plan before each action. ")
                }
                profile.emotionalTradingIndicator < 0.3 -> {
                    append("✅ Your activity shows disciplined behavior. ")
                }
            }
            
            append("Educational coaching only - NOT psychological advice.")
        }
    }
    
    /**
     * Announce warning via voice
     * 
     * @param warning Guardrail warning to announce
     */
    fun announceWarning(warning: GuardrailWarning) {
        if (!voiceAnnouncer.isReady()) {
            Timber.w("Voice announcer not ready")
            return
        }
        
        voiceAnnouncer.speak(
            warning.voiceMessage,
            "guardrail_${System.currentTimeMillis()}"
        )
    }
    
    /**
     * Reset behavioral state (for testing or user request)
     */
    suspend fun resetState() = withContext(Dispatchers.IO) {
        try {
            if (stateFile.exists()) {
                stateFile.delete()
            }
            Timber.i("Behavioral state reset")
        } catch (e: Exception) {
            Timber.e(e, "Error resetting behavioral state")
        }
    }
    
    data class BehavioralStatistics(
        val viewsLast24h: Int = 0,
        val tradesLast7d: Int = 0,
        val winRateLast7d: Double = 0.0,
        val currentStreak: Int = 0,
        val activeCooldown: Boolean = false,
        val disciplineScore: Int = 100,
        val disciplineLevel: DisciplineLevel = DisciplineLevel.GOOD,
        val personalityInsight: String = ""
    )
    
    enum class DisciplineLevel {
        EXCELLENT,   // 90-100: Following guardrails consistently
        GOOD,        // 70-89: Generally disciplined
        FAIR,        // 50-69: Some emotional decisions
        POOR,        // 30-49: Frequent emotional trading
        CRITICAL     // 0-29: Severe emotional trading patterns
    }
    
    private fun loadState(): BehavioralState {
        if (!stateFile.exists()) {
            return BehavioralState()
        }
        
        return try {
            val json = JSONObject(stateFile.readText())
            BehavioralState(
                detectionViews = parseViewEvents(json.optJSONArray("detectionViews")),
                tradeResults = parseTradeResults(json.optJSONArray("tradeResults")),
                cooldownSuggestions = parseCooldownEvents(json.optJSONArray("cooldownSuggestions")),
                lastUpdateTimestamp = json.optLong("lastUpdateTimestamp", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            Timber.e(e, "Error loading behavioral state, using defaults")
            BehavioralState()
        }
    }
    
    private fun saveState(state: BehavioralState) {
        try {
            val json = JSONObject().apply {
                put("detectionViews", serializeViewEvents(state.detectionViews))
                put("tradeResults", serializeTradeResults(state.tradeResults))
                put("cooldownSuggestions", serializeCooldownEvents(state.cooldownSuggestions))
                put("lastUpdateTimestamp", System.currentTimeMillis())
            }
            
            AtomicFile.write(stateFile, json.toString(2))
            
        } catch (e: Exception) {
            Timber.e(e, "Error saving behavioral state")
        }
    }
    
    private fun checkForWarnings(state: BehavioralState): GuardrailWarning? {
        val now = System.currentTimeMillis()
        
        val recentViews = state.detectionViews.filter { 
            (now - it.timestamp) < RAPID_VIEW_WINDOW_MS 
        }
        
        if (recentViews.size >= RAPID_VIEW_THRESHOLD) {
            state.cooldownSuggestions.add(CooldownEvent(
                timestamp = now,
                reason = "Rapid pattern viewing",
                suggestedDurationMs = COOLDOWN_SUGGESTION_MS
            ))
            
            saveState(state)
            
            return GuardrailWarning(
                type = WarningType.RAPID_VIEWING,
                severity = Severity.CAUTION,
                message = "📚 You've viewed $RAPID_VIEW_THRESHOLD patterns in ${RAPID_VIEW_WINDOW_MS / 60000} minutes. " +
                         "Consider taking a break to avoid emotional trading decisions.",
                voiceMessage = "You've viewed $RAPID_VIEW_THRESHOLD patterns quickly. " +
                             "Consider taking a break to avoid emotional trading.",
                suggestedAction = "Take a 30-minute break, review your trading plan, or practice mindfulness."
            )
        }
        
        val veryRecentViews = state.detectionViews.filter {
            (now - it.timestamp) < ACTIVITY_BURST_WINDOW_MS
        }
        
        if (veryRecentViews.size >= ACTIVITY_BURST_THRESHOLD) {
            return GuardrailWarning(
                type = WarningType.ACTIVITY_BURST,
                severity = Severity.WARNING,
                message = "📚 Unusual activity burst detected: ${veryRecentViews.size} views in ${ACTIVITY_BURST_WINDOW_MS / 60000} minutes. " +
                         "This pattern often indicates emotional or impulsive behavior.",
                voiceMessage = "Activity burst detected. This may indicate emotional trading. Take a break.",
                suggestedAction = "STOP. Take a mandatory break. Review your trading rules before continuing."
            )
        }
        
        return null
    }
    
    private fun checkForStreakWarnings(state: BehavioralState): GuardrailWarning? {
        val recentTrades = state.tradeResults.takeLast(LOSS_STREAK_THRESHOLD)
        
        if (recentTrades.size >= LOSS_STREAK_THRESHOLD && recentTrades.all { !it.isWin }) {
            return GuardrailWarning(
                type = WarningType.LOSS_STREAK,
                severity = Severity.WARNING,
                message = "📚 You've had $LOSS_STREAK_THRESHOLD consecutive losses. " +
                         "This is a high-risk time for revenge trading and emotional decisions.",
                voiceMessage = "You've had $LOSS_STREAK_THRESHOLD losses in a row. " +
                             "This is a high-risk time for emotional decisions.",
                suggestedAction = "Stop trading immediately. Review your strategy. Do not attempt to 'win it back'."
            )
        }
        
        return null
    }
    
    private fun calculateCurrentStreak(trades: List<TradeResult>): Int {
        if (trades.isEmpty()) return 0
        
        var streak = 0
        val lastResult = trades.last().isWin
        
        for (trade in trades.reversed()) {
            if (trade.isWin == lastResult) {
                streak++
            } else {
                break
            }
        }
        
        return if (lastResult) streak else -streak
    }
    
    private fun cleanOldData(state: BehavioralState) {
        val now = System.currentTimeMillis()
        val weekAgo = now - 7 * 24 * 60 * 60 * 1000L
        
        state.detectionViews.removeIf { it.timestamp < weekAgo }
        state.tradeResults.removeIf { it.timestamp < weekAgo }
        state.cooldownSuggestions.removeIf { it.timestamp < weekAgo }
    }
    
    private fun parseViewEvents(array: JSONArray?): MutableList<ViewEvent> {
        val events = mutableListOf<ViewEvent>()
        array?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                events.add(ViewEvent(
                    timestamp = obj.getLong("timestamp"),
                    patternName = obj.getString("patternName"),
                    confidence = obj.getDouble("confidence")
                ))
            }
        }
        return events
    }
    
    private fun parseTradeResults(array: JSONArray?): MutableList<TradeResult> {
        val results = mutableListOf<TradeResult>()
        array?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                results.add(TradeResult(
                    timestamp = obj.getLong("timestamp"),
                    patternName = obj.getString("patternName"),
                    isWin = obj.getBoolean("isWin"),
                    profitLoss = obj.getDouble("profitLoss")
                ))
            }
        }
        return results
    }
    
    private fun parseCooldownEvents(array: JSONArray?): MutableList<CooldownEvent> {
        val events = mutableListOf<CooldownEvent>()
        array?.let {
            for (i in 0 until it.length()) {
                val obj = it.getJSONObject(i)
                events.add(CooldownEvent(
                    timestamp = obj.getLong("timestamp"),
                    reason = obj.getString("reason"),
                    suggestedDurationMs = obj.getLong("suggestedDurationMs")
                ))
            }
        }
        return events
    }
    
    private fun serializeViewEvents(events: List<ViewEvent>): JSONArray {
        return JSONArray().apply {
            events.forEach { event ->
                put(JSONObject().apply {
                    put("timestamp", event.timestamp)
                    put("patternName", event.patternName)
                    put("confidence", event.confidence)
                })
            }
        }
    }
    
    private fun serializeTradeResults(results: List<TradeResult>): JSONArray {
        return JSONArray().apply {
            results.forEach { result ->
                put(JSONObject().apply {
                    put("timestamp", result.timestamp)
                    put("patternName", result.patternName)
                    put("isWin", result.isWin)
                    put("profitLoss", result.profitLoss)
                })
            }
        }
    }
    
    private fun serializeCooldownEvents(events: List<CooldownEvent>): JSONArray {
        return JSONArray().apply {
            events.forEach { event ->
                put(JSONObject().apply {
                    put("timestamp", event.timestamp)
                    put("reason", event.reason)
                    put("suggestedDurationMs", event.suggestedDurationMs)
                })
            }
        }
    }
    
    private fun VoiceAnnouncer.speak(text: String, utteranceId: String) {
        try {
            val params = android.os.Bundle().apply {
                putString(android.speech.tts.TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val ttsField = this::class.java.getDeclaredField("tts")
            ttsField.isAccessible = true
            val tts = ttsField.get(this) as? android.speech.tts.TextToSpeech
            
            tts?.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, params, utteranceId)
            
        } catch (e: Exception) {
            Timber.e(e, "Error speaking warning")
        }
    }
}
