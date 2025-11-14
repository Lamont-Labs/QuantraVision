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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.achievements.model.Achievement
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.EmptyState
import com.lamontlabs.quantravision.ui.components.ErrorState
import com.lamontlabs.quantravision.ui.components.FeatureDiscoveryBanner
import com.lamontlabs.quantravision.ui.components.LoadingScreen
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.components.TierBadge
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.HomeViewModel
import com.lamontlabs.quantravision.utils.FormatUtils
import com.lamontlabs.quantravision.entitlements.Feature

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
        when {
            uiState.isLoading -> {
                LoadingScreen(message = "Loading dashboard...")
            }
            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage!!,
                    onRetry = { viewModel.refresh() }
                )
            }
            else -> {
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
                        
                        IconButton(
                            onClick = onNavigateToPaywall,
                            modifier = Modifier.semantics { contentDescription = "Upgrade tier" }
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Upgrade", tint = AppColors.NeonGold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    FeatureDiscoveryBanner(
                        feature = Feature.UNLIMITED_HIGHLIGHTS,
                        title = "Unlimited Highlights Unlocked!",
                        description = "Scan as many charts as you want without daily limits",
                        icon = Icons.Default.AllInclusive,
                        actionLabel = null,
                        accentColor = AppColors.TierStarter
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    FeatureDiscoveryBanner(
                        feature = Feature.ADVANCED_EDUCATION,
                        title = "Advanced Lessons Available!",
                        description = "Access interactive lessons on advanced trading strategies",
                        icon = Icons.Default.School,
                        actionLabel = "Start Learning",
                        onAction = onNavigateToAnalytics,
                        accentColor = AppColors.TierPro
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
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
                    
                    if (uiState.recentDetections.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            message = "No patterns detected yet",
                            description = "Start scanning charts to detect patterns",
                            actionText = null,
                            onActionClick = null
                        )
                    } else {
                        uiState.recentDetections.take(5).forEach { detection ->
                            DetectionCard(detection = detection)
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    if (uiState.achievements.isNotEmpty()) {
                        SectionHeader(
                            title = "Recent Achievements",
                            actionText = "View All",
                            onActionClick = onNavigateToAchievements
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        
                        uiState.achievements.take(3).forEach { achievement ->
                            AchievementMiniCard(achievement = achievement)
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                        }
                    }
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
                    text = "Confidence: ${FormatUtils.formatConfidence(detection.confidence)}",
                    style = AppTypography.bodySmall,
                    color = AppColors.NeonCyan,
                    modifier = Modifier.semantics {
                        contentDescription = "Pattern confidence ${(detection.confidence * 100f).toInt()} percent"
                    }
                )
            }
            
            Text(
                text = FormatUtils.formatTimestamp(detection.timestamp),
                style = AppTypography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun AchievementMiniCard(achievement: Achievement) {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = AppColors.NeonGold,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = achievement.title,
                    style = AppTypography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = "Achievement unlocked!",
                    style = AppTypography.labelSmall,
                    color = AppColors.Success
                )
            }
        }
    }
}
