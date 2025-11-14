package com.lamontlabs.quantravision.ui.screens.intelligence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.components.*
import com.lamontlabs.quantravision.ui.theme.*
import com.lamontlabs.quantravision.entitlements.Feature
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground

@Composable
fun ProofCapsulesScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNavigateToPaywall: () -> Unit = {}
) {
    FeatureGate(
        feature = Feature.PROOF_CAPSULES,
        onUpgradeClick = onNavigateToPaywall
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Proof Capsules") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            StaticBrandBackground {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(AppSpacing.base)
                ) {
                    NeonText(
                        text = "Detection Audit Trail",
                        style = AppTypography.headlineMedium
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    Text(
                        text = "Cryptographically signed proof of every pattern detection with full explainability and audit trail.",
                        style = AppTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        items(
                            listOf(
                                ProofCapsuleData("Bull Flag", 0.89f, "2 min ago", true),
                                ProofCapsuleData("Head & Shoulders", 0.76f, "15 min ago", true),
                                ProofCapsuleData("Double Bottom", 0.92f, "1 hour ago", true),
                                ProofCapsuleData("Cup & Handle", 0.81f, "3 hours ago", false)
                            )
                        ) { capsule ->
                            ProofCapsuleCard(capsule)
                        }
                    }
                }
            }
        }
    }
}

data class ProofCapsuleData(
    val patternName: String,
    val confidence: Float,
    val timestamp: String,
    val signed: Boolean
)

@Composable
private fun ProofCapsuleCard(capsule: ProofCapsuleData) {
    MetallicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(AppSpacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = capsule.patternName,
                        style = AppTypography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = capsule.timestamp,
                        style = AppTypography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    if (capsule.signed) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = "Signed",
                            tint = AppColors.Success,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${(capsule.confidence * 100).toInt()}%",
                        style = AppTypography.titleSmall,
                        color = AppColors.NeonCyan
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Visibility, null)
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text("View Proof Details")
            }
        }
    }
}
