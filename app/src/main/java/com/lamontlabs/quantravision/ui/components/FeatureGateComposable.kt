package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppRadius
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.theme.MetallicText

/**
 * FeatureGate Composable - Tier-based feature locking system
 * 
 * Conditionally displays content based on user's subscription tier.
 * Shows beautiful upgrade prompts for locked features using the MetallicCard design system.
 * 
 * ## Usage Examples
 * 
 * ### Example 1: Full screen lock with upgrade prompt
 * ```kotlin
 * FeatureGate(
 *     feature = Feature.TRADING_BOOK,
 *     onUpgradeClick = { navController.navigate("paywall") }
 * ) {
 *     TradingBookScreen()
 * }
 * ```
 * 
 * ### Example 2: Custom locked content
 * ```kotlin
 * FeatureGate(
 *     feature = Feature.PATTERN_TO_PLAN,
 *     lockedContent = { 
 *         CustomLockedScreen() 
 *     }
 * ) {
 *     PatternToPlanScreen()
 * }
 * ```
 * 
 * ### Example 3: Inline feature lock (dim + overlay)
 * ```kotlin
 * InlineFeatureGate(
 *     feature = Feature.VOICE_ALERTS,
 *     onUpgradeClick = { navController.navigate("paywall") }
 * ) {
 *     VoiceAlertsToggle(enabled = voiceEnabled)
 * }
 * ```
 * 
 * @param feature Feature to gate (contains tier requirements)
 * @param modifier Modifier for customization
 * @param onUpgradeClick Callback when user clicks upgrade button
 * @param lockedContent Custom locked content (optional, uses default if null)
 * @param content Unlocked content to display when user has access
 */
@Composable
fun FeatureGate(
    feature: Feature,
    modifier: Modifier = Modifier,
    onUpgradeClick: (() -> Unit)? = null,
    lockedContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val currentTier by EntitlementManager.currentTier.collectAsState()
    val hasAccess = EntitlementManager.hasFeatureAccess(feature)
    
    if (hasAccess) {
        content()
    } else {
        lockedContent?.invoke() ?: DefaultLockedContent(
            feature = feature,
            currentTier = currentTier,
            modifier = modifier,
            onUpgradeClick = onUpgradeClick
        )
    }
}

/**
 * DefaultLockedContent - Beautiful locked state UI
 * 
 * Premium metallic card with lock icon, feature name, tier badge, and upgrade button.
 * Uses the MetallicCard design system for consistent branding.
 * 
 * @param feature Feature that is locked
 * @param currentTier User's current subscription tier
 * @param modifier Modifier for customization
 * @param onUpgradeClick Callback when user clicks upgrade button
 */
@Composable
private fun DefaultLockedContent(
    feature: Feature,
    currentTier: SubscriptionTier,
    modifier: Modifier = Modifier,
    onUpgradeClick: (() -> Unit)? = null
) {
    MetallicCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(AppSpacing.base)
    ) {
        Column(
            modifier = Modifier
                .padding(AppSpacing.lg)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            // Lock icon
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked feature",
                modifier = Modifier.size(48.dp),
                tint = AppColors.NeonCyan
            )
            
            // Feature name
            MetallicText(
                text = feature.displayName,
                style = AppTypography.titleLarge,
                color = Color.White
            )
            
            // Required tier badge
            TierBadge(tier = feature.requiredTier)
            
            // Description
            Text(
                text = "This feature requires ${feature.requiredTier.displayName}",
                style = AppTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            // Upgrade button (if callback provided)
            onUpgradeClick?.let { onClick ->
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getTierColor(feature.requiredTier)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text("Upgrade to ${feature.requiredTier.displayName}")
                }
            }
        }
    }
}

/**
 * TierBadge - Visual tier indicator
 * 
 * Displays subscription tier with appropriate color, icon, and label.
 * Uses semi-transparent background with colored border for premium look.
 * 
 * @param tier Subscription tier to display
 * @param modifier Modifier for customization
 */
@Composable
fun TierBadge(
    tier: SubscriptionTier,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = getTierColor(tier).copy(alpha = 0.2f),
        shape = RoundedCornerShape(AppRadius.sm),
        border = BorderStroke(1.dp, getTierColor(tier))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacing.md,
                vertical = AppSpacing.xs
            ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getTierIcon(tier),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = getTierColor(tier)
            )
            Text(
                text = tier.displayName,
                style = AppTypography.labelMedium,
                color = getTierColor(tier)
            )
        }
    }
}

/**
 * InlineFeatureGate - For inline feature locking (non-modal)
 * 
 * Displays content with dimmed overlay and lock icon when locked.
 * Shows subtle upgrade prompt below the locked content.
 * Perfect for toggles, buttons, and small UI elements.
 * 
 * @param feature Feature to gate
 * @param showUpgradePrompt Whether to show upgrade text below content
 * @param onUpgradeClick Callback when user clicks upgrade prompt
 * @param content Content to display (will be dimmed if locked)
 */
@Composable
fun InlineFeatureGate(
    feature: Feature,
    showUpgradePrompt: Boolean = true,
    onUpgradeClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val hasAccess = EntitlementManager.hasFeatureAccess(feature)
    
    Column {
        Box(
            modifier = Modifier.then(
                if (!hasAccess) {
                    Modifier.alpha(0.4f) // Dim locked content
                } else {
                    Modifier
                }
            )
        ) {
            content()
            
            if (!hasAccess) {
                // Overlay lock icon
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
        
        if (!hasAccess && showUpgradePrompt) {
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpgradeClick?.invoke() }
                    .padding(AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = getTierColor(feature.requiredTier)
                )
                Text(
                    text = "Requires ${feature.requiredTier.displayName}",
                    style = AppTypography.labelSmall,
                    color = getTierColor(feature.requiredTier)
                )
            }
        }
    }
}

/**
 * Get tier color from design system
 * 
 * @param tier Subscription tier
 * @return Color for the tier (gray, cyan, gold, or purple)
 */
private fun getTierColor(tier: SubscriptionTier): Color {
    return when (tier) {
        SubscriptionTier.FREE -> AppColors.TierFree
        SubscriptionTier.STARTER -> AppColors.TierStarter
        SubscriptionTier.STANDARD -> AppColors.TierStandard
        SubscriptionTier.PRO -> AppColors.TierPro
    }
}

/**
 * Get tier icon from Material Icons
 * 
 * @param tier Subscription tier
 * @return Icon representing the tier
 */
private fun getTierIcon(tier: SubscriptionTier): ImageVector {
    return when (tier) {
        SubscriptionTier.FREE -> Icons.Default.Person
        SubscriptionTier.STARTER -> Icons.Default.Star
        SubscriptionTier.STANDARD -> Icons.Default.StarBorder
        SubscriptionTier.PRO -> Icons.Default.Favorite
    }
}
