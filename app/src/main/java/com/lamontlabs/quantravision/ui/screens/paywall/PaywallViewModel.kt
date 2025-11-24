package com.lamontlabs.quantravision.ui.screens.paywall

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lamontlabs.quantravision.billing.BillingManager
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * PaywallViewModel - State management for the professional paywall screen
 * 
 * Handles tier options, purchase flow, and EntitlementManager integration.
 * Integrates with BillingManager for Google Play Billing transactions.
 * 
 * LIFECYCLE-SAFE: Stores Application context to prevent Activity leaks.
 * BillingManager is created fresh for each purchase with current Activity.
 */
class PaywallViewModel(context: Context) : ViewModel() {
    
    private val TAG = "PaywallViewModel"
    
    // Store Application context, not Activity context (prevents memory leaks)
    private val appContext = context.applicationContext
    
    /**
     * UI State for the paywall screen
     * 
     * @param currentTier User's current subscription tier
     * @param selectedTier Currently selected tier in the UI (for highlighting)
     * @param isPurchasing Whether a purchase is in progress
     * @param purchaseError Error message if purchase failed
     * @param purchaseSuccess Success message if purchase completed
     * @param tiers List of all tier options to display
     */
    data class UiState(
        val currentTier: SubscriptionTier = SubscriptionTier.FREE,
        val selectedTier: SubscriptionTier? = null,
        val isPurchasing: Boolean = false,
        val purchaseError: String? = null,
        val purchaseSuccess: String? = null,
        val tiers: List<TierOption> = emptyList()
    )
    
    /**
     * Tier option display data
     * 
     * @param tier The subscription tier
     * @param price Display price (accounts for upgrades)
     * @param originalPrice Original price before upgrade discount (null if not upgrade)
     * @param features List of feature descriptions
     * @param isCurrent Whether this is the user's current tier
     * @param isRecommended Whether to show "RECOMMENDED" badge
     * @param isUpgrade Whether this is an upgrade from current tier
     */
    data class TierOption(
        val tier: SubscriptionTier,
        val price: String,
        val originalPrice: String? = null,
        val features: List<String>,
        val isCurrent: Boolean,
        val isRecommended: Boolean,
        val isUpgrade: Boolean = false
    )
    
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private var billingManager: BillingManager? = null
    
    init {
        loadTierOptions()
        observeTierChanges()
    }
    
    /**
     * Load tier options with pricing and feature lists
     * Accounts for upgrade pricing based on current tier
     */
    private fun loadTierOptions() {
        viewModelScope.launch {
            val currentTier = EntitlementManager.currentTier.value
            
            val tiers = listOf(
                TierOption(
                    tier = SubscriptionTier.FREE,
                    price = "Free",
                    features = listOf(
                        "10 Basic Patterns",
                        "3 Scans/Day",
                        "1 AI Explanation/Day",
                        "Basic Overlay",
                        "Visual Alerts"
                    ),
                    isCurrent = currentTier == SubscriptionTier.FREE,
                    isRecommended = false
                ),
                TierOption(
                    tier = SubscriptionTier.BASIC,
                    price = "$4.99/mo",
                    features = listOf(
                        "25 Core Patterns",
                        "25 Scans/Day",
                        "5 AI Explanations/Day",
                        "5 Saved Summaries",
                        "Core Overlay",
                        "Haptic Alerts"
                    ),
                    isCurrent = currentTier == SubscriptionTier.BASIC,
                    isRecommended = true
                ),
                TierOption(
                    tier = SubscriptionTier.PRO,
                    price = "$14.99/mo",
                    features = listOf(
                        "50 Advanced Patterns",
                        "75 Scans/Day",
                        "20 AI Explanations/Day",
                        "20 Saved Summaries",
                        "Batch Mode",
                        "Full Apex Overlay",
                        "Regime Navigator",
                        "Voice Alerts"
                    ),
                    isCurrent = currentTier == SubscriptionTier.PRO,
                    isRecommended = false
                ),
                TierOption(
                    tier = SubscriptionTier.APEX,
                    price = "$29.99/mo",
                    features = listOf(
                        "All 109 Patterns",
                        "200 Scans/Day",
                        "60 AI Explanations/Day",
                        "100 Saved Summaries",
                        "Advanced Logic",
                        "AI Scan Learning",
                        "Trade Scenario Overlay",
                        "Pattern-to-Plan Engine"
                    ),
                    isCurrent = currentTier == SubscriptionTier.APEX,
                    isRecommended = false
                )
            )
            
            _uiState.update { it.copy(tiers = tiers, currentTier = currentTier) }
        }
    }
    
