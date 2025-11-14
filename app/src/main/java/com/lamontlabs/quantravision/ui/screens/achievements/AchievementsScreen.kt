package com.lamontlabs.quantravision.ui.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.achievements.AchievementManager
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val achievementManager = remember { AchievementManager.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var allAchievements by remember { mutableStateOf<List<Achievement>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        isLoading = true
        allAchievements = achievementManager.getAllAchievements()
        isLoading = false
    }
    
    val unlockedAchievements = allAchievements.filter { it.isUnlocked }
    val lockedAchievements = allAchievements.filter { !it.isUnlocked }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface
                )
            )
        }
    ) { paddingValues ->
        StaticBrandBackground {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(AppSpacing.base)
                ) {
                    MetallicCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                NeonText(
                                    text = "${unlockedAchievements.size}/${allAchievements.size}",
                                    style = AppTypography.headlineLarge
                                )
                                Text(
                                    text = "Achievements Unlocked",
                                    style = AppTypography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                            
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = AppColors.NeonGold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        if (unlockedAchievements.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Unlocked")
                                Spacer(modifier = Modifier.height(AppSpacing.sm))
                            }
                            
                            items(unlockedAchievements) { achievement ->
                                AchievementCard(
                                    achievement = achievement,
                                    unlocked = true
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(AppSpacing.md))
                            }
                        }
                        
                        if (lockedAchievements.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Locked")
                                Spacer(modifier = Modifier.height(AppSpacing.sm))
                            }
                            
                            items(lockedAchievements) { achievement ->
                                AchievementCard(
                                    achievement = achievement,
                                    unlocked = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: Achievement,
    unlocked: Boolean
) {
    MetallicCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconEmoji,
                    fontSize = 32.sp,
                    color = if (unlocked) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = AppTypography.titleMedium,
                    color = if (unlocked) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = achievement.description,
                    style = AppTypography.bodySmall,
                    color = if (unlocked) {
                        Color.White.copy(alpha = 0.7f)
                    } else {
                        Color.White.copy(alpha = 0.3f)
                    }
                )
                
                if (unlocked) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.Success
                        )
                        Text(
                            text = "Unlocked",
                            style = AppTypography.labelSmall,
                            color = AppColors.Success
                        )
                    }
                } else if (achievement.totalRequired > 1) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Column {
                        LinearProgressIndicator(
                            progress = achievement.getProgressPercent(),
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.NeonCyan
                        )
                        Text(
                            text = "${achievement.progress}/${achievement.totalRequired}",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = AppSpacing.xxs)
                        )
                    }
                }
            }
        }
    }
}
