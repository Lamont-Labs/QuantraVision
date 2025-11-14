package com.lamontlabs.quantravision.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.EmptyState
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.components.TierBadge
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen - Main dashboard displaying user stats, recent detections, and achievements
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { HomeViewModel(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StaticBrandBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.base)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    NeonText(
                        text = "Welcome, ${uiState.userName}",
                        style = AppTypography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    TierBadge(tier = uiState.currentTier)
                }
                
                IconButton(onClick = onNavigateToPaywall) {
                    Icon(Icons.Default.Star, "Upgrade", tint = AppColors.NeonGold)
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                StatCard(
                    title = "Highlights Today",
                    value = "${uiState.todayHighlightCount}",
                    subtitle = if (uiState.currentTier == SubscriptionTier.FREE) {
                        "${uiState.highlightQuotaRemaining} remaining"
                    } else "Unlimited",
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = "Achievements",
                    value = "${uiState.achievements.size}",
                    subtitle = "unlocked",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAchievements
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            SectionHeader(
                title = "Recent Detections",
                actionText = "View All",
                onActionClick = onNavigateToAnalytics
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.recentDetections.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Search,
                    message = "No patterns detected yet",
                    actionText = "Start Scanning",
                    onActionClick = { }
                )
            } else {
                uiState.recentDetections.take(5).forEach { detection ->
                    DetectionCard(detection = detection)
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    MetallicCard(
        modifier = modifier.then(
            onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .padding(AppSpacing.md)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = AppTypography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            NeonText(
                text = value,
                style = AppTypography.headlineLarge
            )
            Text(
                text = subtitle,
                style = AppTypography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun DetectionCard(detection: PatternMatch) {
    MetallicCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = detection.patternName,
                    style = AppTypography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Confidence: ${(detection.confidence * 100).toInt()}%",
                    style = AppTypography.bodySmall,
                    color = AppColors.NeonCyan
                )
            }
            
            Text(
                text = formatTimestamp(detection.timestamp),
                style = AppTypography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
