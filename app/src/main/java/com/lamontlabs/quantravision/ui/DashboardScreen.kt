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
    onTemplates: () -> Unit,
    onAchievements: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onPredictions: () -> Unit = {}
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
            
            // New feature buttons
            Button(onClick = onAchievements, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Achievements")
            }
            
            Button(onClick = onAnalytics, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.TrendingUp, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Pattern Analytics")
            }
            
            Button(onClick = onPredictions, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.TrendingUp, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Pattern Predictions")
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
