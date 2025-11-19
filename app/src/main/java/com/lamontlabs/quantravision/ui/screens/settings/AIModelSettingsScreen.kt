package com.lamontlabs.quantravision.ui.screens.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.intelligence.llm.ImportState
import com.lamontlabs.quantravision.intelligence.llm.ModelImportController
import com.lamontlabs.quantravision.intelligence.llm.ModelManager
import com.lamontlabs.quantravision.intelligence.llm.ModelState
import com.lamontlabs.quantravision.ui.components.ImportModelDialog
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonCyan
import com.lamontlabs.quantravision.ui.NeonGold
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography

/**
 * AI Model Management Settings Screen
 * 
 * Provides comprehensive control over Gemma model:
 * - View model status and details
 * - Import model from phone's Downloads
 * - Remove model to free storage
 * - Access download instructions
 */
@Composable
fun AIModelSettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val modelManager = remember { ModelManager(context) }
    
    // Track model state
    var modelState by remember { mutableStateOf(modelManager.getModelState()) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    // Model import controller
    val importController = remember { ModelImportController(context) }
    val importState by importController.importState.collectAsStateWithLifecycle()
    var showImportDialog by remember { mutableStateOf(false) }
    
    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // File selected - will be handled by controller
        }
    }
    
    // Show import dialog when importing
    LaunchedEffect(importState) {
        showImportDialog = importState !is ImportState.Idle
    }
    
    // Refresh model state after import
    LaunchedEffect(importState) {
        if (importState is ImportState.Success) {
            modelState = modelManager.getModelState()
        }
    }
    
    if (showImportDialog) {
        ImportModelDialog(
            importState = importState,
            onDismiss = {
                showImportDialog = false
                importController.resetState()
                modelState = modelManager.getModelState()
            },
            onCancel = {
                importController.cancelImport()
            }
        )
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red
                    )
                    Text("Remove AI Model?", color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "This will delete the ${modelManager.getModelSizeMB()}MB model file from your device.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "You'll need to re-import the model to use AI-powered features again.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        modelManager.removeModel()
                        modelState = modelManager.getModelState()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("Remove Model")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1A2332)
        )
    }
    
    StaticBrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.base)
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                NeonText(
                    text = "AI Model",
                    style = AppTypography.headlineLarge
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance layout
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            // Model Status Card
            SectionHeader(title = "Model Status")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // Status indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (modelState) {
                                    is ModelState.Ready -> Icons.Default.CheckCircle
                                    is ModelState.Downloaded -> Icons.Default.CloudDone
                                    is ModelState.NotDownloaded -> Icons.Default.CloudOff
                                    is ModelState.Error -> Icons.Default.Error
                                    else -> Icons.Default.CloudDownload
                                },
                                contentDescription = "Status",
                                tint = when (modelState) {
                                    is ModelState.Ready -> NeonCyan
                                    is ModelState.Downloaded -> NeonGold
                                    is ModelState.NotDownloaded -> Color.Gray
                                    is ModelState.Error -> Color.Red
                                    else -> NeonGold
                                }
                            )
                            Text(
                                text = when (modelState) {
                                    is ModelState.Ready -> "Ready"
                                    is ModelState.Downloaded -> "Downloaded (Not Loaded)"
                                    is ModelState.NotDownloaded -> "Not Downloaded"
                                    is ModelState.Loading -> "Loading..."
                                    is ModelState.Generating -> "Generating..."
                                    is ModelState.Error -> "Error"
                                    else -> "Unknown"
                                },
                                style = AppTypography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    // Model details
                    InfoRow("Model Name", "Gemma 3 1B (INT4)")
                    InfoRow("Model Type", "MediaPipe .task")
                    InfoRow(
                        "File Size",
                        if (modelState !is ModelState.NotDownloaded) {
                            "${modelManager.getModelSizeMB()} MB"
                        } else {
                            "~529 MB (when downloaded)"
                        }
                    )
                    
                    if (modelState !is ModelState.NotDownloaded) {
                        InfoRow("Location", "Internal Storage")
                        Text(
                            text = modelManager.getModelPath(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    
                    if (modelState is ModelState.Error) {
                        Divider(color = Color.White.copy(alpha = 0.2f))
                        Text(
                            text = "⚠️ ${(modelState as ModelState.Error).error}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            // Actions Section
            SectionHeader(title = "Actions")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    // Import button
                    Button(
                        onClick = {
                            val activity = context as? Activity
                            if (activity != null) {
                                importController.startImport(filePicker)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = modelState !is ModelState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Import")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (modelState is ModelState.NotDownloaded) {
                                "Import Model from Phone"
                            } else {
                                "Re-import Model"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Remove button (only if model exists)
                    if (modelState !is ModelState.NotDownloaded) {
                        OutlinedButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remove Model", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            // Instructions Card
            SectionHeader(title = "Download Instructions")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How to get the AI model:",
                        style = AppTypography.titleMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    InstructionStep(
                        number = 1,
                        text = "Open your phone's browser and go to HuggingFace"
                    )
                    InstructionStep(
                        number = 2,
                        text = "Search for 'gemma-3-1b-it-int4.task'"
                    )
                    InstructionStep(
                        number = 3,
                        text = "Download the .task file to your phone (~529MB)"
                    )
                    InstructionStep(
                        number = 4,
                        text = "Tap 'Import Model' above and select the downloaded file"
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    Text(
                        text = "💡 The model enables AI-powered explanations in DevBot and QuantraBot. Without it, the app uses template-based responses.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InstructionStep(number: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = NeonCyan.copy(alpha = 0.2f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.weight(1f)
        )
    }
}
