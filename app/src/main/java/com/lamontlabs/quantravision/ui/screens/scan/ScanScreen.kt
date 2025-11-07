package com.lamontlabs.quantravision.ui.screens.scan

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.MetallicButton
import com.lamontlabs.quantravision.ui.NeonText

/**
 * Scan Screen - Detection Controls
 * Professional interface for:
 * - Starting/stopping detection
 * - Overlay controls
 * - Live detection status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    context: Context,
    onStartScan: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pattern Detection",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Hero section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Radar,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    NeonText(
                        text = "Live Chart Scanning",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Real-time pattern recognition on any trading app",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Start detection button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                MetallicButton(
                    onClick = onStartScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "START DETECTION",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // How it works section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "How It Works",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                HowItWorksCard(
                    step = "1",
                    title = "Grant Permissions",
                    description = "Allow overlay and screen capture permissions"
                )
            }
            
            item {
                HowItWorksCard(
                    step = "2",
                    title = "Open Trading App",
                    description = "Navigate to your favorite trading platform (TradingView, Robinhood, etc.)"
                )
            }
            
            item {
                HowItWorksCard(
                    step = "3",
                    title = "Real-Time Detection",
                    description = "QuantraVision analyzes charts in real-time and overlays detected patterns"
                )
            }
            
            // Features list
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Detection Features",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        FeatureRow(
                            icon = Icons.Default.Speed,
                            text = "Sub-second latency"
                        )
                        
                        FeatureRow(
                            icon = Icons.Default.Vibration,
                            text = "Multi-modal alerts (voice, haptic, visual)"
                        )
                        
                        FeatureRow(
                            icon = Icons.Default.OfflineBolt,
                            text = "100% offline processing"
                        )
                        
                        FeatureRow(
                            icon = Icons.Default.TouchApp,
                            text = "Touch-passthrough overlay"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HowItWorksCard(
    step: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        step,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
