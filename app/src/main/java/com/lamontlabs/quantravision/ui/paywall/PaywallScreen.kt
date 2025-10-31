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
        Text("Free: 3-5 highlights, 30 core patterns. Upgrade for unlimited access.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Standard — \$9.99 (one-time)")
                Text("• Unlimited highlights\n• 30 core patterns\n• Enhanced detection\n• Remove watermarks\n• Export PDFs\n• All 25 lessons")
                Button(onClick = onStandard, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Upgrade to Standard")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Pro — \$24.99 (one-time)")
                Text("• All Standard features\n• Full 108-pattern library\n• Voice alerts & haptic feedback\n• Predictive detection (40-85% formation)\n• Pattern invalidation alerts\n• Auto-scanning watchlist\n• Backtesting engine")
                Button(onClick = onPro, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Go Pro")
                }
            }
        }
    }
}
