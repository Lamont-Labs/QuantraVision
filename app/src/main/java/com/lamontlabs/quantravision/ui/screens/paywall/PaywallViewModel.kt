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
                        "3 Highlights/Day",
                        "Basic Overlay",
                        "Visual Alerts"
                    ),
                    isCurrent = currentTier == SubscriptionTier.FREE,
                    isRecommended = false
                ),
                TierOption(
                    tier = SubscriptionTier.STARTER,
                    price = "$9.99",
                    features = listOf(
                        "25 Core Patterns",
                        "Unlimited Highlights",
                        "Haptic Alerts",
                        "All FREE features"
                    ),
                    isCurrent = currentTier == SubscriptionTier.STARTER,
                    isRecommended = true
                ),
                TierOption(
                    tier = SubscriptionTier.STANDARD,
                    price = if (currentTier == SubscriptionTier.STARTER) "$15.00" else "$24.99",
                    originalPrice = if (currentTier == SubscriptionTier.STARTER) "$24.99" else null,
                    features = listOf(
                        "50 Advanced Patterns",
                        "Regime Navigator",
                        "Behavioral Guardrails",
                        "Trading Book Access",
                        "All STARTER features"
                    ),
                    isCurrent = currentTier == SubscriptionTier.STANDARD,
                    isRecommended = false,
                    isUpgrade = currentTier == SubscriptionTier.STARTER
                ),
                TierOption(
                    tier = SubscriptionTier.PRO,
                    price = when (currentTier) {
                        SubscriptionTier.STARTER -> "$40.00"
                        SubscriptionTier.STANDARD -> "$25.00"
                        else -> "$49.99"
                    },
                    originalPrice = if (currentTier == SubscriptionTier.STARTER || currentTier == SubscriptionTier.STANDARD) "$49.99" else null,
                    features = listOf(
                        "All 109 Patterns",
                        "Pattern-to-Plan Engine",
                        "AI Scan Learning",
                        "Voice Alerts",
                        "Proof Capsules",
                        "Trade Scenario Overlay",
                        "All STANDARD features"
                    ),
                    isCurrent = currentTier == SubscriptionTier.PRO,
                    isRecommended = false,
                    isUpgrade = currentTier == SubscriptionTier.STARTER || currentTier == SubscriptionTier.STANDARD
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
                            SubscriptionTier.STARTER -> billing.purchaseStarter()
                            SubscriptionTier.STANDARD -> billing.purchaseStandard()
                            SubscriptionTier.PRO -> billing.purchasePro()
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
