package com.lamontlabs.quantravision.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.onboarding.FeatureDiscoveryStore
import com.lamontlabs.quantravision.ui.theme.*
import kotlinx.coroutines.launch

/**
 * FeatureDiscoveryBanner - Contextual banner for newly unlocked features
 * 
 * Shows a dismissible banner highlighting a newly unlocked feature
 * until user interacts with it or dismisses it.
 * 
 * Observes reactive StateFlow to automatically appear when tier changes,
 * even if the screen is on the back stack.
 * 
 * Architecture:
 * - Observes FeatureDiscoveryStore.undiscoveredFeatures StateFlow
 * - Derives visibility from: feature in undiscoveredFeatures && !isDismissed
 * - Marks feature discovered on IO thread when dismissed
 * 
 * Usage:
 * ```
 * FeatureDiscoveryBanner(
 *     feature = Feature.TRADING_BOOK,
 *     title = "New: Trading Book Unlocked!",
 *     description = "Access comprehensive trading education",
 *     icon = Icons.Default.Book,
 *     actionLabel = "Read Now",
 *     onAction = { navController.navigate("book") }
 * )
 * ```
 */
@Composable
fun FeatureDiscoveryBanner(
    feature: Feature,
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.NewReleases,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    accentColor: Color = AppColors.NeonCyan
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val undiscoveredFeatures by FeatureDiscoveryStore.undiscoveredFeatures.collectAsState()
    
    var isDismissed by remember { mutableStateOf(false) }
    
    val isVisible = feature in undiscoveredFeatures && !isDismissed
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.md),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(AppRadius.sm),
                    color = accentColor.copy(alpha = 0.2f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(AppSpacing.xs),
                        tint = accentColor
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = title,
                            style = AppTypography.titleSmall,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    
                    Text(
                        text = description,
                        style = AppTypography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    if (actionLabel != null && onAction != null) {
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                        TextButton(
                            onClick = {
                                onAction()
                                isDismissed = true
                                scope.launch {
                                    FeatureDiscoveryStore.markFeatureDiscovered(context, feature)
                                }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                actionLabel,
                                color = accentColor,
                                style = AppTypography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xxs))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                IconButton(
                    onClick = { 
                        isDismissed = true
                        scope.launch {
                            FeatureDiscoveryStore.markFeatureDiscovered(context, feature)
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Compact version for inline placement within screens
 * 
 * Observes reactive StateFlow to automatically appear when tier changes,
 * even if the screen is on the back stack.
 */
@Composable
fun CompactFeatureDiscoveryBanner(
    feature: Feature,
    message: String,
    icon: ImageVector = Icons.Default.TipsAndUpdates,
    accentColor: Color = AppColors.NeonGold,
    onAction: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val undiscoveredFeatures by FeatureDiscoveryStore.undiscoveredFeatures.collectAsState()
    
    var isDismissed by remember { mutableStateOf(false) }
    
    val isVisible = feature in undiscoveredFeatures && !isDismissed
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AppRadius.sm),
            color = accentColor.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = message,
                    style = AppTypography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                
                if (onAction != null) {
                    TextButton(
                        onClick = {
                            onAction()
                            isDismissed = true
                            scope.launch {
                                FeatureDiscoveryStore.markFeatureDiscovered(context, feature)
                            }
                        },
                        contentPadding = PaddingValues(horizontal = AppSpacing.xs)
                    ) {
                        Text(
                            "Try",
                            color = accentColor,
                            style = AppTypography.labelSmall
                        )
                    }
                }
                
                IconButton(
                    onClick = { 
                        isDismissed = true
                        scope.launch {
                            FeatureDiscoveryStore.markFeatureDiscovered(context, feature)
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
