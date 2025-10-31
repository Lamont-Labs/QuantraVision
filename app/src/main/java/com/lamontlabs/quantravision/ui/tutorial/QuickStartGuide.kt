package com.lamontlabs.quantravision.ui.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * QuickStartGuide - Always-accessible reference guide
 * 
 * Shows the correct order every time user opens the app
 * Can be minimized/dismissed
 */
@Composable
fun QuickStartGuide(
    onDismiss: () -> Unit,
    onOpenFullTutorial: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color(0xFF1A2A3A),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quick Start",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Row {
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) 
                                Icons.Default.ExpandLess 
                            else 
                                Icons.Default.ExpandMore,
                            contentDescription = "Expand",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFFB0BEC5)
                        )
                    }
                }
            }
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Correct order
                OrderedStepCard(
                    number = 1,
                    icon = Icons.Default.ShowChart,
                    title = "Open Chart App FIRST",
                    description = "TradingView, Webull, Robinhood, etc.",
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OrderedStepCard(
                    number = 2,
                    icon = Icons.Default.Apps,
                    title = "Open QuantraVision",
                    description = "Press Home, then open this app",
                    color = Color(0xFF00E5FF)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OrderedStepCard(
                    number = 3,
                    icon = Icons.Default.PlayArrow,
                    title = "Tap 'Start Overlay'",
                    description = "Wait 5-10 seconds for detection",
                    color = Color(0xFFFF9800)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Full tutorial button
                OutlinedButton(
                    onClick = onOpenFullTutorial,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full Interactive Tutorial")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Chart app → QuantraVision → Start Overlay",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
    }
}

@Composable
private fun OrderedStepCard(
    number: Int,
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A1218))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Text
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFFB0BEC5)
            )
        }
    }
}
