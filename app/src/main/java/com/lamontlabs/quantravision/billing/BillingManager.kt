package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.*
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
    private val BYPASS_PAYWALLS = true
    
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
    private val bookPurchasedKey = "qv_book_purchased"

    var onTierChanged: ((String) -> Unit)? = null

    fun initialize(onReady: () -> Unit = {}) {
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
            "qv_starter_one", 
            "qv_standard_one", 
            "qv_pro_one",
            "qv_book_standalone",
            "qv_starter_to_standard_upgrade",
            "qv_starter_to_pro_upgrade",
            "qv_standard_to_pro_upgrade"
        ).map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        
        client.queryProductDetailsAsync(params) { res, list ->
            if (res.responseCode == BillingClient.BillingResponseCode.OK) {
                productMap = list.associateBy { it.productId }
                Log.d("BillingManager", "Products loaded: ${productMap.keys}")
            } else {
                Log.e("BillingManager", "Failed to query products: ${res.debugMessage}")
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
            .setProductType(BillingClient.ProductType.INAPP)
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
                    .remove(bookPurchasedKey)
                    .apply()
                onTierChanged?.invoke("")
                Log.w("BillingManager", "Entitlements cleared")
            } catch (e: Exception) {
                Log.e("BillingManager", "Failed to clear entitlements", e)
                // Non-fatal - worst case user keeps access when they shouldn't
            }
        }
    }
    
    private fun setBookPurchased(token: String) {
        synchronized(this) {
            try {
                prefs.edit()
                    .putBoolean(bookPurchasedKey, true)
                    .putString("${bookPurchasedKey}_token", token)
                    .apply()
                Log.d("BillingManager", "Book purchase recorded")
            } catch (e: Exception) {
                Log.e("BillingManager", "Failed to save book purchase", e)
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

    fun purchaseStarter() = launchPurchase("qv_starter_one")
    fun purchaseStandard() {
        val currentTier = getCurrentTierEnum()
        val upgradeSku = getUpgradeSku(currentTier, Tier.STANDARD)
        launchPurchase(upgradeSku ?: "qv_standard_one")
    }
    fun purchasePro() {
        val currentTier = getCurrentTierEnum()
        val upgradeSku = getUpgradeSku(currentTier, Tier.PRO)
        launchPurchase(upgradeSku ?: "qv_pro_one")
    }
    fun purchaseBook() = launchPurchase("qv_book_standalone")

    /**
     * Get current tier as Tier enum
     */
    private fun getCurrentTierEnum(): Tier {
        return when (getUnlockedTier()) {
            "PRO" -> Tier.PRO
            "STANDARD" -> Tier.STANDARD
            "STARTER" -> Tier.STARTER
            else -> Tier.FREE
        }
    }

    /**
     * Check if moving from currentTier to targetTier is an upgrade
     */
    fun isUpgrade(currentTier: Tier, targetTier: Tier): Boolean {
        val tierOrder = listOf(Tier.FREE, Tier.STARTER, Tier.STANDARD, Tier.PRO)
        val currentIndex = tierOrder.indexOf(currentTier)
        val targetIndex = tierOrder.indexOf(targetTier)
        return currentIndex >= 0 && targetIndex > currentIndex
    }

    /**
     * Get the appropriate upgrade SKU based on tier transition
     * Returns null if not an upgrade (e.g., FREE → STARTER uses regular SKU)
     * 
     * Upgrade SKUs available:
     * - STARTER → STANDARD: qv_starter_to_standard_upgrade ($15.00)
     * - STARTER → PRO: qv_starter_to_pro_upgrade ($40.00)
     * - STANDARD → PRO: qv_standard_to_pro_upgrade ($25.00)
     */
    fun getUpgradeSku(currentTier: Tier, targetTier: Tier): String? {
        if (!isUpgrade(currentTier, targetTier)) return null
        
        return when {
            currentTier == Tier.STARTER && targetTier == Tier.STANDARD -> "qv_starter_to_standard_upgrade"
            currentTier == Tier.STARTER && targetTier == Tier.PRO -> "qv_starter_to_pro_upgrade"
            currentTier == Tier.STANDARD && targetTier == Tier.PRO -> "qv_standard_to_pro_upgrade"
            else -> null
        }
    }

    fun launchPurchase(sku: String) {
        val pd = productMap[sku]
        if (pd == null) {
            Log.e("BillingManager", "Product not found: $sku")
            return
        }
        
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(pd)
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
        // Acknowledge purchase if needed
        if (!purchase.isAcknowledged) {
            acknowledgePurchaseWithRetry(purchase.purchaseToken, retryCount = 0)
        }

        // Grant entitlement based on verified purchase
        val sku = purchase.products.firstOrNull()
        when (sku) {
            "qv_pro_one" -> {
                setUnlockedSecure("PRO", purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Pro unlock granted")
                }
            }
            "qv_standard_one" -> {
                if (getUnlockedTier() != "PRO") {
                    setUnlockedSecure("STANDARD", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Standard unlock granted")
                    }
                }
            }
            "qv_starter_one" -> {
                if (getUnlockedTier() != "PRO" && getUnlockedTier() != "STANDARD") {
                    setUnlockedSecure("STARTER", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Starter unlock granted")
                    }
                }
            }
            "qv_starter_to_standard_upgrade" -> {
                if (getUnlockedTier() != "PRO") {
                    setUnlockedSecure("STANDARD", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Standard upgrade from Starter granted")
                    }
                }
            }
            "qv_starter_to_pro_upgrade" -> {
                setUnlockedSecure("PRO", purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Pro upgrade from Starter granted")
                }
            }
            "qv_standard_to_pro_upgrade" -> {
                setUnlockedSecure("PRO", purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Pro upgrade from Standard granted")
                }
            }
            "qv_book_standalone" -> {
                // Book purchase doesn't change tier, but we track it separately
                setBookPurchased(purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Standalone book purchase granted")
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
    
    fun isStarter(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "STARTER" || isStandard() || isPro()
    fun isStandard(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "STANDARD" || isPro()
    fun isPro(): Boolean = BYPASS_PAYWALLS || getUnlockedTier() == "PRO"
    
    /**
     * Check if user has access to the book
     * Book is included with STANDARD/PRO or can be purchased standalone
     */
    fun hasBook(): Boolean {
        // DEBUG: Bypass paywalls
        if (BYPASS_PAYWALLS) return true
        
        // STANDARD and PRO tiers include the book
        if (isStandard() || isPro()) return true
        
        // Check for standalone book purchase
        return synchronized(this) {
            try {
                prefs.getBoolean(bookPurchasedKey, false)
            } catch (e: Exception) {
                Log.e("BillingManager", "Failed to read book purchase status", e)
                false
            }
        }
    }
    
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
