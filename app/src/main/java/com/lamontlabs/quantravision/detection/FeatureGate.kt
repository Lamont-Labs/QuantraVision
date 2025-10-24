package com.lamontlabs.quantravision.detection

import com.lamontlabs.quantravision.billing.Entitlements

/**
 * Assign each pattern to a group:
 *  - "core_half": visible in Standard, and in Free (but Free has 5 highlight quota)
 *  - "pro_only": visible only in Pro
 */
object PatternCatalog {
    // patternId -> group
    val groupById: Map<String, String> = mapOf(
        "head_and_shoulders" to "core_half",
        "inverse_hns" to "core_half",
        "double_top" to "core_half",
        "double_bottom" to "core_half",
        "bull_flag" to "core_half",
        // pro extensions
        "cup_handle" to "pro_only",
        "ascending_triangle" to "pro_only",
        "descending_triangle" to "pro_only",
        "gartley" to "pro_only",
        "butterfly" to "pro_only"
    )
}

fun allowedPatternIds(ent: Entitlements): Set<String> {
    if ("all" in ent.allowedPatternGroups) return PatternCatalog.groupById.keys
    val allowedGroups = ent.allowedPatternGroups
    return PatternCatalog.groupById.filterValues { it in allowedGroups }.keys
}
