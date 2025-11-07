package com.lamontlabs.quantravision.ui.screens.learn

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.ui.*

/**
 * Learn Screen - Trading Education Hub
 * Completely overhauled with modular neon panels and achievement showcase
 * Features:
 * - Quantum grid background with particle starfield
 * - Circular data rings showing progress
 * - Glass morphic cards for learning modules
 * - Horizontal scrolling achievement showcase
 * - HUD-style stat cards with progress rings
 */
@Composable
fun LearnScreen(
    context: Context,
    onNavigateToTutorials: () -> Unit,
    onNavigateToBook: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    // Sample data - would be loaded from actual data sources
    val lessonsCompleted = remember { mutableStateOf(12) }
    val totalLessons = 25
    val currentStreak = remember { mutableStateOf(5) }
    val totalXP = remember { mutableStateOf(850) }
    val achievementsUnlocked = remember { mutableStateOf(7) }
    val totalAchievements = 15
    
    // Learning module data
    val learningModules = remember {
        listOf(
            LearningModule(
                icon = Icons.Default.School,
                title = "Chart Patterns",
                subtitle = "Master 109 patterns",
                progress = 0.48f,
                completedCount = 12,
                totalCount = 25,
                color = NeonCyan
            ),
            LearningModule(
                icon = Icons.Default.TrendingUp,
                title = "Technical Analysis",
                subtitle = "Support & Resistance",
                progress = 0.35f,
                completedCount = 7,
                totalCount = 20,
                color = NeonGold
            ),
            LearningModule(
                icon = Icons.Default.Analytics,
                title = "Indicators",
                subtitle = "RSI, MACD, Moving Averages",
                progress = 0.25f,
                completedCount = 5,
                totalCount = 20,
                color = Color(0xFF00FF88)
            ),
            LearningModule(
                icon = Icons.Default.Psychology,
                title = "Trading Psychology",
                subtitle = "Risk & Money Management",
                progress = 0.60f,
                completedCount = 12,
                totalCount = 20,
                color = Color(0xFFFF4444)
            )
        )
    }
    
    // Achievement data
    val achievements = remember {
        listOf(
            AchievementData("first_detection", "First Detection", "🎯", true, true),
            AchievementData("pattern_master_10", "Pattern Explorer", "🔍", true, true),
            AchievementData("pattern_master_25", "Pattern Expert", "🏆", true, false),
            AchievementData("streak_7", "Weekly Warrior", "⚡", true, true),
            AchievementData("streak_30", "Monthly Master", "👑", false, false),
            AchievementData("quiz_master", "Quiz Champion", "📚", true, false),
            AchievementData("educator", "Pattern Educator", "🎓", true, true),
            AchievementData("high_confidence", "Sharp Eye", "👁️", false, false),
            AchievementData("night_owl", "Night Trader", "🌙", false, false),
            AchievementData("early_adopter", "Early Adopter", "🚀", true, true)
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Quantum Grid Background
        QuantumGridBackground(
            modifier = Modifier.fillMaxSize(),
            gridColor = NeonCyan.copy(alpha = 0.15f),
            animateGrid = true
        )
        
        // Layer 2: Particle Starfield for depth
        ParticleStarfield(
            modifier = Modifier.fillMaxSize(),
            particleCount = 30,
            particleColor = NeonCyan.copy(alpha = 0.6f)
        )
        
        // Layer 3: Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Section
            item {
                HeroSection()
            }
            
            // Progress Overview Card
            item {
                ProgressOverviewCard(
                    lessonsCompleted = lessonsCompleted.value,
                    totalLessons = totalLessons,
                    achievementsUnlocked = achievementsUnlocked.value,
                    totalAchievements = totalAchievements,
                    currentStreak = currentStreak.value
                )
            }
            
            // Quick Stats Row
            item {
                QuickStatsRow(
                    lessonsCompleted = lessonsCompleted.value,
                    totalLessons = totalLessons,
                    currentStreak = currentStreak.value,
                    totalXP = totalXP.value
                )
            }
            
            // Learning Modules Section
            item {
                NeonText(
                    text = "Learning Modules",
                    style = MaterialTheme.typography.titleLarge,
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // 2-column grid of learning modules
            items(learningModules.chunked(2)) { rowModules ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowModules.forEach { module ->
                        LearningModuleCard(
                            module = module,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (module.title) {
                                    "Chart Patterns" -> onNavigateToTutorials()
                                    else -> {}
                                }
                            }
                        )
                    }
                    // Fill empty space if odd number of modules
                    if (rowModules.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            // Achievement Showcase Section
            item {
                NeonText(
                    text = "Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                AchievementShowcase(
                    achievements = achievements,
                    onViewAll = onNavigateToAchievements
                )
            }
            
            // Additional Resources
            item {
                NeonText(
                    text = "Resources",
                    style = MaterialTheme.typography.titleLarge,
                    glowColor = NeonCyan,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                ResourceCard(
                    icon = Icons.Default.MenuBook,
                    title = "Trading Book",
                    description = "Comprehensive pattern recognition handbook",
                    onClick = onNavigateToBook
                )
            }
            
            // Educational disclaimer
            item {
                DisclaimerCard()
            }
        }
    }
}

/**
 * Hero section with logo badge and neon title
 */
@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Logo badge with pulsing glow
        MetallicHeroBadge(
            modifier = Modifier.size(80.dp),
            pulseSync = true
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "Education Hub",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                tint = NeonCyan
            )
        }
        
        // Hero title
        NeonText(
            text = "Trading Education Hub",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            glowColor = NeonCyan,
            enablePulse = false
        )
        
        Text(
            text = "Master chart patterns • Build trading confidence • Track your progress",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Progress overview card with circular data ring
 */
@Composable
private fun ProgressOverviewCard(
    lessonsCompleted: Int,
    totalLessons: Int,
    achievementsUnlocked: Int,
    totalAchievements: Int,
    currentStreak: Int
) {
    val overallProgress = (lessonsCompleted.toFloat() / totalLessons.toFloat())
    val achievementProgress = (achievementsUnlocked.toFloat() / totalAchievements.toFloat())
    
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.85f),
        borderColor = NeonCyan,
        glowIntensity = 0.6f
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NeonText(
                text = "Overall Progress",
                style = MaterialTheme.typography.titleMedium,
                glowColor = NeonCyan
            )
            
            // Circular data ring showing progress
            CircularDataRing(
                rings = listOf(
                    RingData(
                        progress = overallProgress,
                        color = NeonCyan,
                        label = "Lessons"
                    ),
                    RingData(
                        progress = achievementProgress,
                        color = NeonGold,
                        label = "Achievements"
                    ),
                    RingData(
                        progress = (currentStreak / 30f).coerceAtMost(1f),
                        color = Color(0xFF00FF88),
                        label = "Streak"
                    )
                ),
                size = 200.dp,
                centerContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NeonText(
                            text = "${(overallProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            glowColor = NeonCyan
                        )
                        Text(
                            text = "Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            )
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = NeonCyan, label = "Lessons", value = "$lessonsCompleted/$totalLessons")
                LegendItem(color = NeonGold, label = "Achievements", value = "$achievementsUnlocked/$totalAchievements")
                LegendItem(color = Color(0xFF00FF88), label = "Streak", value = "${currentStreak}d")
            }
        }
    }
}

/**
 * Legend item for progress overview
 */
@Composable
private fun LegendItem(color: Color, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

/**
 * Quick stats row with circular HUD progress indicators
 */
@Composable
private fun QuickStatsRow(
    lessonsCompleted: Int,
    totalLessons: Int,
    currentStreak: Int,
    totalXP: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lessons stat
        StatCard(
            label = "Lessons",
            value = "$lessonsCompleted",
            subtitle = "of $totalLessons",
            progress = lessonsCompleted.toFloat() / totalLessons.toFloat(),
            color = NeonCyan,
            modifier = Modifier.weight(1f)
        )
        
        // Streak stat
        StatCard(
            label = "Streak",
            value = "$currentStreak",
            subtitle = "days",
            progress = (currentStreak / 30f).coerceAtMost(1f),
            color = Color(0xFFFFB347),
            modifier = Modifier.weight(1f)
        )
        
        // XP stat
        StatCard(
            label = "Total XP",
            value = "$totalXP",
            subtitle = "points",
            progress = (totalXP / 1000f).coerceAtMost(1f),
            color = Color(0xFF00FF88),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual stat card with circular HUD progress
 */
@Composable
private fun StatCard(
    label: String,
    value: String,
    subtitle: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassMorphicCard(
        modifier = modifier,
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.80f),
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularHUDProgress(
                progress = progress,
                size = 80.dp,
                strokeWidth = 8.dp,
                showTicks = false,
                centerContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = color
                        )
                    }
                }
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Learning module card with icon and progress ring
 */
@Composable
private fun LearningModuleCard(
    module: LearningModule,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassMorphicCard(
        modifier = modifier,
        onClick = onClick,
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.85f),
        borderColor = module.color.copy(alpha = 0.4f),
        glowIntensity = 0.5f
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with glow
            GlowingIcon(
                imageVector = module.icon,
                contentDescription = module.title,
                size = 40.dp,
                glowColor = module.color,
                iconColor = module.color
            )
            
            // Progress ring
            CircularHUDProgress(
                progress = module.progress,
                size = 100.dp,
                strokeWidth = 8.dp,
                showTicks = true,
                centerContent = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${(module.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = module.color
                        )
                    }
                }
            )
            
            // Title and subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = module.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                
                // Completion badge
                if (module.progress >= 1f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF00FF88),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00FF88)
                        )
                    }
                } else {
                    Text(
                        text = "${module.completedCount}/${module.totalCount} complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Achievement showcase - horizontal scrolling row
 */
@Composable
private fun AchievementShowcase(
    achievements: List<AchievementData>,
    onViewAll: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(achievements) { index, achievement ->
                AchievementCard(
                    achievement = achievement,
                    index = index
                )
            }
        }
        
        // View all button
        MetallicButton(
            onClick = onViewAll,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "View All Achievements",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

/**
 * Individual achievement card
 */
@Composable
private fun AchievementCard(
    achievement: AchievementData,
    index: Int
) {
    // Pulsing animation for unlocked achievements
    val pulseAlpha = if (achievement.isUnlocked && achievement.showPulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "achievementPulse_$index")
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_$index"
        ).value
    } else {
        1f
    }
    
    val cardAlpha = if (achievement.isUnlocked) pulseAlpha else 0.4f
    val glowColor = if (achievement.isUnlocked) NeonCyan else Color.Gray
    
    MetallicCard(
        modifier = Modifier
            .width(140.dp)
            .alpha(cardAlpha),
        enableShimmer = achievement.isUnlocked && achievement.showPulse,
        elevation = if (achievement.isUnlocked) 12.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Achievement icon (emoji)
            Text(
                text = achievement.iconEmoji,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.alpha(if (achievement.isUnlocked) 1f else 0.3f)
            )
            
            // Title
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (achievement.isUnlocked) Color.White else Color.Gray,
                textAlign = TextAlign.Center
            )
            
            // Status indicator
            if (achievement.isUnlocked) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Unlocked",
                        tint = NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Unlocked",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

/**
 * Resource card for books, guides, etc.
 */
@Composable
private fun ResourceCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        backgroundColor = Color(0xFF0D1219).copy(alpha = 0.80f),
        borderColor = NeonGold.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlowingIcon(
                imageVector = icon,
                contentDescription = title,
                size = 48.dp,
                glowColor = NeonGold,
                iconColor = NeonGold
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Disclaimer card
 */
@Composable
private fun DisclaimerCard() {
    GlassMorphicCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF1A1A2E).copy(alpha = 0.60f),
        borderColor = Color(0xFFFFB347).copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFFFFB347),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Educational tool only. Pattern detection does not guarantee trading success. Always do your own research and practice proper risk management.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Learning module data class
 */
private data class LearningModule(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val completedCount: Int,
    val totalCount: Int,
    val color: Color
)

/**
 * Achievement data class
 */
private data class AchievementData(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val showPulse: Boolean
)
