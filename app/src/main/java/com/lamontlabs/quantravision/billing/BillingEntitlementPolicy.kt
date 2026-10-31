package com.lamontlabs.quantravision.billing

import com.lamontlabs.quantravision.entitlements.SubscriptionTier

/** Resolves only the current successful Play query; cached tiers cannot prevent a downgrade. */
object BillingEntitlementPolicy {
    data class OwnedSubscription(val products: List<String>, val purchased: Boolean, val token: String)
    data class Selection(val tier: SubscriptionTier, val token: String)
    private val products = mapOf("qv_basic_monthly" to SubscriptionTier.BASIC,
        "qv_pro_monthly" to SubscriptionTier.PRO, "qv_apex_monthly" to SubscriptionTier.APEX)

    fun resolveOwned(purchases: List<OwnedSubscription>): Selection = purchases
        .filter { it.purchased && it.token.isNotBlank() }
        .flatMap { purchase -> purchase.products.mapNotNull { id -> products[id]?.let { Selection(it, purchase.token) } } }
        .sortedWith(compareByDescending<Selection> { it.tier.ordinal }.thenBy { it.token })
        .firstOrNull() ?: Selection(SubscriptionTier.FREE, "")
}
