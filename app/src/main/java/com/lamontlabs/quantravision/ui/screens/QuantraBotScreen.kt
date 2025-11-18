package com.lamontlabs.quantravision.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamontlabs.quantravision.ui.AdvancedVisuals.StaticBrandBackground
import com.lamontlabs.quantravision.ui.theme.DesignSystem.NeonCyan
import com.lamontlabs.quantravision.ui.theme.DesignSystem.NeonGold
import java.text.SimpleDateFormat
import java.util.*

/**
 * QuantraBot - AI Trading Assistant Screen
 * 
 * Chat interface for pattern validation, explanations, and trading Q&A.
 * Uses on-device Gemma 2B LLM with pattern knowledge base.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantraBotScreen(
    viewModel: QuantraBotViewModel = viewModel(),
    paddingValues: PaddingValues
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isReady by viewModel.isReady.collectAsState()
    val hasModel by viewModel.hasModel.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    
    // Initialize on first composition
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }
    
    StaticBrandBackground(contentAlignment = Alignment.TopStart) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D1825).copy(alpha = 0.95f),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "QuantraBot",
                            tint = NeonCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = "QuantraBot",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (!isReady) "Initializing..." 
                                      else if (!hasModel) "Fallback Mode" 
                                      else "AI Assistant Ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasModel) NeonCyan else NeonGold
                            )
                        }
                    }
                    
                    if (!hasModel && isReady) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 AI model not available. Using fallback explanations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonGold.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            // Messages
            val listState = rememberLazyListState()
            
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        WelcomeMessage(
                            onQuestionClick = { question ->
                                viewModel.updateInputText(question)
                                viewModel.sendMessage()
                            }
                        )
                    }
                } else {
                    items(messages) { message ->
                        ChatMessageBubble(message)
                    }
                }
                
                if (isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }
            
            // Auto-scroll to bottom when new message arrives
            LaunchedEffect(messages.size, isLoading) {
                if (messages.isNotEmpty() || isLoading) {
                    listState.animateScrollToItem(
                        index = maxOf(0, messages.size + if (isLoading) 1 else 0 - 1)
                    )
                }
            }
            
            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF0D1825).copy(alpha = 0.95f),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask about patterns, scans, trading...") },
                        enabled = isReady && !isLoading,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    
                    FilledIconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = isReady && !isLoading && inputText.isNotBlank(),
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
        }
    }
}

@Composable
private fun WelcomeMessage(
    onQuestionClick: (String) -> Unit = {}
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
                text = "👋 Hi! I'm QuantraBot",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
            
            Text(
                text = "I'm your AI trading assistant. I can help you:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BulletPoint("Explain pattern detections in plain English")
                BulletPoint("Validate patterns and explain QuantraScores")
                BulletPoint("Answer trading and technical analysis questions")
                BulletPoint("Compare different chart patterns")
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
                SuggestedQuestion(
                    text = "What makes a Head and Shoulders pattern valid?",
                    onClick = { onQuestionClick("What makes a Head and Shoulders pattern valid?") }
                )
                SuggestedQuestion(
                    text = "Difference between bull flag and cup and handle?",
                    onClick = { onQuestionClick("What's the difference between a bull flag and cup and handle?") }
                )
                SuggestedQuestion(
                    text = "Explain my last scan results",
                    onClick = { onQuestionClick("Explain my last scan results") }
                )
                SuggestedQuestion(
                    text = "How do I use QuantraScore effectively?",
                    onClick = { onQuestionClick("How do I use QuantraScore to find high-quality patterns?") }
                )
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
private fun SuggestedQuestion(
    text: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
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
private fun ChatMessageBubble(message: ChatMessage) {
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
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!message.isUser) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Bot",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "QuantraBot",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
                
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                
                Text(
                    text = message.formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Bot typing",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Thinking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = NeonCyan
                )
            }
        }
    }
}

/**
 * Chat message data class
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
