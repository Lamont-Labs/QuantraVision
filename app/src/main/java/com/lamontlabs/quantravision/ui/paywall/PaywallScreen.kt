package com.lamontlabs.quantravision.ui.paywall

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.*

@Composable
fun Paywall(
    activity: Activity,
    entitlements: Entitlements,
    onStandard: () -> Unit,
    onPro: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Unlock QuantraVision", style = MaterialTheme.typography.headlineSmall)
        Text("Free: 3 highlights/day, 10 basic patterns. Upgrade for unlimited access.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Standard — \$14.99 (one-time)")
                Text("• Unlimited highlights\n• 30 core patterns\n• Regime Navigator\n• Free trading book\n• Remove watermarks\n• Export PDFs\n• All 25 lessons")
                Button(onClick = onStandard, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Upgrade to Standard")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Pro — \$29.99 (one-time)")
                Text("• All Standard features\n• Full 108-pattern library\n• 4 Intelligence Features (Regime Navigator, Pattern-to-Plan, Behavioral Guardrails, Proof Capsules)\n• Free trading book\n• Voice alerts & haptic feedback\n• Predictive detection\n• Auto-scanning watchlist\n• Backtesting engine")
                Button(onClick = onPro, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Go Pro")
                }
            }
        }
    }
}
