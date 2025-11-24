package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.BillingManager
import com.lamontlabs.quantravision.billing.Tier
import android.app.Activity

@Composable
fun UpgradeScreen(activity: Activity, bm: BillingManager) {
    var tier by remember { mutableStateOf(bm.getUnlockedTier()) }
    var isRestoring by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        bm.onTierChanged = { newTier ->
            tier = newTier
        }
    }
    
    QuantraVisionTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Choose Your Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Monthly subscription • Cancel anytime • Auto-renews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(Modifier.height(24.dp))
                
                // FREE tier status
                TierCard(
                    title = "FREE",
                    price = "Current Plan",
                    features = listOf(
                        "10 basic patterns",
                        "3 scans/day",
                        "1 AI explanation/day",
                        "Basic overlay"
                    ),
                    isCurrentTier = tier == "",
                    isPurchased = false,
                    onPurchase = {}
                )
                
                // BASIC tier
                val basicProduct = bm.getProductDetails("qv_basic_monthly")
                TierCard(
                    title = "BASIC",
                    price = basicProduct?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "$4.99/mo",
                    badge = "MOST POPULAR",
                    features = listOf(
                        "25 core patterns",
                        "25 scans/day",
                        "5 AI explanations/day",
                        "5 saved summaries",
                        "Core overlay"
                    ),
                    isCurrentTier = tier == "BASIC",
                    isPurchased = bm.isBasic(),
                    isUpgrade = false,
                    onPurchase = { bm.purchaseBasic() }
                )
                
                // PRO tier
                val currentTier = when (tier) {
                    "APEX" -> Tier.APEX
                    "PRO" -> Tier.PRO
                    "BASIC" -> Tier.BASIC
                    else -> Tier.FREE
                }
                
                val proProduct = bm.getProductDetails("qv_pro_monthly")
                TierCard(
                    title = "PRO",
                    price = proProduct?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "$14.99/mo",
                    features = listOf(
                        "50 advanced patterns",
                        "75 scans/day",
                        "20 AI explanations/day",
                        "20 saved summaries",
                        "Batch mode",
                        "Full apex overlay"
                    ),
                    isCurrentTier = tier == "PRO",
                    isPurchased = bm.isPro(),
                    isUpgrade = false,
                    onPurchase = { bm.purchasePro() }
                )
                
                // APEX tier
                val apexProduct = bm.getProductDetails("qv_apex_monthly")
                TierCard(
                    title = "APEX",
                    price = apexProduct?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "$29.99/mo",
                    features = listOf(
                        "ALL 109 patterns",
                        "200 scans/day",
                        "60 AI explanations/day",
                        "100 saved summaries",
                        "Advanced logic",
                        "AI scan learning",
                        "Everything unlocked"
                    ),
                    isCurrentTier = tier == "APEX",
                    isPurchased = bm.isApex(),
                    isUpgrade = false,
                    onPurchase = { bm.purchaseApex() }
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Restore purchases button
                OutlinedButton(
                    onClick = {
                        isRestoring = true
                        bm.restorePurchases {
                            tier = bm.getUnlockedTier()
                            isRestoring = false
                        }
                    },
                    enabled = !isRestoring,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Restore Purchases")
                }
            }
        }
    }
}

@Composable
fun TierCard(
    title: String,
    price: String,
    features: List<String>,
    isCurrentTier: Boolean,
    isPurchased: Boolean,
    isUpgrade: Boolean = false,
    onPurchase: () -> Unit,
    badge: String? = null,
    originalPrice: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTier) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                badge?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (isUpgrade) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "🎁 UPGRADE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            
            if (isUpgrade && originalPrice != null) {
                Text(
                    originalPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        price,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "upgrade price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    "You pay only the difference",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            } else {
                Text(
                    price,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.height(8.dp))
            features.forEach { feature ->
                Row(Modifier.padding(vertical = 2.dp)) {
                    Text("✓ ", color = MaterialTheme.colorScheme.primary)
                    Text(feature, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onPurchase,
                enabled = !isPurchased && !isCurrentTier,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        isCurrentTier && isPurchased -> "CURRENT PLAN ✓"
                        isPurchased -> "PURCHASED ✓"
                        isUpgrade -> "UPGRADE NOW"
                        else -> "BUY NOW"
                    }
                )
            }
        }
    }
}
