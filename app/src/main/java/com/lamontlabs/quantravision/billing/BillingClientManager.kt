package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingClientManager(private val context: Context) : PurchasesUpdatedListener {

    private val client = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _purchasedSkus = MutableStateFlow<Set<String>>(emptySet())
    val purchasedSkus = _purchasedSkus.asStateFlow()

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
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()
        client.queryProductDetailsAsync(params) { result, list ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK || list.isEmpty()) return@queryProductDetailsAsync
            val productDetails = list.first()
            val offer = productDetails.subscriptionOfferDetails?.firstOrNull() ?: return@queryProductDetailsAsync
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offer.offerToken)
                            .build()
                    )
                ).build()
            client.launchBillingFlow(activity, flowParams)
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
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { _, list ->
            _purchasedSkus.value = list.flatMap { it.products }.toSet()
        }
    }

    private fun acknowledgeIfNeeded(purchases: List<Purchase>) {
        purchases.filter { !it.isAcknowledged }.forEach {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(it.purchaseToken).build()
            client.acknowledgePurchase(params) { }
        }
    }
}
