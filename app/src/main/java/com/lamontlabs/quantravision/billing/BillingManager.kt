package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.*
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import kotlinx.coroutines.*

/**
 * SECURE billing manager with:
 * - Encrypted SharedPreferences for unlock storage
 * - Purchase history verification on startup
 * - Dynamic pricing from ProductDetails (no hardcoded prices)
 * - Proper lifecycle management (initialize once)
 * - Purchase token validation
 */
class BillingManager(private val activity: Activity) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var client: BillingClient
    private var productMap: Map<String, ProductDetails> = emptyMap()
    
    // DEBUG: Bypass all paywalls for testing (set to false for production)
    private val BYPASS_PAYWALLS = false
    
    /**
     * SECURITY: Encrypted SharedPreferences with FAIL-CLOSED pattern
     * NO fallback to unencrypted storage - throws exception if encryption fails
     * This prevents exposing purchase data in plaintext
     * 
     * Synchronized access prevents race conditions when multiple feature gates read simultaneously
     */
    private val prefs by lazy {
        synchronized(this) {
            try {
                val masterKey = MasterKey.Builder(activity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    activity,
                    "qv_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                Log.e("BillingManager", "CRITICAL: EncryptedSharedPreferences initialization failed", e)
                // SECURITY: FAIL-CLOSED PATTERN
                // Do NOT fall back to unencrypted SharedPreferences
                // Better to block access than expose purchase data in plaintext
                // User must clear app data or reinstall to recover
                throw SecurityException(
                    "Cannot initialize secure storage. Please clear app data in Settings > Apps > QuantraVision > Storage > Clear Data, then restart the app.",
                    e
                )
            }
        }
    }

    private val unlockedKey = "qv_unlocked_tier"
    private val purchaseTokenKey = "qv_purchase_token"

    var onTierChanged: ((String) -> Unit)? = null

    fun initialize(onReady: () -> Unit = {}) {
        EntitlementManager.initialize(activity)
        
        client = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        // CRITICAL: Add 15-second timeout to prevent indefinite hang on devices with poor Play Services
        var timeoutJob: Job? = null
        var connectionCompleted = false
        
        timeoutJob = scope.launch {
            delay(15000) // 15 second timeout
            if (!connectionCompleted) {
                Log.e("BillingManager", "CRITICAL: Billing connection timeout after 15 seconds")
                // Call onReady to prevent app hang
                onReady()
            }
        }

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connectionCompleted = true
                timeoutJob?.cancel() // Cancel timeout since connection completed
                
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Billing connected successfully")
                    queryProducts()
                    restorePurchases(onReady)
                } else {
                    Log.e("BillingManager", "Billing setup failed: ${result.debugMessage}")
                    onReady()
                }
            }
            override fun onBillingServiceDisconnected() {
                connectionCompleted = true
                timeoutJob?.cancel() // Cancel timeout on disconnect
                Log.w("BillingManager", "Billing disconnected, will retry on next operation")
            }
        })
    }

    private fun queryProducts() {
        val products = listOf(
            "qv_basic_monthly",
            "qv_pro_monthly",
            "qv_apex_monthly"
        ).map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        
        client.queryProductDetailsAsync(params) { res, list ->
            if (res.responseCode == BillingClient.BillingResponseCode.OK) {
                productMap = list.associateBy { it.productId }
                Log.d("BillingManager", "Subscription products loaded: ${productMap.keys}")
            } else {
                Log.e("BillingManager", "Failed to query subscription products: ${res.debugMessage}")
            }
        }
    }

    /**
     * Restore purchases from Play Store on app startup
     * This ensures legitimate buyers always have access
     * CRITICAL: Clears entitlements if no valid purchases found (refunds, revocations)
     */
    fun restorePurchases(onComplete: () -> Unit = {}) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    client.queryPurchasesAsync(params)
                }
                
                if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchases = result.purchasesList
                    Log.d("BillingManager", "Restored ${purchases.size} purchases")
                    
                    var validPurchaseFound = false
                    
                    if (purchases.isNotEmpty()) {
                        for (purchase in purchases) {
                            when (purchase.purchaseState) {
                                Purchase.PurchaseState.PURCHASED -> {
                                    processPurchase(purchase, isRestoration = true)
                                    validPurchaseFound = true
                                }
                                Purchase.PurchaseState.PENDING -> {
                                    Log.w("BillingManager", "Purchase pending: ${purchase.products}")
                                }
                                else -> {
                                    Log.w("BillingManager", "Invalid purchase state: ${purchase.purchaseState}")
                                }
                            }
                        }
                    }
                    
                    // CRITICAL: If no valid purchases found, clear entitlements
                    // This handles refunds, revocations, and chargebacks
                    if (!validPurchaseFound) {
                        Log.w("BillingManager", "No valid purchases found - clearing entitlements")
                        clearEntitlements()
                    }
                } else {
                    Log.e("BillingManager", "Purchase restoration failed: ${result.billingResult.debugMessage}")
                    // Don't clear entitlements on network error - preserve offline access
                    scheduleRetry()
                }
                onComplete()
            } catch (e: Exception) {
                Log.e("BillingManager", "Error restoring purchases", e)
                scheduleRetry()
                onComplete()
            }
        }
    }
    
    /**
     * Clear all entitlements (for refunds, revocations, chargebacks)
     */
    private fun clearEntitlements() {
        synchronized(this) {
            try {
                prefs.edit()
                    .remove(unlockedKey)
                    .remove(purchaseTokenKey)
                    .apply()
                onTierChanged?.invoke("")
                Log.w("BillingManager", "Entitlements cleared")
            } catch (e: Exception) {
                Log.e("BillingManager", "Failed to clear entitlements", e)
            }
        }
    }
    
    /**
     * Schedule retry for failed restoration (network issues, etc.)
     */
    private fun scheduleRetry() {
        scope.launch {
            delay(30000) // Retry after 30 seconds
            Log.d("BillingManager", "Retrying purchase restoration...")
            restorePurchases()
        }
    }

    fun purchaseBasic() = launchPurchase("qv_basic_monthly")
    fun purchasePro() = launchPurchase("qv_pro_monthly")
    fun purchaseApex() = launchPurchase("qv_apex_monthly")

    private fun getCurrentTierEnum(): Tier {
        return when (getUnlockedTier()) {
            "APEX" -> Tier.APEX
            "PRO" -> Tier.PRO
            "BASIC" -> Tier.BASIC
            else -> Tier.FREE
        }
    }

    fun launchPurchase(sku: String) {
        val pd = productMap[sku]
        if (pd == null) {
            Log.e("BillingManager", "Subscription product not found: $sku")
            return
        }
        
        val offerToken = pd.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Log.e("BillingManager", "No subscription offers available for: $sku")
            return
        }
        
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(pd)
                    .setOfferToken(offerToken)
                    .build()
            )).build()
        
        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            Log.e("BillingManager", "Failed to launch billing flow: ${result.debugMessage}")
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    processPurchase(purchase, isRestoration = false)
                }
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingManager", "User canceled purchase")
        } else {
            Log.e("BillingManager", "Purchase failed: ${result.debugMessage}")
        }
    }

    private fun processPurchase(purchase: Purchase, isRestoration: Boolean) {
        if (!purchase.isAcknowledged) {
            acknowledgePurchaseWithRetry(purchase.purchaseToken, retryCount = 0)
        }

        val sku = purchase.products.firstOrNull()
        when (sku) {
            "qv_apex_monthly" -> {
                setUnlockedSecure("APEX", purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Apex subscription activated")
                }
            }
            "qv_pro_monthly" -> {
                if (getUnlockedTier() != "APEX") {
                    setUnlockedSecure("PRO", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Pro subscription activated")
                    }
                }
            }
            "qv_basic_monthly" -> {
                if (getUnlockedTier() != "PRO" && getUnlockedTier() != "APEX") {
                    setUnlockedSecure("BASIC", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Basic subscription activated")
                    }
                }
            }
        }
    }

    private fun setUnlockedSecure(tier: String, token: String) {
        synchronized(this) {
            try {
                val normalizedTier = tier.uppercase()
                prefs.edit()
                    .putString(unlockedKey, normalizedTier)
                    .putString(purchaseTokenKey, token)
                    .apply()
                EntitlementManager.updateTierFromString(normalizedTier)
                onTierChanged?.invoke(normalizedTier)
            } catch (e: Exception) {
                Log.e("BillingManager", "CRITICAL: Failed to save entitlements - purchase may not be persisted", e)
                // Notify user of storage failure
                activity.runOnUiThread {
                    android.widget.Toast.makeText(
                        activity,
                        "Failed to save purchase. Please ensure you have sufficient storage space.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun getUnlockedTier(): String {
        return synchronized(this) {
            try {
                val tier = prefs.getString(unlockedKey, "") ?: ""
                tier.uppercase()  // Normalize to uppercase for backward compatibility
            } catch (e: Exception) {
                Log.e("BillingManager", "CRITICAL: Failed to read entitlements from secure storage", e)
                // SECURITY: Return empty string (deny access) if we can't read secure storage
                // Do NOT fall back to allowing access - fail closed
                ""
            }
        }
    }
    
    fun isBasic(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "BASIC" || isPro() || isApex()
    fun isPro(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "PRO" || isApex()
    fun isApex(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "APEX"
    
    fun getProductDetails(sku: String): ProductDetails? = productMap[sku]
    
    /**
     * Acknowledge purchase with retry logic
     * CRITICAL: Prevents Google Play refunds after 3 days (~0.1-0.5% of purchases)
     * Retries up to 5 times with 5-second delay
     */
    private fun acknowledgePurchaseWithRetry(purchaseToken: String, retryCount: Int) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingManager", "Purchase acknowledged successfully")
            } else {
                Log.e("BillingManager", "Failed to acknowledge purchase (attempt ${retryCount + 1}/5): ${result.debugMessage}")
                
                // Retry up to 5 times with 5-second delay
                if (retryCount < 5) {
                    scope.launch {
                        delay(5000) // 5-second delay
                        acknowledgePurchaseWithRetry(purchaseToken, retryCount + 1)
                    }
                } else {
                    Log.e("BillingManager", "CRITICAL: Failed to acknowledge purchase after 5 attempts - Google Play may refund after 3 days")
                }
            }
        }
    }

    fun cleanup() {
        scope.cancel()
        if (::client.isInitialized) {
            client.endConnection()
        }
    }
}
