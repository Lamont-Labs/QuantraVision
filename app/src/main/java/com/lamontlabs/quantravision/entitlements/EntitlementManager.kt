package com.lamontlabs.quantravision.entitlements

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SubscriptionTier(val tierName: String, val displayName: String) {
    FREE("free", "Free"),
    STARTER("starter", "Starter - $9.99"),
    STANDARD("standard", "Standard - $24.99"),
    PRO("pro", "Pro - $49.99");
    
    companion object {
        fun fromString(tier: String?): SubscriptionTier {
            return when (tier?.lowercase()) {
                "starter" -> STARTER
                "standard" -> STANDARD
                "pro" -> PRO
                else -> FREE
            }
        }
    }
}

enum class Feature(val requiredTier: SubscriptionTier, val displayName: String) {
    BASIC_PATTERNS(SubscriptionTier.FREE, "10 Basic Patterns"),
    STARTER_PATTERNS(SubscriptionTier.STARTER, "25 Core Patterns"),
    STANDARD_PATTERNS(SubscriptionTier.STANDARD, "50 Advanced Patterns"),
    ALL_PATTERNS(SubscriptionTier.PRO, "All 109 Patterns"),
    
    LIMITED_HIGHLIGHTS(SubscriptionTier.FREE, "3 Highlights/Day"),
    UNLIMITED_HIGHLIGHTS(SubscriptionTier.STARTER, "Unlimited Highlights"),
    
    REGIME_NAVIGATOR(SubscriptionTier.STANDARD, "Regime Navigator"),
    PATTERN_TO_PLAN(SubscriptionTier.PRO, "Pattern-to-Plan Engine"),
    BEHAVIORAL_GUARDRAILS(SubscriptionTier.STANDARD, "Behavioral Guardrails"),
    PROOF_CAPSULES(SubscriptionTier.PRO, "Proof Capsules"),
    
    BASIC_OVERLAY(SubscriptionTier.FREE, "Basic Pattern Overlay"),
    TRADE_SCENARIOS(SubscriptionTier.PRO, "Trade Scenario Overlay"),
    
    SCAN_LEARNING(SubscriptionTier.PRO, "AI Scan Learning Engine"),
    
    VISUAL_ALERTS(SubscriptionTier.FREE, "Visual Alerts"),
    HAPTIC_ALERTS(SubscriptionTier.STARTER, "Haptic Alerts"),
    VOICE_ALERTS(SubscriptionTier.PRO, "Voice Alerts"),
    
    BASIC_EDUCATION(SubscriptionTier.FREE, "Basic Lessons"),
    TRADING_BOOK(SubscriptionTier.STANDARD, "Trading Book Access"),
    ADVANCED_EDUCATION(SubscriptionTier.PRO, "Advanced Interactive Lessons")
}

object EntitlementManager {
    private const val TAG = "EntitlementManager"
    private const val PREFS_NAME = "qv_secure_prefs"
    private const val UNLOCKED_TIER_KEY = "qv_unlocked_tier"
    
    private const val ENABLE_TIER_OVERRIDE = false
    
    private val lock = Any()
    
    private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
    val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()
    
    private var isInitialized = false
    
    fun initialize(context: Context) {
        synchronized(lock) {
            if (isInitialized) {
                Log.d(TAG, "Already initialized, skipping")
                return
            }
            
            val tier = readTierFromStorage(context)
            _currentTier.value = tier
            isInitialized = true
            
            Log.d(TAG, "Initialized with tier: ${tier.tierName}")
        }
    }
    
    private fun getSecurePrefs(context: Context): android.content.SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize encrypted SharedPreferences", e)
            null
        }
    }
    
    private fun readTierFromStorage(context: Context): SubscriptionTier {
        return synchronized(lock) {
            try {
                val prefs = getSecurePrefs(context)
                if (prefs == null) {
                    Log.w(TAG, "Failed to access secure storage, defaulting to FREE tier")
                    return SubscriptionTier.FREE
                }
                
                val tierString = prefs.getString(UNLOCKED_TIER_KEY, "") ?: ""
                val tier = SubscriptionTier.fromString(tierString)
                
                Log.d(TAG, "Read tier from storage: $tierString -> ${tier.tierName}")
                tier
            } catch (e: Exception) {
                Log.e(TAG, "Error reading tier from storage, defaulting to FREE", e)
                SubscriptionTier.FREE
            }
        }
    }
    
    fun updateTier(newTier: SubscriptionTier) {
        synchronized(lock) {
            val oldTier = _currentTier.value
            if (oldTier != newTier) {
                _currentTier.value = newTier
                Log.d(TAG, "Tier updated: ${oldTier.tierName} -> ${newTier.tierName}")
            }
        }
    }
    
    fun updateTierFromString(tierString: String) {
        val tier = SubscriptionTier.fromString(tierString)
        updateTier(tier)
    }
    
    fun hasFeatureAccess(feature: Feature): Boolean {
        return _currentTier.value.ordinal >= feature.requiredTier.ordinal
    }
    
    fun hasFeatureAccess(requiredTier: SubscriptionTier): Boolean {
        return _currentTier.value.ordinal >= requiredTier.ordinal
    }
    
    fun getAvailableFeatures(): List<Feature> {
        return Feature.values().filter { hasFeatureAccess(it) }
    }
    
    fun getLockedFeatures(): List<Feature> {
        return Feature.values().filter { !hasFeatureAccess(it) }
    }
    
    fun setTestTier(tier: SubscriptionTier) {
        if (ENABLE_TIER_OVERRIDE) {
            Log.w(TAG, "TEST MODE: Setting tier to ${tier.tierName}")
            _currentTier.value = tier
        } else {
            Log.w(TAG, "Test tier override is disabled. Set ENABLE_TIER_OVERRIDE = true to enable.")
        }
    }
    
    fun getCurrentTierName(): String {
        return _currentTier.value.tierName
    }
    
    fun getCurrentTierDisplay(): String {
        return _currentTier.value.displayName
    }
    
    fun isFree(): Boolean = _currentTier.value == SubscriptionTier.FREE
    fun isStarter(): Boolean = _currentTier.value.ordinal >= SubscriptionTier.STARTER.ordinal
    fun isStandard(): Boolean = _currentTier.value.ordinal >= SubscriptionTier.STANDARD.ordinal
    fun isPro(): Boolean = _currentTier.value == SubscriptionTier.PRO
}
