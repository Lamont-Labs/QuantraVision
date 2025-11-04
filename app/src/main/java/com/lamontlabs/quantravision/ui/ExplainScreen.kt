package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch

/**
 * Offline explanation screen. No network. Shows why a detection qualified.
 * Inputs are deterministic values from the engine and the template YAML.
 */
@Composable
fun ExplainScreen(
    match: PatternMatch,
    templateDescription: String,
    threshold: Double,
    scale: Double,
    onClose: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(DeepNavyBackground)
            .padding(24.dp)
    ) {
        Text(
            "Pattern Explanation", 
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = CyanGlowShadow
            ),
            color = ElectricCyan
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Pattern: ${match.patternName}", 
            style = MaterialTheme.typography.titleLarge,
            color = CrispWhite,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        Text("Timeframe: ${match.timeframe}", color = MetallicSilver)
        Text("Consensus: ${"%.3f".format(match.consensusScore)}", color = MetallicSilver)
        Text(
            "Calibrated Confidence: ${"%.3f".format(match.confidence)}  (threshold ${"%.2f".format(threshold)})",
            color = MetallicSilver
        )
        Text("Scale used: ${"%.2f".format(scale)}x", color = MetallicSilver)
        Spacer(Modifier.height(24.dp))
        Divider(color = ElectricCyan.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text(
            "Definition", 
            style = MaterialTheme.typography.titleMedium,
            color = ElectricCyan,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(templateDescription, color = CrispWhite)
        Spacer(Modifier.height(24.dp))
        Text(
            "Deterministic Criteria", 
            style = MaterialTheme.typography.titleMedium,
            color = ElectricCyan,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "- Template correlation ≥ threshold\n" +
            "- Multi-scale consensus agrees near median scale\n" +
            "- Temporal stability not decaying\n" +
            "- Conflict resolver favored this geometry",
            color = CrispWhite
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { 
                Text("Close", color = ElectricCyan)
            }
        }
    }
}
