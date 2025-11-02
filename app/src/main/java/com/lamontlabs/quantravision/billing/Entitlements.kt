package com.lamontlabs.quantravision.billing

enum class Tier { FREE, STARTER, STANDARD, PRO }

data class Entitlements(
    val tier: Tier = Tier.FREE,
    val canHighlight: Boolean = true,
    val maxTrialHighlights: Int = 5,          // free tier quota
    val allowedPatternGroups: Set<String> = setOf("core_half"), // free shows half but gated by quota
    val extraFeatures: Set<String> = emptySet()
)

// Map Play products -> entitlements
object Sku {
    const val STARTER = "qv_starter_one"    // Play Console product ID (one-time purchase)
    const val STANDARD = "qv_standard_one"  // Play Console product ID (one-time purchase)
    const val PRO = "qv_pro_one"            // Play Console product ID (one-time purchase)
    
    // Upgrade SKUs - allow users to pay only the difference when upgrading
    const val STARTER_TO_STANDARD_UPGRADE = "qv_starter_to_standard_upgrade"  // $15.00
    const val STARTER_TO_PRO_UPGRADE = "qv_starter_to_pro_upgrade"            // $40.00
    const val STANDARD_TO_PRO_UPGRADE = "qv_standard_to_pro_upgrade"          // $25.00
    
    val ALL = setOf(STARTER, STANDARD, PRO, STARTER_TO_STANDARD_UPGRADE, STARTER_TO_PRO_UPGRADE, STANDARD_TO_PRO_UPGRADE)
}

// Derive entitlements from purchased SKUs
fun entitlementsFor(purchasedSkus: Set<String>): Entitlements = when {
    Sku.PRO in purchasedSkus || Sku.STARTER_TO_PRO_UPGRADE in purchasedSkus || Sku.STANDARD_TO_PRO_UPGRADE in purchasedSkus -> Entitlements(
        tier = Tier.PRO,
        canHighlight = true,
        maxTrialHighlights = Int.MAX_VALUE,
        allowedPatternGroups = setOf("all"),  // All 109 patterns
        extraFeatures = setOf("export_csv","multi_watchlist","deep_backtest","intelligence_stack","ai_learning","behavioral_guardrails","proof_capsules")
    )
    Sku.STANDARD in purchasedSkus || Sku.STARTER_TO_STANDARD_UPGRADE in purchasedSkus -> Entitlements(
        tier = Tier.STANDARD,
        canHighlight = true,
        maxTrialHighlights = Int.MAX_VALUE,
        allowedPatternGroups = setOf("standard_tier"),  // 50 patterns
        extraFeatures = setOf("achievements","lessons","book","exports","analytics")
    )
    Sku.STARTER in purchasedSkus -> Entitlements(
        tier = Tier.STARTER,
        canHighlight = true,
        maxTrialHighlights = Int.MAX_VALUE,
        allowedPatternGroups = setOf("starter_tier"),  // 25 patterns
        extraFeatures = setOf("multi_timeframe","basic_analytics")
    )
    else -> Entitlements()  // Free: 10 patterns
}
