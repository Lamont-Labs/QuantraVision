package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.BillingManager
import android.app.Activity

@Composable
fun UpgradeScreen(activity: Activity, bm: BillingManager) {
    var tier by remember { mutableStateOf(bm.getUnlockedTier()) }
    var isRestoring by remember { mutableStateOf(false) }
    
    // Update tier when billing manager changes it
    LaunchedEffect(Unit) {
        bm.onTierChanged = { newTier ->
            tier = newTier
        }
    }
    
    QuantraVisionTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "QuantraVision Unlock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text("Buy once. Own forever.")
                Text(
                    "No subscriptions. No renewals.",
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(16.dp))

                // Current tier status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        when {
                            bm.isPro() -> Text(
                                "Current Tier: PRO ✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            bm.isStandard() -> Text(
                                "Current Tier: STANDARD ✓",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            else -> Text("Current Tier: FREE")
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Standard unlock button (dynamic pricing)
                val standardProduct = bm.getProductDetails("qv_standard_one")
                Button(
                    onClick = { bm.purchaseStandard() },
                    enabled = !bm.isStandard() && !bm.isPro(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (standardProduct != null) {
                            "Unlock Standard — ${standardProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99"}"
                        } else {
                            "Unlock Standard (Loading...)"
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Pro unlock button (dynamic pricing)
                val proProduct = bm.getProductDetails("qv_pro_one")
                Button(
                    onClick = { bm.purchasePro() },
                    enabled = !bm.isPro(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (proProduct != null) {
                            "Unlock Pro — ${proProduct.oneTimePurchaseOfferDetails?.formattedPrice ?: "$9.99"}"
                        } else {
                            "Unlock Pro (Loading...)"
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

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

                Spacer(Modifier.height(24.dp))

                // Feature description
                Text(
                    "Pro includes all patterns, confidence overlays, export bundles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
