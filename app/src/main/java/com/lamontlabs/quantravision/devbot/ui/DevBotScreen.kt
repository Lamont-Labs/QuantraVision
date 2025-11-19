package com.lamontlabs.quantravision.devbot.ui

import android.content.Intent
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamontlabs.quantravision.BuildConfig
import com.lamontlabs.quantravision.devbot.ai.DiagnosticChatMessage
import com.lamontlabs.quantravision.intelligence.llm.ImportState
import com.lamontlabs.quantravision.ui.components.ImportModelDialog
import com.lamontlabs.quantravision.ui.NeonCyan
import com.lamontlabs.quantravision.ui.NeonGold

@Composable
fun DevBotScreen() {
    val context = LocalContext.current
    val viewModel: DevBotViewModel = viewModel()
    
    val importController = viewModel.modelImportController
    val importState by importController.importState.collectAsStateWithLifecycle()
    var showImportDialog by remember { mutableStateOf(false) }
    
    // File picker launcher
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { selectedUri ->
            importController.handleFileSelected(selectedUri)
        }
    }
    
    // Show import dialog when importing
    LaunchedEffect(importState) {
        showImportDialog = importState !is ImportState.Idle
    }
    
    if (showImportDialog) {
        ImportModelDialog(
            importState = importState,
            onDismiss = {
                showImportDialog = false
                importController.resetState()
                
                // Trigger ViewModel to refresh model state after successful import
                if (importState is ImportState.Success) {
                    viewModel.refreshModelState()
                }
            },
            onCancel = {
                importController.cancelImport()
            }
        )
    }
    
    if (!BuildConfig.DEBUG) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A2332)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color.Red,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "DevBot Not Available",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "DevBot is only available in debug builds. This feature is not enabled in release builds.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        return
    }
    
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val hasModel by viewModel.hasModel.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isReady by viewModel.isReady.collectAsStateWithLifecycle()
    val errorStats by viewModel.errorStats.collectAsStateWithLifecycle()
    val exportStatus by viewModel.exportStatus.collectAsStateWithLifecycle()
    
    var showExportConfirmation by remember { mutableStateOf(false) }
    
    LaunchedEffect(exportStatus) {
        when (exportStatus) {
            is ExportStatus.ConfirmationRequired -> {
                showExportConfirmation = true
            }
            is ExportStatus.Success -> {
                context.startActivity((exportStatus as ExportStatus.Success).intent)
                viewModel.resetExportStatus()
            }
            else -> {}
        }
    }
    
    if (showExportConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showExportConfirmation = false
                viewModel.resetExportStatus()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Export Sensitive Data?", color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "This diagnostic export contains sensitive information:",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Complete stack traces\n• App crash details\n• Performance metrics\n• Network URLs\n• Database queries",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Only share with developers you trust.",
                        color = NeonGold,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportConfirmation = false
                        viewModel.exportDiagnostics()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Export & Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportConfirmation = false
                        viewModel.resetExportStatus()
                    }
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1A2332),
            iconContentColor = NeonGold
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (exportStatus is ExportStatus.Error) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export failed: ${(exportStatus as ExportStatus.Error).message}",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.resetExportStatus() }) {
                        Icon(Icons.Default.Close, "Dismiss", tint = Color.Red)
                    }
                }
            }
        }
        
        DevBotHeader(
            isReady = isReady,
            hasModel = hasModel,
            errorStats = errorStats,
            exportStatus = exportStatus,
            onClearChat = { viewModel.clearChat() },
            onClearErrors = { viewModel.clearErrorHistory() },
            onExport = { viewModel.requestExport() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val listState = rememberLazyListState()
        
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    if (!hasModel && isReady) {
                        // Show import prompt when model not available
                        ModelNotFoundCard(
                            onImportClick = {
                                val activity = context as? Activity
                                if (activity != null) {
                                    importController.startImport(filePicker)
                                }
                            }
                        )
                    } else {
                        DevBotWelcome(
                            suggestedQuestions = viewModel.getSuggestedQuestions(),
                            onQuestionClick = { question ->
                                viewModel.updateInputText(question)
                                viewModel.sendMessage()
                            }
                        )
                    }
                }
            } else {
                items(messages) { message ->
                    ChatMessageBubble(message)
                }
            }
            
            if (isProcessing) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        LaunchedEffect(messages.size, isProcessing) {
            if (messages.isNotEmpty() || isProcessing) {
                listState.animateScrollToItem(
                    index = maxOf(0, messages.size + if (isProcessing) 1 else 0 - 1)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        DevBotInputArea(
            inputText = inputText,
            onInputChange = { viewModel.updateInputText(it) },
            onSend = { viewModel.sendMessage() },
            enabled = isReady && !isProcessing
        )
    }
}

@Composable
private fun DevBotHeader(
    isReady: Boolean,
    hasModel: Boolean,
    errorStats: ErrorStats,
    exportStatus: ExportStatus,
    onClearChat: () -> Unit,
    onClearErrors: () -> Unit,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2332)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🤖 DevBot",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = if (!isReady) "Initializing..." 
                              else if (!hasModel) "Fallback Mode" 
                              else "AI Diagnostic Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (hasModel) NeonCyan else NeonGold
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onExport,
                        enabled = exportStatus !is ExportStatus.Exporting
                    ) {
                        if (exportStatus is ExportStatus.Exporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = NeonGold,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Diagnostics",
                                tint = NeonGold
                            )
                        }
                    }
                    IconButton(onClick = onClearChat) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(onClick = onClearErrors) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Errors",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Divider(color = Color.White.copy(alpha = 0.2f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ErrorStatChip("Total", errorStats.totalErrors)
                ErrorStatChip("Crashes", errorStats.crashes, critical = errorStats.crashes > 0)
                ErrorStatChip("Perf", errorStats.performanceIssues)
                ErrorStatChip("Network", errorStats.networkErrors)
                ErrorStatChip("DB", errorStats.databaseIssues)
            }
        }
    }
}

