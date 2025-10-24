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
        Text("Free: 5 highlights. Standard: half the patterns. Pro: all patterns + extras.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Standard — \$4.99/mo")
                Text("• Half of chart patterns\n• Unlimited highlights")
                Button(onClick = onStandard, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Upgrade to Standard")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Pro — \$9.99/mo")
                Text("• All patterns\n• Export, multi-watchlist, deep backtest")
                Button(onClick = onPro, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Go Pro")
                }
            }
        }
    }
}
