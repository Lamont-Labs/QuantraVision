package com.lamontlabs.quantravision.licensing

import android.content.Context
import com.lamontlabs.quantravision.storage.AtomicFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File

/**
 * AdvancedFeatureGate
 * 
 * Ensures users accept legal disclaimers before accessing advanced features:
 * - Regime Navigator
 * - Pattern-to-Plan Engine
 * - Behavioral Guardrails
 * - Proof Capsules
 * 
 * LEGAL: Users must explicitly accept legal/ADVANCED_FEATURES_DISCLAIMER.md
 * before using ANY advanced feature. This protects both users and Lamont Labs
 * by ensuring informed consent about educational nature of features.
 * 
 * Acceptance is stored in: app/filesDir/advanced_features_accepted.json
 * 
 * Format:
 * {
 *   "accepted": true,
 *   "timestamp": 1730397000000,
 *   "version": "2025.10.31",
 *   "disclaimer_version": "1.0"
 * }
 */
object AdvancedFeatureGate {

    private const val ACCEPTANCE_FILE = "advanced_features_accepted.json"
    private const val DISCLAIMER_VERSION = "1.0"
    private const val CURRENT_VERSION = "2025.10.31"
    
    private var cachedAcceptance: Boolean? = null
    
    /**
     * Check if user has accepted advanced features disclaimer
     * 
     * @param context Android context
     * @return true if accepted, false otherwise
     */
    suspend fun hasAccepted(context: Context): Boolean = withContext(Dispatchers.IO) {
        cachedAcceptance?.let { return@withContext it }
        
        try {
            val file = File(context.filesDir, ACCEPTANCE_FILE)
            if (!file.exists()) {
                cachedAcceptance = false
                return@withContext false
            }
            
            val json = JSONObject(file.readText())
            val accepted = json.optBoolean("accepted", false)
            val disclaimerVersion = json.optString("disclaimer_version", "")
            
            val isValid = accepted && disclaimerVersion == DISCLAIMER_VERSION
            cachedAcceptance = isValid
            
            Timber.d("Advanced features acceptance: $isValid (version: $disclaimerVersion)")
            isValid
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking advanced features acceptance")
            cachedAcceptance = false
            false
        }
    }
    
    /**
     * Check if user has accepted advanced features disclaimer (synchronous)
     * 
     * WARNING: Only use this from background threads. Use hasAccepted() for coroutines.
     * 
     * @param context Android context
     * @return true if accepted, false otherwise
     */
    fun hasAcceptedSync(context: Context): Boolean {
        cachedAcceptance?.let { return it }
        
        try {
            val file = File(context.filesDir, ACCEPTANCE_FILE)
            if (!file.exists()) {
                cachedAcceptance = false
                return false
            }
            
            val json = JSONObject(file.readText())
            val accepted = json.optBoolean("accepted", false)
            val disclaimerVersion = json.optString("disclaimer_version", "")
            
            val isValid = accepted && disclaimerVersion == DISCLAIMER_VERSION
            cachedAcceptance = isValid
            
            return isValid
            
        } catch (e: Exception) {
            Timber.e(e, "Error checking advanced features acceptance (sync)")
            cachedAcceptance = false
            return false
        }
    }
    
    /**
     * Record user's acceptance of advanced features disclaimer
     * 
     * @param context Android context
     */
    suspend fun recordAcceptance(context: Context) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, ACCEPTANCE_FILE)
            
            val json = JSONObject().apply {
                put("accepted", true)
                put("timestamp", System.currentTimeMillis())
                put("version", CURRENT_VERSION)
                put("disclaimer_version", DISCLAIMER_VERSION)
                put("disclaimer_path", "legal/ADVANCED_FEATURES_DISCLAIMER.md")
            }
            
            AtomicFile.write(file, json.toString(2))
            cachedAcceptance = true
            
            Timber.i("Advanced features disclaimer accepted (version: $DISCLAIMER_VERSION)")
            
        } catch (e: Exception) {
            Timber.e(e, "Error recording advanced features acceptance")
            throw e
        }
    }
    
    /**
     * Revoke user's acceptance (for testing or if user wants to disable)
     * 
     * @param context Android context
     */
    suspend fun revokeAcceptance(context: Context) = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, ACCEPTANCE_FILE)
            if (file.exists()) {
                file.delete()
            }
            
            cachedAcceptance = false
            Timber.i("Advanced features acceptance revoked")
            
        } catch (e: Exception) {
            Timber.e(e, "Error revoking advanced features acceptance")
        }
    }
    
    /**
     * Get acceptance details (for UI display)
     * 
     * @param context Android context
     * @return Acceptance info or null if not accepted
     */
    suspend fun getAcceptanceInfo(context: Context): AcceptanceInfo? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, ACCEPTANCE_FILE)
            if (!file.exists()) return@withContext null
            
            val json = JSONObject(file.readText())
            AcceptanceInfo(
                accepted = json.optBoolean("accepted", false),
                timestamp = json.optLong("timestamp", 0L),
                version = json.optString("version", "unknown"),
                disclaimerVersion = json.optString("disclaimer_version", "unknown")
            )
            
        } catch (e: Exception) {
            Timber.e(e, "Error reading acceptance info")
            null
        }
    }
    
    /**
     * Require acceptance before proceeding
     * 
     * Throws IllegalStateException if not accepted
     * 
     * @param context Android context
     * @param featureName Name of feature being accessed (for error message)
     */
    suspend fun requireAcceptance(context: Context, featureName: String) {
        if (!hasAccepted(context)) {
            throw IllegalStateException(
                "$featureName requires acceptance of Advanced Features Disclaimer. " +
                "User must accept legal/ADVANCED_FEATURES_DISCLAIMER.md first."
            )
        }
    }
    
    /**
     * Get disclaimer text for display
     * 
     * @param context Android context
     * @return Full disclaimer text or error message
     */
    suspend fun getDisclaimerText(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("legal/ADVANCED_FEATURES_DISCLAIMER.md")
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Timber.e(e, "Error reading disclaimer text")
            "Error loading disclaimer. Please check legal/ADVANCED_FEATURES_DISCLAIMER.md"
        }
    }
    
    /**
     * Acceptance information
     */
    data class AcceptanceInfo(
        val accepted: Boolean,
        val timestamp: Long,
        val version: String,
        val disclaimerVersion: String
    )
}
