package com.lamontlabs.quantravision.ui

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
import com.lamontlabs.quantravision.entitlements.EntitlementManager
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.components.InlineFeatureGate
import com.lamontlabs.quantravision.ui.components.SectionHeader
import com.lamontlabs.quantravision.ui.components.TierBadge
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.viewmodels.SettingsViewModel

/**
 * SettingsScreen - App configuration and account management
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToPaywall: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel(context) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    StaticBrandBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.base)
        ) {
            NeonText(
                text = "Settings",
                style = AppTypography.headlineLarge
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            SectionHeader(title = "Account")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Current Plan",
                                style = AppTypography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            TierBadge(tier = uiState.currentTier)
                        }
                        
                        Button(onClick = onNavigateToPaywall) {
                            Icon(Icons.Default.Star, contentDescription = "Upgrade")
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text("Upgrade")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            SectionHeader(title = "Alerts")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    SettingToggle(
                        title = "Visual Alerts",
                        description = "Show pattern detection overlays",
                        checked = true,
                        enabled = true,
                        onCheckedChange = {}
                    )
                    
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    InlineFeatureGate(
                        feature = Feature.HAPTIC_ALERTS,
                        onUpgradeClick = onNavigateToPaywall
                    ) {
                        SettingToggle(
                            title = "Haptic Alerts",
                            description = "Vibrate on pattern detection",
                            checked = uiState.hapticsEnabled,
                            enabled = EntitlementManager.hasFeatureAccess(Feature.HAPTIC_ALERTS),
                            onCheckedChange = { viewModel.toggleHaptics(it) }
                        )
                    }
                    
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    InlineFeatureGate(
                        feature = Feature.VOICE_ALERTS,
                        onUpgradeClick = onNavigateToPaywall
                    ) {
                        SettingToggle(
                            title = "Voice Alerts",
                            description = "Speak pattern names aloud",
                            checked = uiState.voiceAlertsEnabled,
                            enabled = EntitlementManager.hasFeatureAccess(Feature.VOICE_ALERTS),
                            onCheckedChange = { viewModel.toggleVoiceAlerts(it) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            
            SectionHeader(title = "About")
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.md)) {
                    InfoRow("Version", uiState.appVersion)
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    InfoRow("Overlay Permission", if (uiState.hasOverlayPermission) "Granted" else "Not Granted")
                }
            }
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTypography.titleSmall,
                color = Color.White.copy(alpha = if (enabled) 1f else 0.4f)
            )
            Text(
                text = description,
                style = AppTypography.bodySmall,
                color = Color.White.copy(alpha = if (enabled) 0.7f else 0.3f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.semantics {
                contentDescription = "$title is ${if (checked) "enabled" else "disabled"}"
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTypography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = AppTypography.bodyMedium,
            color = Color.White
        )
    }
}