    /**
     * Observe EntitlementManager for tier changes
     * Refreshes tier options when tier changes
     */
    private fun observeTierChanges() {
        viewModelScope.launch {
            EntitlementManager.currentTier.collect { tier ->
                _uiState.update { it.copy(currentTier = tier) }
                loadTierOptions()
            }
        }
    }
    
    /**
     * Select a tier in the UI (for highlighting)
     */
    fun selectTier(tier: SubscriptionTier) {
        _uiState.update { it.copy(selectedTier = tier) }
    }
    
    /**
     * Purchase a tier via BillingManager
     * 
     * LIFECYCLE-SAFE: Creates BillingManager fresh for each purchase with current Activity.
     * This ensures we never hold a stale Activity reference after rotation.
     * 
     * @param activity Activity context for billing flow (current Activity, not stored)
     * @param tier Tier to purchase
     */
    fun purchaseTier(activity: Activity, tier: SubscriptionTier) {
        val currentTier = _uiState.value.currentTier
        
        if (tier == currentTier) {
            _uiState.update { it.copy(purchaseError = "You already own this tier") }
            return
        }
        
        if (tier.ordinal < currentTier.ordinal) {
            _uiState.update { it.copy(purchaseError = "Cannot downgrade to a lower tier") }
            return
        }
        
        if (tier == SubscriptionTier.FREE) {
            _uiState.update { it.copy(
                isPurchasing = false,
                purchaseError = "Cannot purchase FREE tier"
            )}
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isPurchasing = true, purchaseError = null, purchaseSuccess = null) }
                
                // Create BillingManager with current Activity (not stored - prevents leaks)
                val billing = BillingManager(activity)
                billingManager = billing
                
                // Set up tier change callback
                billing.onTierChanged = { tierString ->
                    Log.d(TAG, "Tier changed via BillingManager: $tierString")
                    EntitlementManager.updateTierFromString(tierString)
                    _uiState.update { it.copy(
                        isPurchasing = false,
                        purchaseSuccess = "Purchase successful! Unlocked $tierString tier."
                    )}
                    loadTierOptions()
                }
                
                // Initialize and launch purchase flow
                billing.initialize {
                    try {
                        when (tier) {
                            SubscriptionTier.BASIC -> billing.purchaseBasic()
                            SubscriptionTier.PRO -> billing.purchasePro()
                            SubscriptionTier.APEX -> billing.purchaseApex()
                            else -> {
                                _uiState.update { it.copy(
                                    isPurchasing = false,
                                    purchaseError = "Cannot purchase FREE tier"
                                )}
                            }
                        }
                        Log.d(TAG, "Purchase flow initiated for tier: ${tier.tierName}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Purchase failed", e)
                        _uiState.update { it.copy(
                            isPurchasing = false,
                            purchaseError = "Purchase failed: ${e.message}"
                        )}
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Purchase initialization failed", e)
                _uiState.update { it.copy(
                    isPurchasing = false,
                    purchaseError = "Purchase initialization failed: ${e.message}"
                )}
            }
        }
    }
    
    /**
     * Clear error messages
     */
    fun clearError() {
        _uiState.update { it.copy(purchaseError = null) }
    }
    
    /**
     * Clear success messages
     */
    fun clearSuccess() {
        _uiState.update { it.copy(purchaseSuccess = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up BillingManager resources
        billingManager?.onTierChanged = null
        billingManager?.cleanup()
        billingManager = null
        Log.d(TAG, "PaywallViewModel cleared")
    }
}

/**
 * ViewModelFactory for PaywallViewModel
 * 
 * Ensures Application context is passed to ViewModel, not Activity context.
 * This prevents memory leaks across configuration changes.
 */
class PaywallViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaywallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PaywallViewModel(context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
