package com.lamontlabs.quantravision.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.theme.*

/**
 * TierUpgradeCelebration - Post-purchase celebration modal
 * 
 * Shows users exactly what they unlocked after upgrading.
 * Includes deep-links to newly unlocked features.
 * 
 * Design:
 * - Celebratory headline with tier name
 * - List of 3-5 marquee features unlocked
 * - Deep-link CTAs for each feature
 * - Dismissible overlay
 */
@Composable
fun TierUpgradeCelebration(
    tier: SubscriptionTier,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onExploreFeature: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            MetallicCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.85f),
                elevation = AppElevation.highest
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(AppSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(40.dp))
                        NeonText(
                            text = "🎉",
                            style = AppTypography.displayLarge
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    
                    NeonText(
                        text = "Welcome to ${tier.displayName}!",
                        style = AppTypography.headlineLarge
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    
                    Text(
                        text = "You've unlocked powerful new features",
                        style = AppTypography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                    ) {
                        getTierUnlocks(tier).forEach { unlock ->
                            UnlockFeatureCard(
                                unlock = unlock,
                                onExplore = { onExploreFeature(unlock.navigationRoute) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getTierColor(tier)
                        )
                    ) {
                        Text("Start Exploring", style = AppTypography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockFeatureCard(
    unlock: TierUnlock,
    onExplore: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        color = Color.White.copy(alpha = 0.05f),
        tonalElevation = AppElevation.low
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(AppRadius.sm),
                color = unlock.color.copy(alpha = 0.2f)
            ) {
                Icon(
                    unlock.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(AppSpacing.sm),
                    tint = unlock.color
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = unlock.title,
                    style = AppTypography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = unlock.description,
                    style = AppTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            if (unlock.navigationRoute.isNotEmpty()) {
                TextButton(onClick = onExplore) {
                    Text(
                        "Explore",
                        color = unlock.color,
                        style = AppTypography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xxs))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = unlock.color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private data class TierUnlock(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val navigationRoute: String
)

private fun getTierUnlocks(tier: SubscriptionTier): List<TierUnlock> {
    return when (tier) {
        SubscriptionTier.STARTER -> listOf(
            TierUnlock(
                title = "25 Core Patterns",
                description = "15 additional patterns for more comprehensive chart analysis",
                icon = Icons.Default.GridView,
                color = AppColors.TierStarter,
                navigationRoute = "scan"
            ),
            TierUnlock(
                title = "Unlimited Highlights",
                description = "No daily limit on pattern detections - scan as much as you want",
                icon = Icons.Default.AllInclusive,
                color = AppColors.TierStarter,
                navigationRoute = "home"
            ),
            TierUnlock(
                title = "Haptic Alerts",
                description = "Vibration feedback when patterns are detected",
                icon = Icons.Default.Vibration,
                color = AppColors.TierStarter,
                navigationRoute = "settings"
            ),
            TierUnlock(
                title = "Enhanced Analytics",
                description = "Detailed statistics and performance tracking",
                icon = Icons.Default.Analytics,
                color = AppColors.TierStarter,
                navigationRoute = "home"
            )
        )
        
        SubscriptionTier.STANDARD -> listOf(
            TierUnlock(
                title = "50 Advanced Patterns",
                description = "25 more patterns including complex formations and rare setups",
                icon = Icons.Default.AutoAwesome,
                color = AppColors.TierStandard,
                navigationRoute = "scan"
            ),
            TierUnlock(
                title = "Trading Book Access",
                description = "Complete trading education book with strategies and risk management",
                icon = Icons.Default.Book,
                color = AppColors.TierStandard,
                navigationRoute = "learn"
            ),
            TierUnlock(
                title = "Real-Time Market Data",
                description = "Live market information and trend indicators",
                icon = Icons.Default.TrendingUp,
                color = AppColors.TierStandard,
                navigationRoute = "markets"
            ),
            TierUnlock(
                title = "Interactive Lessons",
                description = "25 comprehensive lessons on technical analysis",
                icon = Icons.Default.School,
                color = AppColors.TierStandard,
                navigationRoute = "learn"
            )
        )
        
        SubscriptionTier.PRO -> listOf(
            TierUnlock(
                title = "All 109 Patterns",
                description = "Complete pattern library with every formation in the catalog",
                icon = Icons.Default.StarBorder,
                color = AppColors.TierPro,
                navigationRoute = "scan"
            ),
            TierUnlock(
                title = "Regime Navigator",
                description = "AI-powered market condition analysis for optimal pattern timing",
                icon = Icons.Default.Explore,
                color = AppColors.TierPro,
                navigationRoute = "regime_navigator"
            ),
            TierUnlock(
                title = "Pattern-to-Plan Engine",
                description = "Automatic trade scenario generation with entry/exit levels",
                icon = Icons.Default.CalendarMonth,
                color = AppColors.TierPro,
                navigationRoute = "pattern_to_plan"
            ),
            TierUnlock(
                title = "Behavioral Guardrails",
                description = "Trading psychology protection to avoid emotional mistakes",
                icon = Icons.Default.Psychology,
                color = AppColors.TierPro,
                navigationRoute = "behavioral_guardrails"
            ),
            TierUnlock(
                title = "Voice Alerts",
                description = "Spoken pattern names when detections occur",
                icon = Icons.Default.RecordVoiceOver,
                color = AppColors.TierPro,
                navigationRoute = "settings"
            )
        )
        
        SubscriptionTier.FREE -> emptyList()
    }
}

private fun getTierColor(tier: SubscriptionTier): Color {
    return when (tier) {
        SubscriptionTier.FREE -> AppColors.TierFree
        SubscriptionTier.STARTER -> AppColors.TierStarter
        SubscriptionTier.STANDARD -> AppColors.TierStandard
        SubscriptionTier.PRO -> AppColors.TierPro
    }
}
