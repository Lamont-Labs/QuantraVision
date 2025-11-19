package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.intelligence.llm.ImportState
import com.lamontlabs.quantravision.ui.NeonCyan
import com.lamontlabs.quantravision.ui.NeonGold

/**
 * Dialog showing model import progress
 * 
 * Displays:
 * - Progress bar (0-100%)
 * - Copy speed (MB/s)
 * - Time remaining estimate
 * - Cancel button
 * - Current status text
 */
@Composable
fun ImportModelDialog(
    importState: ImportState,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            // Only allow dismiss on success or error
            if (importState is ImportState.Success || importState is ImportState.Error) {
                onDismiss()
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (importState) {
                        is ImportState.Success -> Icons.Default.CheckCircle
                        is ImportState.Error -> Icons.Default.Error
                        else -> Icons.Default.Download
                    },
                    contentDescription = null,
                    tint = when (importState) {
                        is ImportState.Success -> NeonCyan
                        is ImportState.Error -> Color.Red
                        else -> NeonGold
                    }
                )
                Text(
                    text = when (importState) {
                        is ImportState.Success -> "Import Complete"
                        is ImportState.Error -> "Import Failed"
                        else -> "Importing Model"
                    },
                    color = Color.White
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (importState) {
                    is ImportState.Idle -> {
                        Text(
                            text = "Preparing to import...",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    is ImportState.Selecting -> {
                        Text(
                            text = "Opening file picker...",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NeonCyan
                        )
                    }
                    
                    is ImportState.Copying -> {
                        val progressPercent = importState.progress
                        val totalMB = importState.totalBytes / (1024f * 1024f)
                        val copiedMB = (progressPercent / 100f) * totalMB
                        
                        Text(
                            text = "Copying model file...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        LinearProgressIndicator(
                            progress = progressPercent / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = NeonCyan,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$progressPercent%",
                                color = NeonCyan,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${String.format("%.1f", copiedMB)} / ${String.format("%.1f", totalMB)} MB",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        
                        if (progressPercent > 0) {
                            val estimatedSeconds = ((100 - progressPercent) * 10) // Rough estimate
                            Text(
                                text = "Estimated time: ${estimatedSeconds}s",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    is ImportState.Validating -> {
                        Text(
                            text = "Validating model file...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = importState.progress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = NeonGold
                        )
                    }
                    
                    is ImportState.Success -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "✅ Model imported successfully!",
                                color = NeonCyan,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "The AI model is now ready to use. You can start using DevBot and QuantraBot with real AI-powered explanations.",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    is ImportState.Error -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "❌ Import failed",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = importState.message,
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (importState.recoverable) {
                                Text(
                                    text = "💡 You can try importing again.",
                                    color = NeonGold.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (importState) {
                is ImportState.Success -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Done")
                    }
                }
                is ImportState.Error -> {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Close")
                    }
                }
                is ImportState.Copying -> {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                }
                else -> {
                    // No button during Idle/Selecting/Validating
                }
            }
        },
        containerColor = Color(0xFF1A2332),
        iconContentColor = NeonCyan
    )
}
