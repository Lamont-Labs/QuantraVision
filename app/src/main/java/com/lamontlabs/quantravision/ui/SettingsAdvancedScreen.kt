package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.config.AppConfig
import com.lamontlabs.quantravision.tuning.RuntimeTuner

/**
 * SettingsAdvancedScreen
 * Full control panel for overlay, performance, and detection parameters.
 * Writes to AppConfig and applies via RuntimeTuner without restart.
 */
@Composable
fun SettingsAdvancedScreen(context: Context, onBack: () -> Unit) {
    var cfg by remember { mutableStateOf(AppConfig.load(context)) }

    fun saveApply() {
        AppConfig.save(context, cfg)
        RuntimeTuner.apply(context, cfg)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Controls") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = { TextButton(onClick = { saveApply() }) { Text("Save") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            Text("Overlay", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Opacity")
                Slider(
                    value = cfg.overlay.opacity,
                    onValueChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(opacity = it)) },
                    valueRange = 0f..1f
                )
                Text(String.format("%.2f", cfg.overlay.opacity))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Label Size")
                Slider(
                    value = cfg.overlay.labelTextSizeSp,
                    onValueChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(labelTextSizeSp = it)) },
                    valueRange = 8f..28f
                )
                Text("${cfg.overlay.labelTextSizeSp.toInt()}sp")
            }
            Row {
                Checkbox(checked = cfg.overlay.showHeatmap, onCheckedChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(showHeatmap = it)) })
                Text("Show Heatmap")
            }

            Divider()

            Text("Performance", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Target FPS")
                Slider(
                    value = cfg.performance.targetFps.toFloat(),
                    onValueChange = { cfg = cfg.copy(performance = cfg.performance.copy(targetFps = it.toInt())) },
                    valueRange = 4f..30f
                )
                Text("${cfg.performance.targetFps}")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("ROI Max Regions")
                Slider(
                    value = cfg.performance.roiMaxRegions.toFloat(),
                    onValueChange = { cfg = cfg.copy(performance = cfg.performance.copy(roiMaxRegions = it.toInt())) },
                    valueRange = 1f..64f
                )
                Text("${cfg.performance.roiMaxRegions}")
            }

            Divider()

            Text("Detection", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Global Threshold")
                Slider(
                    value = cfg.detection.globalThreshold.toFloat(),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(globalThreshold = it.toDouble())) },
                    valueRange = 0.4f..0.95f
                )
                Text(String.format("%.2f", cfg.detection.globalThreshold))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Scale Min")
                Slider(
                    value = cfg.detection.scaleMin.toFloat(),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleMin = it.toDouble())) },
                    valueRange = 0.2f..1.0f
                )
                Text(String.format("%.2f", cfg.detection.scaleMin))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Scale Max")
                Slider(
                    value = cfg.detection.scaleMax.toFloat(),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleMax = it.toDouble())) },
                    valueRange = 1.0f..4.0f
                )
                Text(String.format("%.2f", cfg.detection.scaleMax))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Scale Stride")
                Slider(
                    value = cfg.detection.scaleStride.toFloat(),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleStride = it.toDouble())) },
                    valueRange = 0.02f..0.5f
                )
                Text(String.format("%.2f", cfg.detection.scaleStride))
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Temporal Half-Life (s)")
                Slider(
                    value = (cfg.detection.temporalHalfLifeMs / 1000f),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(temporalHalfLifeMs = (it * 1000).toLong())) },
                    valueRange = 1f..30f
                )
                Text("${(cfg.detection.temporalHalfLifeMs / 1000)}s")
            }
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Consensus Sigma")
                Slider(
                    value = cfg.detection.consensusSigma.toFloat(),
                    onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(consensusSigma = it.toDouble())) },
                    valueRange = 0.05f..1.0f
                )
                Text(String.format("%.2f", cfg.detection.consensusSigma))
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = { saveApply() }, modifier = Modifier.fillMaxWidth()) { Text("Save & Apply") }
        }
    }
}
