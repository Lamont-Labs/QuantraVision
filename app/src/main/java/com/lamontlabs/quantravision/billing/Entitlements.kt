package com.lamontlabs.quantravision.billing

enum class Tier { FREE, STANDARD, PRO }

data class Entitlements(
    val tier: Tier = Tier.FREE,
    val canHighlight: Boolean = true,
    val maxTrialHighlights: Int = 5,          // free tier quota
    val allowedPatternGroups: Set<String> = setOf("core_half"), // free shows half but gated by quota
    val extraFeatures: Set<String> = emptySet()
)

// Map Play products -> entitlements
object Sku {
    const val STANDARD = "qv_standard_one"  // Play Console product ID (one-time purchase)
    const val PRO = "qv_pro_one"            // Play Console product ID (one-time purchase)
    val ALL = setOf(STANDARD, PRO)
}

// Derive entitlements from purchased SKUs
fun entitlementsFor(purchasedSkus: Set<String>): Entitlements = when {
    Sku.PRO in purchasedSkus -> Entitlements(
        tier = Tier.PRO,
        canHighlight = true,
        maxTrialHighlights = Int.MAX_VALUE,
        allowedPatternGroups = setOf("all"),
        extraFeatures = setOf("export_csv","multi_watchlist","deep_backtest")
    )
    Sku.STANDARD in purchasedSkus -> Entitlements(
        tier = Tier.STANDARD,
        canHighlight = true,
        maxTrialHighlights = Int.MAX_VALUE,
        allowedPatternGroups = setOf("core_half"), // half of library
        extraFeatures = emptySet()
    )
    else -> Entitlements()
}
