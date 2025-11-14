package com.lamontlabs.quantravision.ui.screens.scan

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.SubscriptionTier
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.ScanViewModel

/**
 * ScanScreen - Pattern scanner control and stats
 */
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { ScanViewModel(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StaticBrandBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(AppSpacing.base)
        ) {
            NeonText(
                text = "Pattern Scanner",
                style = AppTypography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                StatCard(
                    title = "Detections",
                    value = "${uiState.detectionCount}",
                    subtitle = "total",
                    modifier = Modifier.weight(1f)
                )
                
                StatCard(
                    title = "Highlights",
                    value = "${uiState.highlightsUsedToday}",
                    subtitle = if (uiState.currentTier == SubscriptionTier.FREE) {
                        "${uiState.highlightsRemaining} left"
                    } else "Unlimited",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            if (uiState.currentTier == SubscriptionTier.PRO) {
                MetallicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.md)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "PRO feature",
                                tint = AppColors.TierPro
                            )
                            Text(
                                text = "AI Scan Learning",
                                style = AppTypography.titleMedium,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        
                        LinearProgressIndicator(
                            progress = uiState.scanLearningProgress / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "${uiState.scanLearningProgress}% learned from your scans",
                            style = AppTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.lg))
            }
            
            Button(
                onClick = {
                    if (uiState.isOverlayActive) {
                        viewModel.stopOverlay()
                    } else if (uiState.hasOverlayPermission) {
                        viewModel.startOverlay()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .semantics {
                        contentDescription = if (uiState.isOverlayActive) {
                            "Stop pattern scanner"
                        } else {
                            "Start pattern scanner"
                        }
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isOverlayActive) AppColors.Error else AppColors.Success
                )
            ) {
                Icon(
                    imageVector = if (uiState.isOverlayActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text(
                    text = if (uiState.isOverlayActive) "Stop Scanner" else "Start Scanner",
                    style = AppTypography.titleMedium
                )
            }
            
            if (!uiState.hasOverlayPermission) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = "⚠️ Overlay permission required to scan charts",
                    style = AppTypography.bodySmall,
                    color = AppColors.Warning,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    MetallicCard(modifier = modifier) {
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
