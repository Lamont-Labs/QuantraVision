package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.core.PatternUsageLimiter

/**
 * UpgradeScreen
 * - Simple, self-contained tier upgrade screen.
 * - Only local toggle for demo; in production, integrate with Play Billing.
 */
@Composable
fun UpgradeScreen(
    limiter: PatternUsageLimiter,
    onTierChanged: (PatternUsageLimiter.Tier) -> Unit,
    onBack: () -> Unit
) {
    var selected by remember { mutableStateOf(limiter.currentTier()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F14)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(Color(0xFF121820), shape = RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "QuantraVision Upgrade",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Your current tier: ${selected.name}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(20.dp))

            TierCard(
                name = "Standard",
                desc = "Unlock half of all chart patterns.\nUnlimited detections.",
                price = "$4.99 one-time",
                active = selected == PatternUsageLimiter.Tier.STANDARD
            ) { 
                selected = PatternUsageLimiter.Tier.STANDARD
                limiter.upgradeTo(selected)
                onTierChanged(selected)
            }

            Spacer(Modifier.height(16.dp))

            TierCard(
                name = "Pro",
                desc = "All chart patterns unlocked.\nPriority updates & advanced metrics.",
                price = "$9.99 one-time",
                active = selected == PatternUsageLimiter.Tier.PRO
            ) {
                selected = PatternUsageLimiter.Tier.PRO
                limiter.upgradeTo(selected)
                onTierChanged(selected)
            }

            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onBack) {
                Text("Back", color = Color(0xFF00E5FF))
            }
        }
    }
}

@Composable
private fun TierCard(name: String, desc: String, price: String, active: Boolean, onSelect: () -> Unit) {
    val bgColor = if (active) Color(0xFF00E5FF) else Color(0xFF1A2630)
    val textColor = if (active) Color.Black else Color.White
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(name, color = textColor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(desc, color = textColor, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Text(price, color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}
