package com.lamontlabs.quantravision.ui.paywall

import android.app.Activity
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
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.*

@Composable
fun Paywall(
    activity: Activity,
    entitlements: Entitlements,
    onStarter: () -> Unit,
    onStandard: () -> Unit,
    onPro: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Unlock QuantraVision",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Choose the perfect plan for your trading journey",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(8.dp))

        PaywallTierCard(
            title = "STARTER",
            price = "$9.99",
            badge = null,
            features = listOf(
                "25 patterns",
                "Multi-timeframe support",
                "Basic analytics"
            ),
            onClick = onStarter
        )

        PaywallTierCard(
            title = "STANDARD",
            price = "$24.99",
            badge = "MOST POPULAR",
            features = listOf(
                "50 patterns",
                "Full analytics dashboard",
                "50 achievements + 25 lessons",
                "Trading book + exports"
            ),
            onClick = onStandard
        )

        PaywallTierCard(
            title = "PRO",
            price = "$49.99",
            badge = "BEST VALUE",
            features = listOf(
                "ALL 109 patterns",
                "Intelligence Stack",
                "AI Learning (10 algorithms)",
                "Behavioral Guardrails",
                "Proof Capsules",
                "Everything unlocked"
            ),
            onClick = onPro
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "One-time payment • Lifetime access • No subscriptions",
            style = MaterialTheme.typography.bodySmall,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            }
            Text(
                "$price one-time",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            features.forEach { feature ->
                Row(Modifier.padding(vertical = 2.dp)) {
                    Text("✓ ", color = MaterialTheme.colorScheme.primary)
                    Text(feature, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("UPGRADE TO $title")
            }
        }
    }
}
