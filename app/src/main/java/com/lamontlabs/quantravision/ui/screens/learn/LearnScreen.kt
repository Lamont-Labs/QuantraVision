package com.lamontlabs.quantravision.ui.screens.learn

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
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.EmptyState
import com.lamontlabs.quantravision.ui.components.ErrorState
import com.lamontlabs.quantravision.ui.components.FeatureDiscoveryBanner
import com.lamontlabs.quantravision.ui.components.FeatureGate
import com.lamontlabs.quantravision.ui.components.InlineFeatureGate
import com.lamontlabs.quantravision.ui.components.LoadingScreen
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.LearnViewModel

/**
 * LearnScreen - Educational content and lessons
 */
@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    onNavigateToTutorials: () -> Unit = {},
    onNavigateToBook: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { LearnViewModel(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StaticBrandBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.base)
        ) {
            NeonText(
                text = "Learn",
                style = AppTypography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Text(
                        text = "Your Progress",
                        style = AppTypography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    LinearProgressIndicator(
                        progress = if (uiState.totalLessonCount > 0) {
                            uiState.completedLessonCount.toFloat() / uiState.totalLessonCount
                        } else 0f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${uiState.completedLessonCount} of ${uiState.totalLessonCount} lessons completed",
                        style = AppTypography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            FeatureDiscoveryBanner(
                feature = Feature.TRADING_BOOK,
                title = "Trading Book Unlocked!",
                description = "Comprehensive guide to technical analysis and risk management",
                icon = Icons.Default.Book,
                actionLabel = "Read Now",
                onAction = onNavigateToBook,
                accentColor = AppColors.TierStandard
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            FeatureGate(
                feature = Feature.TRADING_BOOK,
                onUpgradeClick = onNavigateToPaywall,
                lockedContent = {
                    InlineFeatureGate(
                        feature = Feature.TRADING_BOOK,
                        onUpgradeClick = onNavigateToPaywall
                    ) {
                        BookCard(onClick = {})
                    }
                }
            ) {
                BookCard(onClick = onNavigateToBook)
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            SectionHeader(title = "Lessons")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            when {
                uiState.isLoading -> {
                    LoadingScreen(message = "Loading lessons...")
                }
                uiState.errorMessage != null -> {
                    ErrorState(
                        message = uiState.errorMessage!!,
                        onRetry = { viewModel.refresh() }
                    )
                }
                uiState.lessons.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.School,
                        message = "No lessons available yet",
                        description = "Check back soon for educational content",
                        actionText = null,
                        onActionClick = null
                    )
                }
                else -> {
                    uiState.lessons.forEach { lesson ->
                        LessonCard(
                            lesson = lesson,
                            onClick = {
                                if (EntitlementManager.hasFeatureAccess(lesson.requiredTier)) {
                                    viewModel.markLessonComplete(lesson.id)
                                } else {
                                    onNavigateToPaywall()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookCard(onClick: () -> Unit) {
    MetallicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = AppColors.NeonGold
            )
            Column {
                Text(
                    text = "Trading Book",
                    style = AppTypography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "Comprehensive pattern encyclopedia",
                    style = AppTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LearnViewModel.Lesson,
    onClick: () -> Unit
) {
    FeatureGate(
        feature = Feature.BASIC_EDUCATION,
        lockedContent = {
            InlineFeatureGate(
                feature = Feature.BASIC_EDUCATION,
                showUpgradePrompt = true
            ) {
                LessonCardContent(lesson, onClick)
            }
        }
    ) {
        LessonCardContent(lesson, onClick)
    }
}

@Composable
private fun LessonCardContent(lesson: LearnViewModel.Lesson, onClick: () -> Unit) {
    MetallicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    style = AppTypography.titleSmall,
                    color = Color.White
                )
                Text(
                    text = lesson.description,
                    style = AppTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            if (lesson.completed) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = AppColors.Success
                )
            } else {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Start",
                    tint = AppColors.NeonCyan
                )
            }
        }
    }
}
