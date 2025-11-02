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
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Your Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 32.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun AchievementCard(achievement: AchievementSystem.Achievement, progress: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) 
                MaterialTheme.colorScheme.tertiaryContainer
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                achievement.icon,
                fontSize = 40.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (achievement.unlocked) {
                        Spacer(Modifier.width(8.dp))
                        Text("✓", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                    }
                }
                
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (achievement.reward > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Reward: +${achievement.reward} highlight${if (achievement.reward > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress bar for unlocked achievements
                if (!achievement.unlocked && progress > 0) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Unlock date
                if (achievement.unlocked && achievement.unlockedDate != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Unlocked: ${achievement.unlockedDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
