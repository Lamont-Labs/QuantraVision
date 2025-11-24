package com.lamontlabs.quantravision.billing

enum class Tier { FREE, BASIC, PRO, APEX }

data class Entitlements(
    val tier: Tier = Tier.FREE,
    val canHighlight: Boolean = true,
    val maxTrialHighlights: Int = 5,
    val allowedPatternGroups: Set<String> = setOf("core_half"),
    val extraFeatures: Set<String> = emptySet()
)

object Sku {
    const val BASIC_MONTHLY = "qv_basic_monthly"
    const val PRO_MONTHLY = "qv_pro_monthly"
    const val APEX_MONTHLY = "qv_apex_monthly"
    
    val ALL = setOf(BASIC_MONTHLY, PRO_MONTHLY, APEX_MONTHLY)
}

fun entitlementsFor(purchasedSkus: Set<String>): Entitlements {
    return when {
        Sku.APEX_MONTHLY in purchasedSkus -> Entitlements(
            tier = Tier.APEX,
            canHighlight = true,
            maxTrialHighlights = Int.MAX_VALUE,
            allowedPatternGroups = setOf("all"),
            extraFeatures = setOf("export_csv","multi_watchlist","deep_backtest","intelligence_stack","ai_learning","behavioral_guardrails","proof_capsules","batch_mode","advanced_apex")
        )
        Sku.PRO_MONTHLY in purchasedSkus -> Entitlements(
            tier = Tier.PRO,
            canHighlight = true,
            maxTrialHighlights = Int.MAX_VALUE,
            allowedPatternGroups = setOf("pro_tier"),
            extraFeatures = setOf("achievements","lessons","exports","analytics","batch_mode","apex_overlay")
        )
        Sku.BASIC_MONTHLY in purchasedSkus -> Entitlements(
            tier = Tier.BASIC,
            canHighlight = true,
            maxTrialHighlights = Int.MAX_VALUE,
            allowedPatternGroups = setOf("basic_tier"),
            extraFeatures = setOf("multi_timeframe","basic_analytics")
        )
        else -> Entitlements()
    }
}
