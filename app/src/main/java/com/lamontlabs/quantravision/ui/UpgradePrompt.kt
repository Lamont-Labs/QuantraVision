package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.quota.HighlightQuota

/**
 * UpgradePrompt
 * Shown when free highlight quota is exhausted.
 * Host should wire purchase flow in onUpgrade().
 */
@Composable
fun UpgradePrompt(
    context: Context,
    visible: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    if (!visible) return
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Unlock QuantraVision Pro", style = MaterialTheme.typography.titleLarge)
                Text("You’ve used all free pattern highlights.")
                Text("Upgrade to continue highlighting every pattern without limits.")
                Text("Includes: full template library, tuning, signed proofs.")
                Divider()
                Text("Free highlights used: ${HighlightQuota.state(context).count}")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Later") }
                    Button(onClick = onUpgrade) { Text("Upgrade") }
                }
            }
        }
    }
}
