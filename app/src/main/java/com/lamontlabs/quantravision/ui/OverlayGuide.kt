package com.lamontlabs.quantravision.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class OverlayGuide : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OverlayInstructionsScreen(
                onLaunchTradingApp = {
                    val intent = packageManager.getLaunchIntentForPackage("com.tradingview.tradingviewapp")
                        ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tradingview.com/"))
                    startActivity(intent)
                    finish()
                }
            )
        }
    }
}

@Composable
fun OverlayInstructionsScreen(onLaunchTradingApp: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("How to Use QuantraVision", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            Text(
                """
                1. Launch QuantraVision first.
                2. Grant overlay permission when prompted.
                3. Then open your trading app (e.g. TradingView, MetaTrader, Webull).
                4. QuantraVision will appear as a transparent layer on top, highlighting detected chart patterns.
                5. Tap the floating eye icon to pause or resume detection.
                """.trimIndent()
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onLaunchTradingApp) {
                Text("Open Trading App")
            }
        }
    }
}
