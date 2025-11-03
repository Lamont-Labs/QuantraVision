package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * AboutScreen
 * Static page with branding, credits, and build metadata.
 */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("About") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("QuantraVision v1.1", style = MaterialTheme.typography.titleLarge)
            Text("Built by Lamont Labs", style = MaterialTheme.typography.bodyMedium)
            Text("Founder / Architect: Jesse J. Lamont", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Text("All processing occurs entirely on-device.\nNo data leaves your phone.", style = MaterialTheme.typography.bodySmall)
        }
    }
}
