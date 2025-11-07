package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Redesigned DashboardScreen with Hybrid Layout using MetallicAccordion components
 * 
 * Features:
 * - Hero CTA: Start Detection button (always visible)
 * - 3 expandable chrome accordions for organized navigation
 * - Settings IconButton in TopAppBar
 * - Clean, professional design
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    // State for accordion expansion (persistent across configuration changes)
    var detectionExpanded by rememberSaveable { mutableStateOf(false) }
    var intelligenceExpanded by rememberSaveable { mutableStateOf(false) }
    var learnExpanded by rememberSaveable { mutableStateOf(false) }
    
    // Placeholder for achievement count (can be replaced with actual data fetching)
    val achievementCount by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QuantraVision Dashboard") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero CTA - Always visible at top
            MetallicButton(
                onClick = onStartScan,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                showTopStrip = true
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Start Detection", fontWeight = FontWeight.Bold)
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Accordion 1: Detection & Review
            MetallicAccordion(
                title = "Detection & Review",
                expanded = detectionExpanded,
                onToggle = { detectionExpanded = !detectionExpanded },
                icon = {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            ) {
                MetallicButton(
                    onClick = onReview,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("View Detections", fontWeight = FontWeight.Bold)
                }
            }
            
            // Accordion 2: Intelligence & Analytics (with 💎 badge)
            MetallicAccordion(
                title = "Intelligence & Analytics",
                expanded = intelligenceExpanded,
                onToggle = { intelligenceExpanded = !intelligenceExpanded },
                icon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("💎", style = MaterialTheme.typography.titleMedium)
                    }
                }
            ) {
                MetallicButton(
                    onClick = onIntelligence,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Intelligence Hub", fontWeight = FontWeight.Bold)
                }
                
                MetallicButton(
                    onClick = onPredictions,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Predictions", fontWeight = FontWeight.Bold)
                }
            }
            
            // Accordion 3: Learn & Progress (with achievement count badge)
            MetallicAccordion(
                title = "Learn & Progress",
                expanded = learnExpanded,
                onToggle = { learnExpanded = !learnExpanded },
                badge = if (achievementCount > 0) achievementCount else null,
                icon = {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            ) {
                MetallicButton(
                    onClick = onTutorials,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Tutorials", fontWeight = FontWeight.Bold)
                }
                
                MetallicButton(
                    onClick = onBook,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Trading Book", fontWeight = FontWeight.Bold)
                }
                
                MetallicButton(
                    onClick = onAchievements,
                    modifier = Modifier.fillMaxWidth(),
                    showTopStrip = false
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Achievements", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