@Composable
private fun ErrorStatChip(label: String, count: Int, critical: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (critical) Color.Red.copy(alpha = 0.2f) else Color(0xFF0D1825)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (critical) Color.Red else NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DevBotWelcome(
    suggestedQuestions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2332)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "👋 Hi! I'm DevBot",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            
            Text(
                text = "I monitor QuantraVision in real-time and help you diagnose issues:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BulletPoint("Analyze crashes and explain what went wrong")
                BulletPoint("Monitor memory, CPU, and performance")
                BulletPoint("Track network and database issues")
                BulletPoint("Provide AI-powered solutions")
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.White.copy(alpha = 0.2f)
            )
            
            Text(
                text = "💡 Tap a question to get started:",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestedQuestions.forEach { question ->
                    SuggestedQuestionChip(
                        text = question,
                        onClick = { onQuestionClick(question) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = NeonGold
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun SuggestedQuestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = NeonCyan.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = NeonCyan.copy(alpha = 0.9f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Ask",
                tint = NeonCyan.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(message: DiagnosticChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    NeonCyan.copy(alpha = 0.2f)
                } else {
                    Color(0xFF1A2332)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A2332)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = NeonCyan.copy(alpha = 0.5f)
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun ModelNotFoundCard(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A2332)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = "AI Model Not Found",
                tint = NeonGold,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "AI Model Not Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "DevBot can still help with diagnostics using fallback templates, but for AI-powered explanations, you'll need to import the Gemma 3 1B model.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            
            Divider(color = Color.White.copy(alpha = 0.2f))
            
            Text(
                text = "📥 Import from Phone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            
            Text(
                text = "1. Download gemma-3-1b-it-int4.task from HuggingFace\n2. Tap Import Model below\n3. Select the .task file from your Downloads folder\n4. Wait for import to complete (~529MB)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            
            Button(
                onClick = onImportClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Download, contentDescription = "Import")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Model from Phone", fontWeight = FontWeight.Bold)
            }
            
            TextButton(onClick = { /* TODO: Open download instructions */ }) {
                Text(
                    text = "Where do I download the model?",
                    color = NeonGold.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DevBotInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask about errors, performance, crashes...") },
            enabled = enabled,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
        
        FilledIconButton(
            onClick = onSend,
            enabled = enabled && inputText.isNotBlank(),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = NeonCyan,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}
