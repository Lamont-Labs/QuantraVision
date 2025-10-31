package com.lamontlabs.quantravision.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.voice.*
import kotlinx.coroutines.delay

/**
 * Updated DashboardScreen with mode toggle banner integration and voice commands.
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
    onPredictions: () -> Unit = {},
    onBacktesting: () -> Unit = {},
    onSimilarity: () -> Unit = {},
    onMultiChart: () -> Unit = {},
    onClearHighlights: () -> Unit = {}
) {
    var voiceCommandStatus by remember { mutableStateOf<VoiceCommandStatus?>(null) }
    var showStatusMessage by remember { mutableStateOf(false) }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == 
                PackageManager.PERMISSION_GRANTED
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            voiceCommandStatus = VoiceCommandStatus(
                VoiceCommandState.ERROR,
                "Microphone permission required for voice commands"
            )
            showStatusMessage = true
        }
    }
    
    val voiceHandler = rememberVoiceCommandHandler(
        onStatusChange = { status ->
            voiceCommandStatus = status
            showStatusMessage = true
        },
        onCommandExecuted = { result ->
            when (result) {
                is VoiceCommandResult.FilterPattern -> {
                    // Filter logic would be implemented here
                }
                is VoiceCommandResult.ClearFilter -> {
                    // Clear filter logic
                }
                is VoiceCommandResult.ExportPDF -> {
                    // PDF already exported by processor
                }
                is VoiceCommandResult.StartScanning -> onStartScan()
                is VoiceCommandResult.StopScanning -> {
                    // Stop scanning logic
                }
                is VoiceCommandResult.NavigateAchievements -> onAchievements()
                is VoiceCommandResult.NavigateAnalytics -> onAnalytics()
                is VoiceCommandResult.NavigatePredictions -> onPredictions()
                is VoiceCommandResult.ClearHighlights -> onClearHighlights()
                is VoiceCommandResult.RefreshDetection -> onStartScan()
                else -> {}
            }
        }
    )
    
    LaunchedEffect(showStatusMessage) {
        if (showStatusMessage) {
            delay(3000)
            showStatusMessage = false
        }
    }
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("QuantraVision Dashboard") }) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = showStatusMessage && voiceCommandStatus != null,
                    enter = fadeIn() + slideInHorizontally { it },
                    exit = fadeOut() + slideOutHorizontally { it }
                ) {
                    Surface(
                        tonalElevation = 4.dp,
                        shape = MaterialTheme.shapes.medium,
                        color = when (voiceCommandStatus?.state) {
                            VoiceCommandState.LISTENING -> MaterialTheme.colorScheme.primaryContainer
                            VoiceCommandState.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
                            VoiceCommandState.ERROR -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = voiceCommandStatus?.message ?: "",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                FloatingActionButton(
                    onClick = {
                        if (hasAudioPermission) {
                            when (voiceCommandStatus?.state) {
                                VoiceCommandState.LISTENING -> voiceHandler.stopListening()
                                else -> voiceHandler.startListening()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    containerColor = when (voiceCommandStatus?.state) {
                        VoiceCommandState.LISTENING -> MaterialTheme.colorScheme.error
                        VoiceCommandState.PROCESSING -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Icon(
                        imageVector = when (voiceCommandStatus?.state) {
                            VoiceCommandState.LISTENING -> Icons.Default.MicOff
                            VoiceCommandState.PROCESSING -> Icons.Default.HourglassEmpty
                            else -> Icons.Default.Mic
                        },
                        contentDescription = "Voice Commands"
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSwitchBanner(context) {}
            
            AlertSettingsCard(context)
            
            Button(onClick = onStartScan, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Start Detection")
            }
            
            Button(onClick = onReview, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.List, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("View Detections")
            }
            
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
            
            Button(onClick = onBacktesting, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Pattern Backtesting")
            }
            
            Button(onClick = onSimilarity, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Similarity Search")
            }
            
            Button(onClick = onMultiChart, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CompareArrows, contentDescription = null)
                Spacer(Modifier.width(8.dp)); Text("Multi-Chart Comparison")
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

@Composable
fun AlertSettingsCard(context: Context) {
    val alertManager = remember { AlertManager.getInstance(context) }
    var voiceEnabled by remember { mutableStateOf(alertManager.isVoiceEnabled()) }
    var hapticEnabled by remember { mutableStateOf(alertManager.isHapticEnabled()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Alert Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Voice Announcements")
                }
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = { enabled ->
                        voiceEnabled = enabled
                        alertManager.setVoiceEnabled(enabled)
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vibration, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Haptic Feedback")
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { enabled ->
                        hapticEnabled = enabled
                        alertManager.setHapticEnabled(enabled)
                    }
                )
            }
        }
    }
}
