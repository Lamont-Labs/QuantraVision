package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BillingClientManager(private val context: Context) : PurchasesUpdatedListener {

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _purchasedSkus = MutableStateFlow<Set<String>>(emptySet())
    val purchasedSkus = _purchasedSkus.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var retryCount = 0
    private val maxRetries = 5

    suspend fun start() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) { queryOwned() }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun launchPurchase(activity: Activity, sku: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            ).build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK || list.isEmpty()) {
                android.util.Log.e("BillingClientManager", "Failed to query product details: ${result.debugMessage}")
                return@queryProductDetailsAsync
            }
            val productDetails = list.first()
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                ).build()
            val launchResult = client.launchBillingFlow(activity, flowParams)
            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                android.util.Log.e("BillingClientManager", "Failed to launch billing flow: ${launchResult.debugMessage}")
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            acknowledgeIfNeeded(purchases)
            _purchasedSkus.value = purchases.flatMap { it.products }.toSet()
        }
    }

    private fun queryOwned() {
        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _purchasedSkus.value = list.flatMap { it.products }.toSet()
                android.util.Log.d("BillingClientManager", "Owned products: ${_purchasedSkus.value}")
            } else {
                android.util.Log.e("BillingClientManager", "Failed to query owned purchases: ${result.debugMessage}")
                scheduleRetry()
            }
        }
    }

    private fun acknowledgeIfNeeded(purchases: List<Purchase>) {
        purchases.filter { !it.isAcknowledged }.forEach {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(it.purchaseToken).build()
            client.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    android.util.Log.e("BillingClientManager", "Failed to acknowledge purchase: ${result.debugMessage}")
                }
            }
        }
    }
    
    private fun scheduleRetry() {
        if (retryCount >= maxRetries) {
            android.util.Log.e("BillingClientManager", "Max retry attempts reached ($maxRetries)")
            retryCount = 0
            return
        }
        
        val delayMs = (1000L * (1 shl retryCount)).coerceAtMost(60000L)
        retryCount++
        
        android.util.Log.w("BillingClientManager", "Scheduling retry #$retryCount in ${delayMs}ms")
        
        scope.launch {
            delay(delayMs)
            queryOwned()
        }
    }
    
    fun cleanup() {
        client.endConnection()
    }
}
