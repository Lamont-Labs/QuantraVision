package com.lamontlabs.quantravision.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.analytics.PatternPerformanceTracker
import com.lamontlabs.quantravision.analytics.model.Outcome
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackSheet(
    context: Context,
    patternMatch: PatternMatch,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedOutcome by remember { mutableStateOf<Outcome?>(null) }
    var userFeedback by remember { mutableStateOf("") }
    var showThankYou by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showThankYou) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.success,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Thank you for your feedback!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your input helps improve pattern accuracy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            } else {
                Text(
                    "Did this pattern work?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    patternMatch.patternName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { selectedOutcome = Outcome.WIN },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedOutcome == Outcome.WIN) 
                                MaterialTheme.colorScheme.success.copy(alpha = 0.2f) 
                            else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ThumbUp, "Win", tint = MaterialTheme.colorScheme.success)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Win", color = MaterialTheme.colorScheme.success)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { selectedOutcome = Outcome.NEUTRAL },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedOutcome == Outcome.NEUTRAL) 
                                MaterialTheme.colorScheme.warning.copy(alpha = 0.2f) 
                            else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Remove, "Neutral", tint = MaterialTheme.colorScheme.warning)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Neutral", color = MaterialTheme.colorScheme.warning)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = { selectedOutcome = Outcome.LOSS },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedOutcome == Outcome.LOSS) 
                                MaterialTheme.colorScheme.error.copy(alpha = 0.2f) 
                            else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ThumbDown, "Loss", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Loss", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = userFeedback,
                    onValueChange = { userFeedback = it },
                    label = { Text("Additional feedback (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            if (selectedOutcome != null) {
                                PatternPerformanceTracker.trackOutcome(
                                    context = context,
                                    patternMatchId = patternMatch.id,
                                    patternName = patternMatch.patternName,
                                    outcome = selectedOutcome!!,
                                    userFeedback = userFeedback,
                                    timeframe = patternMatch.timeframe
                                )
                                showThankYou = true
                            }
                        }
                    },
                    enabled = selectedOutcome != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Feedback")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "⚠️ Educational Only: Pattern tracking is for learning purposes only, not financial advice",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
