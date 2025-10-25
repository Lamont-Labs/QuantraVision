package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * HowToUseScreen
 * - Minimal instruction guide within the app
 * - Clarifies order of operations for correct overlay usage
 * - Keeps UI unobtrusive and easy to reference
 */
@Composable
fun HowToUseScreen(onBack: () -> Unit) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A1015)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(scroll)
                .fillMaxWidth()
                .background(Color(0xFF121820), shape = MaterialTheme.shapes.medium)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "How to Use QuantraVision",
                color = Color(0xFF00E5FF),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            val steps = listOf(
                "1. Open QuantraVision first.",
                "2. Grant overlay permission when prompted.",
                "3. Open your trading application (e.g., TradingView, MetaTrader, Webull).",
                "4. QuantraVision will appear as a small glowing button on screen.",
                "5. Tap the overlay button to activate live pattern detection.",
                "6. Detected chart patterns will glow on-screen with confidence scores.",
                "7. Tap a highlighted pattern for a breakdown and tradeability rating.",
                "8. Use the settings icon to switch chart type (candlestick, bar, line).",
                "9. Adjust overlay transparency and color from the Appearance menu.",
                "10. Free tier allows 5 detections. Upgrade anytime from the Upgrade screen."
            )

            steps.forEach {
                Text(
                    it,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))
            TextButton(onClick = onBack) {
                Text("Back", color = Color(0xFF00E5FF))
            }
        }
    }
}
