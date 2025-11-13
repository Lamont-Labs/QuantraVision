package com.lamontlabs.quantravision.ui.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.*

/**
 * PaywallScreen wrapper composable for navigation compatibility
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onBook: (() -> Unit)? = null,
    onStandard: (() -> Unit)? = null,
    onPro: (() -> Unit)? = null
) {
    val activity = LocalContext.current as? Activity
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upgrade") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (activity != null) {
                val billingManager = remember { BillingManager(activity) }
                val tierString = remember { billingManager.getUnlockedTier() }
                val hasBook = remember { billingManager.hasBook() }
                val tier = when (tierString) {
                    "PRO" -> Tier.PRO
                    "STANDARD" -> Tier.STANDARD
                    "STARTER" -> Tier.STARTER
                    else -> Tier.FREE
                }
                val entitlements = remember(tier, hasBook) {
                    when (tier) {
                        Tier.PRO -> Entitlements(tier = Tier.PRO, canHighlight = true, maxTrialHighlights = Int.MAX_VALUE, allowedPatternGroups = setOf("all"), extraFeatures = setOf("export_csv","multi_watchlist","deep_backtest","intelligence_stack","ai_learning","behavioral_guardrails","proof_capsules"), hasBook = hasBook)
                        Tier.STANDARD -> Entitlements(tier = Tier.STANDARD, canHighlight = true, maxTrialHighlights = Int.MAX_VALUE, allowedPatternGroups = setOf("standard_tier"), extraFeatures = setOf("achievements","lessons","book","exports","analytics"), hasBook = hasBook)
                        Tier.STARTER -> Entitlements(tier = Tier.STARTER, canHighlight = true, maxTrialHighlights = Int.MAX_VALUE, allowedPatternGroups = setOf("starter_tier"), extraFeatures = setOf("multi_timeframe","basic_analytics"), hasBook = hasBook)
                        else -> Entitlements(hasBook = hasBook)
                    }
                }
                
                // Initialize billing connection when screen loads
                LaunchedEffect(billingManager) {
                    billingManager.initialize()
                }
                
                Paywall(
                    activity = activity,
                    billingManager = billingManager,
                    entitlements = entitlements,
                    onStarter = onStandard ?: {},
                    onStandard = onStandard ?: {},
                    onPro = onPro ?: {},
                    onBook = onBook,
                    hasBook = hasBook
                )
            } else {
                Text(
                    "Error: Activity context required",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun Paywall(
    activity: Activity,
    billingManager: BillingManager,
    entitlements: Entitlements,
    onStarter: () -> Unit,
    onStandard: () -> Unit,
    onPro: () -> Unit,
    onBook: (() -> Unit)? = null,
    hasBook: Boolean = false
) {
    val currentTier = entitlements.tier
    
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Unlock QuantraVision",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Choose the perfect plan for your trading journey",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(12.dp))

        PaywallTierCard(
            title = "STARTER",
            price = "$9.99",
            badge = null,
            features = listOf(
                "25 patterns",
                "Multi-timeframe support",
                "Basic analytics"
            ),
            isCurrentTier = currentTier == Tier.STARTER,
            isLowerTier = currentTier.ordinal > Tier.STARTER.ordinal,
            isUpgrade = false,
            onClick = {
                billingManager.purchaseStarter()
                onStarter()
            }
        )

        PaywallTierCard(
            title = "STANDARD",
            price = if (currentTier == Tier.STARTER) "$15.00" else "$24.99",
            originalPrice = if (currentTier == Tier.STARTER) "$24.99" else null,
            badge = "MOST POPULAR",
            features = listOf(
                "50 patterns",
                "Full analytics dashboard",
                "50 achievements + 25 lessons",
                "Trading book + exports"
            ),
            isCurrentTier = currentTier == Tier.STANDARD,
            isLowerTier = currentTier.ordinal > Tier.STANDARD.ordinal,
            isUpgrade = currentTier == Tier.STARTER,
            onClick = {
                billingManager.purchaseStandard()
                onStandard()
            }
        )

        PaywallTierCard(
            title = "PRO",
            price = when (currentTier) {
                Tier.STARTER -> "$40.00"
                Tier.STANDARD -> "$25.00"
                else -> "$49.99"
            },
            originalPrice = if (currentTier == Tier.STARTER || currentTier == Tier.STANDARD) "$49.99" else null,
            badge = "BEST VALUE",
            features = listOf(
                "ALL 109 patterns",
                "Intelligence Stack",
                "AI Learning (10 algorithms)",
                "Behavioral Guardrails",
                "Proof Capsules",
                "Everything unlocked"
            ),
            isCurrentTier = currentTier == Tier.PRO,
            isLowerTier = false,
            isUpgrade = currentTier == Tier.STARTER || currentTier == Tier.STANDARD,
            onClick = {
                billingManager.purchasePro()
                onPro()
            }
        )

        // Show standalone book purchase for FREE and STARTER users only
        if ((currentTier == Tier.FREE || currentTier == Tier.STARTER) && !hasBook && onBook != null) {
            Spacer(Modifier.height(20.dp))
            
            // Separator
            Text(
                "Add-Ons",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            
            // Standalone book card
            PaywallTierCard(
                title = "The Friendly Trader Book",
                price = "$4.99",
                badge = null,
                features = listOf(
                    "10-chapter trading guide",
                    "Offline reading",
                    "Progress tracking",
                    "Complete beginner curriculum"
                ),
                isCurrentTier = false,
                isLowerTier = false,
                isUpgrade = false,
                onClick = {
                    billingManager.purchaseBook()
                    onBook?.invoke()
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "One-time payment • Lifetime access • No subscriptions",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PaywallTierCard(
    title: String,
    price: String,
    badge: String?,
    features: List<String>,
    isCurrentTier: Boolean = false,
    isLowerTier: Boolean = false,
    isUpgrade: Boolean = false,
    originalPrice: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTier) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                badge?.let {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (isUpgrade) {
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "🎁 UPGRADE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            if (isUpgrade && originalPrice != null) {
                Text(
                    originalPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        price,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "upgrade price",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    "You pay only the difference",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    "$price one-time",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(16.dp))
            features.forEach { feature ->
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("✓ ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(feature, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                enabled = !isCurrentTier && !isLowerTier,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    when {
                        isCurrentTier -> "ALREADY OWNED ✓"
                        isLowerTier -> "CANNOT DOWNGRADE"
                        isUpgrade -> "UPGRADE TO $title"
                        else -> "UPGRADE TO $title"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
