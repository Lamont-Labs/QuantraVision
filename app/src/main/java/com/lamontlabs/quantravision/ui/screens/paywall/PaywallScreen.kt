package com.lamontlabs.quantravision.ui.screens.paywall

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.components.TierUpgradeCelebration
import com.lamontlabs.quantravision.ui.theme.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

/**
 * Professional Paywall Screen - Tier Comparison & Purchase Flow
 * 
 * Beautiful tier comparison screen with BillingManager integration.
 * Features:
 * - 4 subscription tiers (FREE/STARTER/STANDARD/PRO)
 * - Upgrade pricing for existing subscribers
 * - Feature comparison table
 * - Google Play Billing integration
 * - Error handling and purchase states
 * 
 * Design System:
 * - StaticBrandBackground for consistent branding
 * - MetallicCard for premium card design
 * - NeonText for glowing headlines
 * - MetallicText for chrome text
 * - AppSpacing, AppTypography, AppColors design tokens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onPurchaseComplete: () -> Unit = {},
    navController: NavController? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity ?: error("PaywallScreen requires Activity context")
    
    // Use ViewModelProvider with factory to properly handle lifecycle
    // This ensures ViewModel survives configuration changes (rotation)
    val viewModel: PaywallViewModel = viewModel(
        factory = PaywallViewModelFactory(context.applicationContext)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Track celebration modal state
    var showCelebration by remember { mutableStateOf(false) }
    var celebrationTier by remember { mutableStateOf<SubscriptionTier?>(null) }
    
    LaunchedEffect(uiState.purchaseSuccess) {
        if (uiState.purchaseSuccess != null) {
            // Show celebration modal first
            celebrationTier = uiState.currentTier
            showCelebration = true
        }
    }
    
    // Celebration Modal
    celebrationTier?.let { tier ->
        TierUpgradeCelebration(
            tier = tier,
            isVisible = showCelebration,
            onDismiss = {
                showCelebration = false
                onPurchaseComplete()
            },
            onExploreFeature = { route ->
                showCelebration = false
                navController?.navigate(route)
                onPurchaseComplete()
            }
        )
    }
    
    StaticBrandBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        MetallicText(
                            text = "Choose Your Plan",
                            style = AppTypography.titleLarge
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(AppSpacing.base),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                item {
                    NeonText(
                        text = "Unlock Your Trading Potential",
                        style = AppTypography.headlineLarge,
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = AppColors.NeonCyan,
                        textColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Text(
                        text = "Choose the plan that fits your trading journey",
                        style = AppTypography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                }
                
                uiState.purchaseError?.let { error ->
                    item {
                        ErrorBanner(
                            message = error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                }
                
                uiState.purchaseSuccess?.let { success ->
                    item {
                        SuccessBanner(
                            message = success,
                            onDismiss = { viewModel.clearSuccess() }
                        )
                    }
                }
                
                items(uiState.tiers) { tierOption ->
                    TierCard(
                        tierOption = tierOption,
                        isSelected = uiState.selectedTier == tierOption.tier,
                        isPurchasing = uiState.isPurchasing,
                        onSelect = { viewModel.selectTier(tierOption.tier) },
                        onPurchase = {
                            activity?.let { viewModel.purchaseTier(it, tierOption.tier) }
                        }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    FeatureComparisonTable()
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text(
                        text = "One-time payment • Lifetime access • No subscriptions",
                        style = AppTypography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                }
            }
        }
    }
}

/**
 * TierCard - Individual subscription tier card
 * 
 * Displays tier name, price, features, and purchase button.
 * Highlights selected tier with border.
 * Shows upgrade pricing and discount badges when applicable.
 */
@Composable
private fun TierCard(
    tierOption: PaywallViewModel.TierOption,
    isSelected: Boolean,
    isPurchasing: Boolean,
    onSelect: () -> Unit,
    onPurchase: () -> Unit
) {
    val canPurchase = !tierOption.isCurrent
    MetallicCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        2.dp,
                        getTierColor(tierOption.tier),
                        RoundedCornerShape(AppRadius.md)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = canPurchase,
                onClick = { if (canPurchase) onSelect() }
            )
            .semantics {
                contentDescription = buildString {
                    append("${tierOption.tier.displayName} tier for ${tierOption.price}. ")
                    append("Features: ${tierOption.features.joinToString(", ")}. ")
                    if (!canPurchase) append("Current plan. ")
                    if (tierOption.isRecommended) append("Recommended.")
                }
            },
        elevation = if (isSelected) AppElevation.highest else AppElevation.medium
    ) {
        Column(
            modifier = Modifier
                .padding(AppSpacing.lg)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetallicText(
                            text = tierOption.tier.displayName.uppercase(),
                            style = AppTypography.titleLarge,
                            enableGlow = tierOption.isRecommended,
                            color = if (tierOption.isRecommended) AppColors.NeonCyan else AppColors.MetallicChrome
                        )
                    }
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tierOption.isRecommended) {
                            Surface(
                                color = AppColors.NeonGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(AppRadius.xs),
                                border = BorderStroke(1.dp, AppColors.NeonGold)
                            ) {
                                Text(
                                    text = "RECOMMENDED",
                                    modifier = Modifier.padding(
                                        horizontal = AppSpacing.sm,
                                        vertical = AppSpacing.xxs
                                    ),
                                    style = AppTypography.labelSmall,
                                    color = AppColors.NeonGold
                                )
                            }
                        }
                        
                        if (tierOption.isUpgrade) {
                            Surface(
                                color = AppColors.Success.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(AppRadius.xs),
                                border = BorderStroke(1.dp, AppColors.Success)
                            ) {
                                Text(
                                    text = "UPGRADE DISCOUNT",
                                    modifier = Modifier.padding(
                                        horizontal = AppSpacing.sm,
                                        vertical = AppSpacing.xxs
                                    ),
                                    style = AppTypography.labelSmall,
                                    color = AppColors.Success
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    
                    if (tierOption.isCurrent) {
                        Text(
                            text = "Current Plan",
                            style = AppTypography.labelMedium,
                            color = AppColors.Success
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    if (tierOption.originalPrice != null) {
                        Text(
                            text = tierOption.originalPrice,
                            style = AppTypography.bodyMedium,
                            color = Color.White.copy(alpha = 0.5f),
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    MetallicText(
                        text = tierOption.price,
                        style = AppTypography.headlineMedium,
                        color = getTierColor(tierOption.tier)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            tierOption.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = getTierColor(tierOption.tier)
                    )
                    Text(
                        text = feature,
                        style = AppTypography.bodyMedium,
                        color = Color.White
                    )
                }
            }
            
            if (!tierOption.isCurrent) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Button(
                    onClick = onPurchase,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPurchasing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getTierColor(tierOption.tier),
                        disabledContainerColor = getTierColor(tierOption.tier).copy(alpha = 0.5f)
                    )
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text(
                        text = if (tierOption.tier == SubscriptionTier.FREE) {
                            "Already Free"
                        } else if (tierOption.isUpgrade) {
                            "Upgrade to ${tierOption.tier.displayName}"
                        } else {
                            "Get ${tierOption.tier.displayName}"
                        }
                    )
                }
            }
        }
    }
}

