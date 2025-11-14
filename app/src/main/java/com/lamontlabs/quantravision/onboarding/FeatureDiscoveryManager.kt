package com.lamontlabs.quantravision.onboarding

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * FeatureDiscoveryStore - Reactive store for tracking newly unlocked feature discovery
 * 
 * Architecture:
 * - Caches encrypted SharedPreferences (created once, not per-call)
 * - Observes EntitlementManager.currentTier reactively
 * - Exposes StateFlow<Set<Feature>> of undiscovered features
 * - All IO operations on Dispatchers.IO
 * - Atomic updates to both persistence and flow state
 * 
 * Usage:
 * ```kotlin
 * // Initialize once at app startup
 * FeatureDiscoveryStore.initialize(applicationContext)
 * 
 * // In composables, observe undiscovered features
 * val undiscoveredFeatures by FeatureDiscoveryStore.undiscoveredFeatures.collectAsState()
 * val isVisible = feature in undiscoveredFeatures && !isDismissed
 * 
 * // Mark feature discovered when user interacts with it
 * scope.launch {
 *     FeatureDiscoveryStore.markFeatureDiscovered(context, feature)
 * }
 * ```
 */
object FeatureDiscoveryStore {
    private const val TAG = "FeatureDiscoveryStore"
    private const val PREFS_NAME = "qv_feature_discovery"
    private const val KEY_LAST_TIER = "last_tier"
    private const val KEY_DISCOVERED_PREFIX = "discovered_"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    @Volatile
    private var cachedPrefs: SharedPreferences? = null
    private val prefsLock = Any()
    
    private val _undiscoveredFeatures = MutableStateFlow<Set<Feature>>(emptySet())
    val undiscoveredFeatures: StateFlow<Set<Feature>> = _undiscoveredFeatures.asStateFlow()
    
    @Volatile
    private var isInitialized = false
    
    /**
     * Get cached SharedPreferences instance (thread-safe, created once)
     */
    private fun getPrefs(context: Context): SharedPreferences? {
        cachedPrefs?.let { return it }
        
        return synchronized(prefsLock) {
            cachedPrefs?.let { return it }
            
            try {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                    
                val prefs = EncryptedSharedPreferences.create(
                    context.applicationContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                
                cachedPrefs = prefs
                Log.d(TAG, "SharedPreferences cached successfully")
                prefs
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize encrypted SharedPreferences", e)
                null
            }
        }
    }
    
    /**
     * Initialize the feature discovery store
     * Must be called once at app startup
     * Observes tier changes and recomputes undiscovered features reactively
     */
    suspend fun initialize(context: Context) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }
        
        isInitialized = true
        Log.d(TAG, "Initializing FeatureDiscoveryStore")
        
        withContext(Dispatchers.IO) {
            val initial = computeUndiscoveredFeatures(context, EntitlementManager.currentTier.value)
            _undiscoveredFeatures.value = initial
            Log.d(TAG, "Initial undiscovered features seeded: ${initial.size}")
        }
        
        EntitlementManager.currentTier.collect { tier ->
            withContext(Dispatchers.IO) {
                _undiscoveredFeatures.value = computeUndiscoveredFeatures(context, tier)
                Log.d(TAG, "Tier changed to ${tier.tierName}, undiscovered features: ${_undiscoveredFeatures.value.size}")
            }
        }
    }
    
    /**
     * Compute undiscovered features for a given tier
     * Runs on IO dispatcher to avoid blocking main thread
     */
    private suspend fun computeUndiscoveredFeatures(
        context: Context,
        tier: SubscriptionTier
    ): Set<Feature> = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        if (prefs == null) {
            Log.w(TAG, "No SharedPreferences available, returning empty set")
            return@withContext emptySet()
        }
        
        Feature.values()
            .filter { feature ->
                feature.requiredTier.ordinal <= tier.ordinal &&
                !prefs.getBoolean("$KEY_DISCOVERED_PREFIX${feature.name}", false)
            }
            .toSet()
    }
    
    /**
     * Mark a feature as discovered
     * Atomically updates both persistence and reactive flow state
     * Runs on IO dispatcher to avoid blocking main thread
     */
    suspend fun markFeatureDiscovered(context: Context, feature: Feature) {
        withContext(Dispatchers.IO) {
            val prefs = getPrefs(context)
            if (prefs == null) {
                Log.w(TAG, "Cannot mark feature discovered: no SharedPreferences")
                return@withContext
            }
            
            prefs.edit().putBoolean("$KEY_DISCOVERED_PREFIX${feature.name}", true).apply()
            
            _undiscoveredFeatures.value = _undiscoveredFeatures.value - feature
            
            Log.d(TAG, "Marked feature discovered: ${feature.name}, remaining: ${_undiscoveredFeatures.value.size}")
        }
    }
    
    /**
     * Check if user has upgraded since last session
     * Returns the new tier if upgraded, null otherwise
     */
    suspend fun checkForUpgrade(context: Context): SubscriptionTier? = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context) ?: return@withContext null
        val currentTier = EntitlementManager.currentTier.value
        val lastTier = prefs.getString(KEY_LAST_TIER, SubscriptionTier.FREE.tierName)
        val lastTierEnum = SubscriptionTier.fromString(lastTier)
        
        prefs.edit().putString(KEY_LAST_TIER, currentTier.tierName).apply()
        
        if (currentTier.ordinal > lastTierEnum.ordinal) {
            Log.d(TAG, "Tier upgrade detected: ${lastTierEnum.tierName} -> ${currentTier.tierName}")
            currentTier
        } else {
            null
        }
    }
    
    /**
     * Get features that were unlocked at a specific tier
     */
    fun getFeaturesForTier(tier: SubscriptionTier): List<Feature> {
        return Feature.values().filter { it.requiredTier == tier }
    }
    
    /**
     * Reset all discovery state (for testing/debugging)
     */
    suspend fun resetDiscoveryState(context: Context) {
        withContext(Dispatchers.IO) {
            val prefs = getPrefs(context)
            if (prefs == null) {
                Log.w(TAG, "Cannot reset discovery state: no SharedPreferences")
                return@withContext
            }
            
            prefs.edit().clear().apply()
            
            val currentTier = EntitlementManager.currentTier.value
            val undiscovered = computeUndiscoveredFeatures(context, currentTier)
            _undiscoveredFeatures.value = undiscovered
            
            Log.d(TAG, "Discovery state reset, undiscovered features: ${undiscovered.size}")
        }
    }
    
    /**
     * LEGACY: Check if banner should show for a feature
     * @deprecated Use undiscoveredFeatures StateFlow instead
     */
    @Deprecated(
        "Use undiscoveredFeatures StateFlow instead",
        ReplaceWith("feature in undiscoveredFeatures.value")
    )
    fun shouldShowDiscoveryBanner(context: Context, feature: Feature): Boolean {
        return feature in _undiscoveredFeatures.value
    }
    
    /**
     * LEGACY: Get list of undiscovered features
     * @deprecated Use undiscoveredFeatures StateFlow instead
     */
    @Deprecated(
        "Use undiscoveredFeatures StateFlow instead",
        ReplaceWith("undiscoveredFeatures.value.toList()")
    )
    fun getUndiscoveredFeatures(context: Context): List<Feature> {
        return _undiscoveredFeatures.value.toList()
    }
}
