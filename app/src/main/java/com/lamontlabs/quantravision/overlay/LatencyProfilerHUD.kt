package com.lamontlabs.quantravision.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

/**
 * LatencyProfilerHUD
 * - Shows real-time ms/frame and target (≤16 ms) bar.
 * - Feed with a sampler: () -> Float returning current avg latency ms.
 * - 0.6 alpha to avoid obscuring the trading app.
 */
@Composable
fun LatencyProfilerHUD(
    sampleMs: () -> Float,
    modifier: Modifier = Modifier,
    targetMs: Float = 16f
) {
    var ms by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            ms = sampleMs().coerceAtLeast(0f)
            delay(250)
        }
    }

    val ratio = (ms / targetMs).coerceIn(0f, 2f) / 2f // 0..1 maps 0..2x
    val (label, color) = when {
        ms <= targetMs     -> "Realtime" to Color(0xFF00E5FF)
        ms <= targetMs*1.5 -> "Okay" to Color(0xFFFFC107)
        else               -> "Slow" to Color(0xFFFF5252)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .alpha(0.9f)
            .background(Color(0xB30B1117))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Latency",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ms.toInt()} ms • $label (≤ ${targetMs.toInt()} ms)",
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = ratio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = color,
                trackColor = Color(0xFF1E2A33)
            )
        }
    }
}
