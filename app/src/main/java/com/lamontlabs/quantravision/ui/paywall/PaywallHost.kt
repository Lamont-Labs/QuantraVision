package com.lamontlabs.quantravision.ui.paywall

import android.app.Activity
import androidx.compose.runtime.*
import com.lamontlabs.quantravision.billing.*

@Composable
fun PaywallHost(activity: Activity, billing: BillingClientManager, purchasedSkus: Set<String>) {
    val ent = entitlementsFor(purchasedSkus)
    Paywall(
        activity = activity,
        entitlements = ent,
        onStandard = { billing.launchPurchase(activity, Sku.STANDARD) },
        onPro = { billing.launchPurchase(activity, Sku.PRO) }
    )
}
