package com.lamontlabs.quantravision.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onClearHighlights: () -> Unit = {},
    onBook: () -> Unit = {},
    onIntelligence: () -> Unit = {}
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
        topBar = { TopAppBar(title = { Text("QuantraVision Dashboard") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModeSwitchBanner(context) {}
            
            AlertSettingsCard(context)
            
            // CORE FEATURE - Most Important
            MetallicText(
                "Detection",
                style = MaterialTheme.typography.titleLarge,
                glowIntensity = 0.8f
            )
            
            MetallicButton(
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                showTopStrip = true
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Detection", fontWeight = FontWeight.Bold)
            }
            
            MetallicButton(
                onClick = onReview,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("View Detections", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            MetallicDivider()
            Spacer(Modifier.height(8.dp))
            
            // INTELLIGENCE STACK
            MetallicText(
                "Intelligence Stack (Pro)",
                style = MaterialTheme.typography.titleLarge,
                glowIntensity = 0.7f
            )
            
            MetallicButton(
                onClick = onIntelligence,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = true
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Intelligence Hub", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            MetallicDivider()
            Spacer(Modifier.height(8.dp))
            
            // PREDICTIONS
            MetallicText(
                "Insights",
                style = MaterialTheme.typography.titleLarge,
                glowIntensity = 0.6f
            )
            
            MetallicButton(
                onClick = onPredictions,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Predictions", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            MetallicDivider()
            Spacer(Modifier.height(8.dp))
            
            // EDUCATION
            MetallicText(
                "Learn & Progress",
                style = MaterialTheme.typography.titleLarge,
                glowIntensity = 0.6f
            )
            
            MetallicButton(
                onClick = onTutorials,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Tutorials", fontWeight = FontWeight.Bold)
            }
            
            MetallicButton(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Trading Book", fontWeight = FontWeight.Bold)
            }
            
            MetallicButton(
                onClick = onAchievements,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Achievements", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            MetallicDivider()
            Spacer(Modifier.height(8.dp))
            
            // SETTINGS
            MetallicButton(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth(),
                showTopStrip = false
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Settings", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AlertSettingsCard(context: Context) {
    val alertManager = remember { AlertManager.getInstance(context) }
    var voiceEnabled by remember { mutableStateOf(alertManager.isVoiceEnabled()) }
    var hapticEnabled by remember { mutableStateOf(alertManager.isHapticEnabled()) }
    
    MetallicCard(
        modifier = Modifier.fillMaxWidth(),
        enableShimmer = false,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetallicText(
                "Alert Settings",
                style = MaterialTheme.typography.titleLarge,
                glowIntensity = 0.6f
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Voice Announcements", fontWeight = FontWeight.Bold)
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
                    Icon(
                        Icons.Default.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Haptic Feedback", fontWeight = FontWeight.Bold)
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
