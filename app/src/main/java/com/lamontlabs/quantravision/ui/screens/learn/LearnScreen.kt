package com.lamontlabs.quantravision.ui.screens.learn

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.*

/**
 * Learn Screen - Clean Trading Education Hub
 * Streamlined design matching home screen style
 */
@Composable
fun LearnScreen(
    context: Context,
    onNavigateToTutorials: () -> Unit,
    onNavigateToBook: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Static brand background
        StaticBrandBackground(modifier = Modifier.fillMaxSize())
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // PROGRESS OVERVIEW
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Your Progress",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                color = NeonCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "48% Complete",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "12",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    "Lessons",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "5",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGold
                                )
                                Text(
                                    "Day Streak",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "850",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00FF88)
                                )
                                Text(
                                    "XP Points",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
            
            // LEARNING MODULES SECTION
            item {
                NeonText(
                    text = "LEARNING MODULES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Module 1: Chart Patterns
            item {
                MenuItemCard(
                    title = "Chart Patterns",
                    subtitle = "Master 109 patterns • 48% complete",
                    onClick = onNavigateToTutorials,
                    icon = {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Module 2: Technical Analysis
            item {
                MenuItemCard(
                    title = "Technical Analysis",
                    subtitle = "Support & Resistance • 35% complete",
                    onClick = onNavigateToTutorials,
                    icon = {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Module 3: Indicators
            item {
                MenuItemCard(
                    title = "Indicators",
                    subtitle = "RSI, MACD, Moving Averages • 25% complete",
                    onClick = onNavigateToTutorials,
                    icon = {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Module 4: Trading Psychology
            item {
                MenuItemCard(
                    title = "Trading Psychology",
                    subtitle = "Risk & Money Management • 60% complete",
                    onClick = onNavigateToTutorials,
                    icon = {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // ACHIEVEMENTS SECTION
            item {
                NeonText(
                    text = "ACHIEVEMENTS",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonGold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                MenuItemCard(
                    title = "View All Achievements",
                    subtitle = "7 of 15 unlocked",
                    onClick = onNavigateToAchievements,
                    icon = {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // RESOURCES SECTION
            item {
                NeonText(
                    text = "RESOURCES",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                MenuItemCard(
                    title = "Trading Book",
                    subtitle = "Comprehensive trading guide",
                    onClick = onNavigateToBook,
                    icon = {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // DISCLAIMER
            item {
                GlassMorphicCard(backgroundColor = Color(0xFF0D1219).copy(alpha = 0.7f)) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Educational content only. Not financial advice.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
