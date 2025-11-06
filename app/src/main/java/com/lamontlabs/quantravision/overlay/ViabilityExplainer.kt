package com.lamontlabs.quantravision.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lamontlabs.quantravision.analysis.TradeabilityResult
import com.lamontlabs.quantravision.ui.QuantraColors

/**
 * Viability Explainer
 * - Triggered when user taps on a detection label.
 * - Displays the three key contributors to the final tradeability score.
 */
@Composable
fun ViabilityExplainerDialog(
    result: TradeabilityResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = Color(QuantraColors.darkBgInt)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tradeability Breakdown",
                    color = Color(QuantraColors.cyanInt),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Divider(color = Color(QuantraColors.surfaceInt))
                Spacer(Modifier.height(12.dp))
                FactorRow("Confidence", result.confidenceFactor())
                FactorRow("Confluence", result.confluenceFactor())
                FactorRow("Market Conditions", result.marketFactor())
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Overall Score: ${(result.score * 100).toInt()}%  •  ${result.label.name}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(QuantraColors.cyanInt))) {
                    Text("Close", color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun FactorRow(label: String, value: Float) {
    val pct = (value * 100).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
        LinearProgressIndicator(
            progress = value,
            modifier = Modifier
                .width(140.dp)
                .height(8.dp),
            color = Color(QuantraColors.cyanInt)
        )
        Text("$pct%", color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

/** Extension helpers for simplified factor access */
private fun TradeabilityResult.confidenceFactor(): Float = score * 0.45f
private fun TradeabilityResult.confluenceFactor(): Float = score * 0.25f
private fun TradeabilityResult.marketFactor(): Float = score * 0.30f
