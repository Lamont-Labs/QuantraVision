package com.lamontlabs.quantravision.billing

import com.lamontlabs.quantravision.billing.BillingEntitlementPolicy.OwnedSubscription
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import org.junit.Assert.*
import org.junit.Test

class BillingEntitlementPolicyTest {
    private fun owned(sku: String, purchased: Boolean = true) = OwnedSubscription(listOf(sku), purchased, "play-token")
    @Test fun pendingPurchasesDoNotUnlockFeatures() {
        assertEquals(SubscriptionTier.FREE, BillingEntitlementPolicy.resolveOwned(listOf(owned("qv_apex_monthly", false))).tier)
    }
    @Test fun unknownProductsAndMissingTokensDoNotUnlockFeatures() {
        assertEquals(SubscriptionTier.FREE, BillingEntitlementPolicy.resolveOwned(listOf(owned("unknown"))).tier)
        assertEquals(SubscriptionTier.FREE, BillingEntitlementPolicy.resolveOwned(listOf(owned("qv_apex_monthly").copy(token = ""))).tier)
    }
    @Test fun highestCurrentlyOwnedTierWinsRegardlessOfQueryOrder() {
        val purchases = listOf(owned("qv_basic_monthly"), owned("qv_apex_monthly"), owned("qv_pro_monthly"))
        assertEquals(SubscriptionTier.APEX, BillingEntitlementPolicy.resolveOwned(purchases).tier)
        assertEquals(SubscriptionTier.APEX, BillingEntitlementPolicy.resolveOwned(purchases.reversed()).tier)
    }
    @Test fun aNewOwnershipQueryCanDowngradeApexToBasic() {
        assertEquals(SubscriptionTier.APEX, BillingEntitlementPolicy.resolveOwned(listOf(owned("qv_apex_monthly"))).tier)
        assertEquals(SubscriptionTier.BASIC, BillingEntitlementPolicy.resolveOwned(listOf(owned("qv_basic_monthly"))).tier)
    }
    @Test fun successfulEmptyOwnershipQueryRevokesAccess() {
        assertEquals(SubscriptionTier.FREE, BillingEntitlementPolicy.resolveOwned(emptyList()).tier)
    }
}
