package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lamontlabs.quantravision.system.PermissionHelper

/**
 * OnboardingFlow
 * Runs on first launch to guide user through overlay, privacy and disclaimer acceptance.
 */
@Composable
fun OnboardingFlow(context: Context, onComplete: () -> Unit) {
    var step by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = {}) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    0 -> {
                        Text("Welcome to QuantraVision", style = MaterialTheme.typography.headlineSmall)
                        Text("This app highlights technical chart patterns directly on your screen, privately and deterministically.")
                        Button(onClick = { step++ }) { Text("Continue") }
                    }
                    1 -> {
                        Text("Permissions", style = MaterialTheme.typography.titleLarge)
                        Text("We require overlay permission to display highlights and storage to save proof exports.")
                        Button(onClick = { PermissionHelper.requestAll(context); step++ }) { Text("Grant") }
                    }
                    2 -> {
                        Text("Disclaimer", style = MaterialTheme.typography.titleLarge)
                        Text("QuantraVision is for educational visualization only. It performs no trading or financial advice.")
                        Button(onClick = { step++ }) { Text("Agree") }
                    }
                    else -> {
                        Text("Setup complete", style = MaterialTheme.typography.titleLarge)
                        Button(onClick = onComplete) { Text("Start") }
                    }
                }
            }
        }
    }
}
