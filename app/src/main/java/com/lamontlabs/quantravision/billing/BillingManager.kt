package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

class BillingManager(
    private val context: Context,
    private val onPurchaseUpdated: (Boolean) -> Unit
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    println("Billing: Connected")
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    fun launchPurchase(activity: Activity, skuId: String) {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(listOf(skuId))
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient.querySkuDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && !details.isNullOrEmpty()) {
                val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(details[0])
                    .build()
                billingClient.launchBillingFlow(activity, flowParams)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val success = result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null
        onPurchaseUpdated(success)
    }

    fun getEntitlement(context: Context): Tier {
        val prefs = context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)
        return Tier.valueOf(prefs.getString("tier", "FREE")!!)
    }

    fun setEntitlement(context: Context, tier: Tier) {
        context.getSharedPreferences("quantravision_prefs", Context.MODE_PRIVATE)
            .edit().putString("tier", tier.name).apply()
    }

    enum class Tier { FREE, STANDARD, PRO }
}
