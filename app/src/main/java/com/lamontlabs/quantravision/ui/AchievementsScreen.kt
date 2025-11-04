package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.gamification.AchievementSystem
import com.lamontlabs.quantravision.gamification.BonusHighlights
import com.lamontlabs.quantravision.gamification.UserStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val achievements = remember { AchievementSystem.getAll(context) }
    val stats = remember { UserStats.load(context) }
    val bonusHighlights = remember { BonusHighlights.available(context) }
    val progress = remember { AchievementSystem.getProgress(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Achievements",
                        style = MaterialTheme.typography.headlineMedium.copy(shadow = SubtleGlowShadow)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkSurface
                    )
                ) {
                    Column(Modifier.padding(24.dp)) {
                        Text(
                            "Your Progress",
                            style = MaterialTheme.typography.headlineSmall.copy(shadow = SubtleGlowShadow),
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem("Streak", "${stats.currentStreak} days", "🔥")
                            StatItem("Detections", "${stats.totalDetections}", "🎯")
                            StatItem("Bonus", "+$bonusHighlights", "⭐")
                        }
                    }
                }
            }

            // Achievement list
            items(achievements) { achievement ->
                AchievementCard(achievement, progress[achievement.id] ?: 0.0)
            }

            // Footer
            item {
                Text(
                    "Earn bonus highlights by unlocking achievements!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textSecondary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            value, 
            style = MaterialTheme.typography.headlineMedium.copy(shadow = CyanGlowShadow),
            color = ElectricCyan,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label, 
            style = MaterialTheme.typography.bodySmall,
            color = MetallicSilver
        )
    }
}

@Composable
fun AchievementCard(achievement: AchievementSystem.Achievement, progress: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                Gunmetal
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                achievement.icon,
                fontSize = 56.sp,
                modifier = Modifier.padding(end = 16.dp),
                color = if (achievement.unlocked) ElectricCyan else MaterialTheme.colorScheme.textDim
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (achievement.unlocked) ElectricCyan else MaterialTheme.colorScheme.textDim,
                        fontWeight = FontWeight.Bold
                    )
                    if (achievement.unlocked) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "✓", 
                            color = NeonGreen, 
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.titleMedium.copy(shadow = SubtleGlowShadow)
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (achievement.unlocked) 
                        MaterialTheme.colorScheme.textSecondary 
                    else 
                        MaterialTheme.colorScheme.textDim
                )

                if (achievement.reward > 0) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Reward: +${achievement.reward} highlight${if (achievement.reward > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(shadow = SubtleGlowShadow),
                        color = MaterialTheme.colorScheme.amber,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress bar for locked achievements
                if (!achievement.unlocked && progress > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = ElectricCyan,
                        trackColor = Gunmetal
                    )
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Unlock date
                if (achievement.unlocked && achievement.unlockedDate != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Unlocked: ${achievement.unlockedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                }
            }
        }
    }
}
