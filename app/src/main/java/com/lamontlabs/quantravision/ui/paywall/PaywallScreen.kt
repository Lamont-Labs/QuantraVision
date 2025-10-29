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
        Text("Free: 2 highlights/day, 1 pattern. Upgrade for full access.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Standard — \$19.99 (one-time)")
                Text("• 10 highlights/day\n• 30 patterns\n• PDF reports (watermarked)\n• Lessons 1-5\n• Basic achievements")
                Button(onClick = onStandard, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Upgrade to Standard")
                }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Pro — \$49.99 (one-time)")
                Text("• Unlimited highlights\n• All 120+ patterns\n• Predictive detection\n• Complete education\n• Voice commands\n• Watermark-free PDFs")
                Button(onClick = onPro, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Go Pro")
                }
            }
        }
    }
}
