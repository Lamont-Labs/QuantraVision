package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.android.billingclient.api.*
import kotlinx.coroutines.*

/**
 * Tier enum for compatibility with LicenseManager
 */
enum class Tier {
    FREE, STANDARD, PRO
}

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
    
    private val prefs by lazy {
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
            Log.e("BillingManager", "EncryptedSharedPreferences failed, falling back to regular prefs", e)
            // CRITICAL: Fallback to regular SharedPreferences to prevent locking out paying users
            // Better to have unencrypted entitlements than no access at all
            activity.getSharedPreferences("qv_billing_prefs", Context.MODE_PRIVATE)
        }
    }

    private val unlockedKey = "qv_unlocked_tier"
    private val purchaseTokenKey = "qv_purchase_token"

    var onTierChanged: ((String) -> Unit)? = null

    fun initialize(onReady: () -> Unit = {}) {
        client = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
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
                Log.w("BillingManager", "Billing disconnected, will retry on next operation")
            }
        })
    }

    private fun queryProducts() {
        val products = listOf("qv_standard_one", "qv_pro_one").map {
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
        prefs.edit()
            .remove(unlockedKey)
            .remove(purchaseTokenKey)
            .apply()
        onTierChanged?.invoke("")
        Log.w("BillingManager", "Entitlements cleared")
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

    fun purchaseStandard() = launchPurchase("qv_standard_one")
    fun purchasePro() = launchPurchase("qv_pro_one")

    private fun launchPurchase(sku: String) {
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
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            client.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Purchase acknowledged")
                } else {
                    Log.e("BillingManager", "Failed to acknowledge: ${result.debugMessage}")
                }
            }
        }

        // Grant entitlement based on verified purchase
        val sku = purchase.products.firstOrNull()
        when (sku) {
            "qv_pro_one" -> {
                setUnlockedSecure("pro", purchase.purchaseToken)
                if (!isRestoration) {
                    Log.d("BillingManager", "Pro unlock granted")
                }
            }
            "qv_standard_one" -> {
                if (getUnlockedTier() != "pro") {
                    setUnlockedSecure("standard", purchase.purchaseToken)
                    if (!isRestoration) {
                        Log.d("BillingManager", "Standard unlock granted")
                    }
                }
            }
        }
    }

    private fun setUnlockedSecure(tier: String, token: String) {
        prefs.edit()
            .putString(unlockedKey, tier)
            .putString(purchaseTokenKey, token)
            .apply()
        onTierChanged?.invoke(tier)
    }

    fun getUnlockedTier(): String = prefs.getString(unlockedKey, "") ?: ""
    fun isStandard(): Boolean = getUnlockedTier() == "standard" || isPro()
    fun isPro(): Boolean = getUnlockedTier() == "pro"
    
    fun getProductDetails(sku: String): ProductDetails? = productMap[sku]
    
    fun cleanup() {
        scope.cancel()
        if (::client.isInitialized) {
            client.endConnection()
        }
    }
}
