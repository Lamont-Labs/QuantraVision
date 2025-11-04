package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                title = { 
                    Text(
                        "Advanced Controls",
                        style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow)
                    )
                },
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Overlay",
                style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Opacity",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.overlay.opacity,
                                onValueChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(opacity = it)) },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.overlay.opacity),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Label Size",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.overlay.labelTextSizeSp,
                                onValueChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(labelTextSizeSp = it)) },
                                valueRange = 8f..28f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${cfg.overlay.labelTextSizeSp.toInt()}sp",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = cfg.overlay.showHeatmap,
                            onCheckedChange = { cfg = cfg.copy(overlay = cfg.overlay.copy(showHeatmap = it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            "Show Heatmap",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

            Text(
                "Performance",
                style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Target FPS",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.performance.targetFps.toFloat(),
                                onValueChange = { cfg = cfg.copy(performance = cfg.performance.copy(targetFps = it.toInt())) },
                                valueRange = 4f..30f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${cfg.performance.targetFps}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "ROI Max Regions",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.performance.roiMaxRegions.toFloat(),
                                onValueChange = { cfg = cfg.copy(performance = cfg.performance.copy(roiMaxRegions = it.toInt())) },
                                valueRange = 1f..64f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${cfg.performance.roiMaxRegions}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

            Text(
                "Detection",
                style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            "Global Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.detection.globalThreshold.toFloat(),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(globalThreshold = it.toDouble())) },
                                valueRange = 0.4f..0.95f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.detection.globalThreshold),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Scale Min",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.detection.scaleMin.toFloat(),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleMin = it.toDouble())) },
                                valueRange = 0.2f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.detection.scaleMin),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Scale Max",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.detection.scaleMax.toFloat(),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleMax = it.toDouble())) },
                                valueRange = 1.0f..4.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.detection.scaleMax),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Scale Stride",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.detection.scaleStride.toFloat(),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(scaleStride = it.toDouble())) },
                                valueRange = 0.02f..0.5f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.detection.scaleStride),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Temporal Half-Life (s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = (cfg.detection.temporalHalfLifeMs / 1000f),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(temporalHalfLifeMs = (it * 1000).toLong())) },
                                valueRange = 1f..30f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${(cfg.detection.temporalHalfLifeMs / 1000)}s",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            "Consensus Sigma",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Slider(
                                value = cfg.detection.consensusSigma.toFloat(),
                                onValueChange = { cfg = cfg.copy(detection = cfg.detection.copy(consensusSigma = it.toDouble())) },
                                valueRange = 0.05f..1.0f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                String.format("%.2f", cfg.detection.consensusSigma),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { saveApply() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Save & Apply",
                    style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow)
                )
            }
        }
    }
}
