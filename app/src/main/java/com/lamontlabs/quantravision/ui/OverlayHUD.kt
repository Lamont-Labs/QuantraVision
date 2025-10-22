package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.telemetry.PerformanceMonitor

/**
 * Floating heads-up display for FPS, latency, and toggles.
 */
@Composable
fun OverlayHUD(
    modifier: Modifier = Modifier,
    onToggleHeatmap: () -> Unit,
    onToggleBoxes: () -> Unit
) {
    val fps by PerformanceMonitor.fps.collectAsState()
    val lat by PerformanceMonitor.avgLatencyMs.collectAsState()

    Box(
        modifier = modifier
            .background(Color(0x99000000))
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("FPS: $fps", color = Color.White)
                Text("Latency: ${lat}ms", color = Color.White)
            }
            Row {
                TextButton(onClick = onToggleBoxes) { Text("Boxes") }
                TextButton(onClick = onToggleHeatmap) { Text("Heatmap") }
            }
        }
    }
}
