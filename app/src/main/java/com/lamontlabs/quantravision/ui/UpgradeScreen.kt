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
                    style = MaterialTheme.typography.displayMedium.copy(shadow = CyanGlowShadow),
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "One-time payment • Lifetime access • No subscriptions",
                    style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow),
                    color = MetallicSilver
                )
                
                Spacer(Modifier.height(32.dp))
                
                // FREE tier status
                TierCard(
                    title = "FREE",
                    price = "Current Plan",
                    features = listOf("10 patterns", "Basic overlay"),
                    isCurrentTier = tier == "",
                    isPurchased = false,
                    onPurchase = {}
                )
                
                // STARTER tier
                val starterProduct = bm.getProductDetails("qv_starter_one")
                TierCard(
                    title = "STARTER",
                    price = starterProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$9.99",
                    features = listOf(
                        "25 patterns",
                        "Multi-timeframe support",
                        "Basic analytics"
                    ),
                    isCurrentTier = tier == "STARTER",
                    isPurchased = bm.isStarter(),
                    isUpgrade = false,
                    onPurchase = { bm.purchaseStarter() }
                )
                
                // STANDARD tier
                val currentTier = when (tier) {
                    "PRO" -> Tier.PRO
                    "STANDARD" -> Tier.STANDARD
                    "STARTER" -> Tier.STARTER
                    else -> Tier.FREE
                }
                
                val standardUpgradeSku = bm.getUpgradeSku(currentTier, Tier.STANDARD)
                val isStandardUpgrade = standardUpgradeSku != null
                val standardUpgradeProduct = standardUpgradeSku?.let { bm.getProductDetails(it) }
                val standardProduct = bm.getProductDetails("qv_standard_one")
                
                TierCard(
                    title = "STANDARD",
                    price = if (isStandardUpgrade) {
                        standardUpgradeProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$15.00"
                    } else {
                        standardProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$24.99"
                    },
                    originalPrice = if (isStandardUpgrade) {
                        standardProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$24.99"
                    } else null,
                    badge = "MOST POPULAR",
                    features = listOf(
                        "50 patterns",
                        "Full analytics dashboard",
                        "50 achievements + 25 lessons",
                        "Trading book + exports"
                    ),
                    isCurrentTier = tier == "STANDARD",
                    isPurchased = bm.isStandard(),
                    isUpgrade = isStandardUpgrade,
                    onPurchase = { bm.purchaseStandard() }
                )
                
                // PRO tier
                val proUpgradeSku = bm.getUpgradeSku(currentTier, Tier.PRO)
                val isProUpgrade = proUpgradeSku != null
                val proUpgradeProduct = proUpgradeSku?.let { bm.getProductDetails(it) }
                val proProduct = bm.getProductDetails("qv_pro_one")
                
                TierCard(
                    title = "PRO",
                    price = if (isProUpgrade) {
                        proUpgradeProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: when (currentTier) {
                            Tier.STARTER -> "$40.00"
                            Tier.STANDARD -> "$25.00"
                            else -> "$49.99"
                        }
                    } else {
                        proProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$49.99"
                    },
                    originalPrice = if (isProUpgrade) {
                        proProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$49.99"
                    } else null,
                    features = listOf(
                        "ALL 109 patterns",
                        "Intelligence Stack",
                        "AI Learning (10 algorithms)",
                        "Behavioral Guardrails",
                        "Proof Capsules",
                        "Everything unlocked"
                    ),
                    isCurrentTier = tier == "PRO",
                    isPurchased = bm.isPro(),
                    isUpgrade = isProUpgrade,
                    onPurchase = { bm.purchasePro() }
                )
                
                // Show standalone book purchase for FREE and STARTER users only
                if ((tier == "" || tier == "STARTER") && !bm.hasBook()) {
                    Spacer(Modifier.height(32.dp))
                    
                    // Separator
                    Text(
                        "Add-Ons",
                        style = MaterialTheme.typography.headlineSmall.copy(shadow = SubtleGlowShadow),
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Standalone book card
                    val bookProduct = bm.getProductDetails("qv_book_standalone")
                    TierCard(
                        title = "The Friendly Trader Book",
                        price = bookProduct?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99",
                        features = listOf(
                            "10-chapter trading guide",
                            "Offline reading",
                            "Progress tracking",
                            "Complete beginner curriculum"
                        ),
                        isCurrentTier = false,
                        isPurchased = false,
                        onPurchase = { bm.purchaseBook() }
                    )
                }
                
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MetallicSilver
                    )
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            color = ElectricCyan
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        "Restore Purchases",
                        style = MaterialTheme.typography.titleSmall
                    )
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
    val borderColor = when {
        isCurrentTier -> ElectricCyan
        badge != null -> AmberAccent
        else -> Gunmetal
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTier) 
                DarkSurface 
            else 
                Gunmetal
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCurrentTier || badge != null) 2.dp else 0.dp,
            color = borderColor
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = if (isCurrentTier) CyanGlowShadow else null
                    ),
                    color = if (isCurrentTier) ElectricCyan else CrispWhite,
                    fontWeight = FontWeight.Bold
                )
                badge?.let {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium.copy(shadow = SubtleGlowShadow),
                        color = AmberAccent,
                        modifier = Modifier
                            .background(
                                AmberAccent.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (isUpgrade) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "🎁 UPGRADE",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberAccent,
                        modifier = Modifier
                            .background(
                                AmberAccent.copy(alpha = 0.2f),
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
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = TextDecoration.LineThrough,
                    color = TextGray
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        price,
                        style = MaterialTheme.typography.displaySmall.copy(shadow = CyanGlowShadow),
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "upgrade price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MetallicSilver
                    )
                }
                Text(
                    "You pay only the difference",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetallicSilver
                )
            } else {
                Text(
                    price,
                    style = MaterialTheme.typography.displaySmall.copy(shadow = CyanGlowShadow),
                    color = ElectricCyan,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(16.dp))
            features.forEach { feature ->
                Row(
                    Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✓ ",
                        color = ElectricCyan,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrispWhite
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onPurchase,
                enabled = !isPurchased && !isCurrentTier,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isCurrentTier && isPurchased -> NeonGreen
                        badge != null && !isPurchased -> AmberAccent
                        else -> ElectricCyan
                    },
                    disabledContainerColor = Gunmetal
                )
            ) {
                Text(
                    when {
                        isCurrentTier && isPurchased -> "CURRENT PLAN ✓"
                        isPurchased -> "PURCHASED ✓"
                        isUpgrade -> "UPGRADE NOW"
                        else -> "BUY NOW"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
