package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp

/**
 * CrashRecoveryDialog
 * Shown if crash log detected on launch.
 */
@Composable
fun CrashRecoveryDialog(onResume: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(Modifier.padding(16.dp)) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Recovery Available", style = MaterialTheme.typography.titleLarge)
                Text("QuantraVision detected a prior crash. Resume last overlay session?")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss) { Text("No") }
                    Button(onClick = onResume) { Text("Resume") }
                }
            }
        }
    }
}
