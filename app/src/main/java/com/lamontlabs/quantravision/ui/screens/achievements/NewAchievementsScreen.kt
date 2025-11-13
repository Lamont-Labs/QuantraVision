package com.lamontlabs.quantravision.ui.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.achievements.model.AchievementCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAchievementsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel = remember { AchievementsViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()
    
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${uiState.unlockedCount} / ${uiState.totalCount}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Achievements Unlocked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = if (uiState.totalCount > 0) {
                            uiState.unlockedCount.toFloat() / uiState.totalCount.toFloat()
                        } else {
                            0f
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            ScrollableTabRow(
                selectedTabIndex = if (uiState.selectedCategory == null) 0 else {
                    AchievementCategory.values().indexOf(uiState.selectedCategory) + 1
                },
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = uiState.selectedCategory == null,
                    onClick = { viewModel.filterByCategory(null) },
                    text = { Text("All") }
                )
                AchievementCategory.values().forEach { category ->
                    Tab(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.filterByCategory(category) },
                        text = { Text(category.name.lowercase().capitalize()) }
                    )
                }
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.achievements) { achievement ->
                        AchievementGridItem(achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementGridItem(achievement: Achievement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = achievement.iconEmoji,
                fontSize = 48.sp,
                modifier = Modifier.padding(8.dp)
            )
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            if (!achievement.isUnlocked && achievement.totalRequired > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = achievement.getProgressPercent(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
                Text(
                    text = "${achievement.progress}/${achievement.totalRequired}",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (achievement.isUnlocked) {
                Text(
                    text = "✓ Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