/**
 * FeatureComparisonTable - Detailed feature matrix
 * 
 * Shows which features are available in which tiers.
 * Compact table format for easy comparison.
 */
@Composable
private fun FeatureComparisonTable() {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.lg)) {
            MetallicText(
                text = "Feature Comparison",
                style = AppTypography.titleMedium
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Feature",
                    modifier = Modifier.weight(2f),
                    style = AppTypography.labelMedium,
                    color = Color.White
                )
                Text(
                    text = "FREE",
                    modifier = Modifier.weight(1f),
                    style = AppTypography.labelSmall,
                    color = AppColors.TierFree
                )
                Text(
                    text = "START",
                    modifier = Modifier.weight(1f),
                    style = AppTypography.labelSmall,
                    color = AppColors.TierStarter
                )
                Text(
                    text = "STD",
                    modifier = Modifier.weight(1f),
                    style = AppTypography.labelSmall,
                    color = AppColors.TierStandard
                )
                Text(
                    text = "PRO",
                    modifier = Modifier.weight(1f),
                    style = AppTypography.labelSmall,
                    color = AppColors.TierPro
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            
            ComparisonRow("Patterns", "10", "25", "50", "109")
            ComparisonRow("Highlights", "3/day", "∞", "∞", "∞")
            ComparisonRow("Regime Nav", "—", "—", "✓", "✓")
            ComparisonRow("Guardrails", "—", "—", "✓", "✓")
            ComparisonRow("Trading Book", "—", "—", "✓", "✓")
            ComparisonRow("Pattern→Plan", "—", "—", "—", "✓")
            ComparisonRow("Scan Learning", "—", "—", "—", "✓")
            ComparisonRow("Voice Alerts", "—", "—", "—", "✓")
            ComparisonRow("Proof Capsules", "—", "—", "—", "✓")
        }
    }
}

/**
 * ComparisonRow - Single row in feature comparison table
 */
@Composable
private fun ComparisonRow(
    feature: String,
    free: String,
    starter: String,
    standard: String,
    pro: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.xs)
    ) {
        Text(
            text = feature,
            modifier = Modifier.weight(2f),
            style = AppTypography.bodySmall,
            color = Color.White
        )
        Text(
            text = free,
            modifier = Modifier.weight(1f),
            style = AppTypography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = starter,
            modifier = Modifier.weight(1f),
            style = AppTypography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = standard,
            modifier = Modifier.weight(1f),
            style = AppTypography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = pro,
            modifier = Modifier.weight(1f),
            style = AppTypography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

/**
 * ErrorBanner - Error message display
 */
@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Error.copy(alpha = 0.2f),
        shape = RoundedCornerShape(AppRadius.sm),
        border = BorderStroke(1.dp, AppColors.Error)
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.md)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.Error,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Dismiss",
                    tint = AppColors.Error
                )
            }
        }
    }
}

/**
 * SuccessBanner - Success message display
 */
@Composable
private fun SuccessBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AppColors.Success.copy(alpha = 0.2f),
        shape = RoundedCornerShape(AppRadius.sm),
        border = BorderStroke(1.dp, AppColors.Success)
    ) {
        Row(
            modifier = Modifier
                .padding(AppSpacing.md)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = AppColors.Success,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Dismiss",
                    tint = AppColors.Success
                )
            }
        }
    }
}

/**
 * Get tier color from design system
 * Maps subscription tier to its visual color identifier
 */
private fun getTierColor(tier: SubscriptionTier): Color {
    return when (tier) {
        SubscriptionTier.FREE -> AppColors.TierFree
        SubscriptionTier.STARTER -> AppColors.TierStarter
        SubscriptionTier.STANDARD -> AppColors.TierStandard
        SubscriptionTier.PRO -> AppColors.TierPro
    }
}
