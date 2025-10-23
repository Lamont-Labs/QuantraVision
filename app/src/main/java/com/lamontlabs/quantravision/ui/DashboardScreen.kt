package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Updated DashboardScreen with mode toggle banner integration.
 */
@Composable
fun DashboardScreen(
    context: Context,
    onStartScan: () -> Unit,
    onReview: () -> Unit,
    onTutorials: () -> Unit,
    onSettings: () -> Unit,
    onTemplates: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("QuantraVision Dashboard") }) }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSwitchBanner(context) {}
            Button(onClick = onStartScan, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Start Detection")
            }
            Button(onClick = onReview, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("View Detections")
            }
            Button(onClick = onTemplates, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Tune, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Template Manager")
            }
            Button(onClick = onTutorials, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.School, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Tutorials & Quizzes")
            }
            Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Settings")
            }
        }
    }
}
