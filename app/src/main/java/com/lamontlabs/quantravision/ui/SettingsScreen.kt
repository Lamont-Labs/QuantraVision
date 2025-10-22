package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SettingsScreen
 * Centralized user preferences, privacy, and accessibility.
 * All stored locally, no network or telemetry.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var darkMode by remember { mutableStateOf(false) }
    var highContrast by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = darkMode, onCheckedChange = { darkMode = it })
                Text("Dark Mode")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = highContrast, onCheckedChange = { highContrast = it })
                Text("High Contrast Theme")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = largeText, onCheckedChange = { largeText = it })
                Text("Large Text")
            }
            Spacer(Modifier.height(24.dp))
            Text("Privacy", style = MaterialTheme.typography.titleMedium)
            Text("QuantraVision does not collect, store, or transmit data. All preferences and logs remain local.")
        }
    }
}
