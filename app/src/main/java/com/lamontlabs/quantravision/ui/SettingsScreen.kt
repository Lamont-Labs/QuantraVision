package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    QuantraVisionTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Text("Theme: Follows system (Dark optimized)")
                Text("Overlay opacity: Adjustable in Quick Controls")
                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
                Text("Lamont Labs", color = MaterialTheme.colorScheme.primary)
                Text("QuantraVision Overlay • v2.x")
            }
        }
    }
}
