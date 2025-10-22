package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Pattern Explanation", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Pattern: ${match.patternName}", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("Timeframe: ${match.timeframe}")
        Text("Consensus: ${"%.3f".format(match.consensusScore)}")
        Text("Calibrated Confidence: ${"%.3f".format(match.confidence)}  (threshold ${"%.2f".format(threshold)})")
        Text("Scale used: ${"%.2f".format(scale)}x")
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))
        Text("Definition", style = MaterialTheme.typography.titleMedium)
        Text(templateDescription)
        Spacer(Modifier.height(12.dp))
        Text("Deterministic Criteria", style = MaterialTheme.typography.titleMedium)
        Text(
            "- Template correlation ≥ threshold\n" +
            "- Multi-scale consensus agrees near median scale\n" +
            "- Temporal stability not decaying\n" +
            "- Conflict resolver favored this geometry"
        )
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { Text("Close") }
        }
    }
}
