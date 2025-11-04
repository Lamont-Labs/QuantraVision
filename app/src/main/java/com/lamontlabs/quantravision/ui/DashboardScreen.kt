package com.lamontlabs.quantravision.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
    onStopScan: () -> Unit,
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
    onIntelligence: () -> Unit = {},
    onLearning: () -> Unit = {},
    onAdvancedLearning: () -> Unit = {},
    onExport: () -> Unit = {},
    onPerformance: () -> Unit = {},
    onHelp: () -> Unit = {}
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
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        "QuantraVision Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(shadow = SubtleGlowShadow)
                    ) 
                }
            ) 
        },
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
            
            Text(
                "Detection",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Detection", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Button(
                        onClick = onStopScan,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Detection")
                    }
                    
                    OutlinedButton(
                        onClick = onReview,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.List, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("View Detections")
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            
            Text(
                "Intelligence Stack",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onIntelligence,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.amber
                        )
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Intelligence Hub")
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "📊 LEARNING & ANALYTICS",
                style = MaterialTheme.typography.titleMedium.copy(shadow = CyanGlowShadow),
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onLearning() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Learning Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow),
                            color = CrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "View scan history and learning progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicSilver
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onAdvancedLearning() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Advanced Learning",
                                style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow),
                                color = CrispWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(8.dp))
                            ProBadge()
                        }
                        Text(
                            "ML-powered insights, forecasting, anomaly detection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicSilver
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "💾 EXPORT & PERFORMANCE",
                style = MaterialTheme.typography.titleMedium.copy(shadow = CyanGlowShadow),
                color = ElectricCyan,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onExport() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Export Center",
                            style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow),
                            color = CrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Export patterns as PDF, CSV, or proof capsules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicSilver
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onPerformance() },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            "Performance Dashboard",
                            style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow),
                            color = CrispWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "FPS, battery, memory, thermal metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MetallicSilver
                        )
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            
            Text(
                "Analytics & Tools",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onAnalytics, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pattern Analytics")
                    }
                    
                    OutlinedButton(onClick = onPredictions, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Predictions")
                    }
                    
                    OutlinedButton(onClick = onBacktesting, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Backtesting")
                    }
                    
                    OutlinedButton(onClick = onSimilarity, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Similarity Search")
                    }
                    
                    OutlinedButton(onClick = onMultiChart, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Multi-Chart Comparison")
                    }
                    
                    OutlinedButton(onClick = onTemplates, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Tune, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Template Manager")
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            
            Text(
                "Learn & Progress",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onTutorials, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.School, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tutorials")
                    }
                    
                    OutlinedButton(onClick = onBook, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Book, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Trading Book")
                    }
                    
                    OutlinedButton(onClick = onAchievements, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Achievements")
                    }
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineSmall.copy(shadow = CyanGlowShadow),
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Settings")
                    }
                    
                    OutlinedButton(onClick = onHelp, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Help, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Help & Support")
                    }
                    
                    OutlinedButton(
                        onClick = onClearHighlights,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear All Detections")
                    }
                }
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Alert Settings",
                style = MaterialTheme.typography.titleLarge.copy(shadow = SubtleGlowShadow),
                color = MaterialTheme.colorScheme.primary
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Voice Announcements",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = voiceEnabled,
                    onCheckedChange = { enabled ->
                        voiceEnabled = enabled
                        alertManager.setVoiceEnabled(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Haptic Feedback",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = { enabled ->
                        hapticEnabled = enabled
                        alertManager.setHapticEnabled(enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

@Composable
fun ProBadge() {
    Surface(
        color = BronzeGlow,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = Modifier
    ) {
        Text(
            text = "PRO",
            style = MaterialTheme.typography.labelSmall,
            color = DeepNavyBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
