package com.lamontlabs.quantravision.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.BillingManager

class OnboardingActivity : ComponentActivity() {
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        billingManager = BillingManager(this)
        billingManager.initialize()

        setContent {
            QuantraVisionIntroScreen(onContinue = {
                startActivity(Intent(this@OnboardingActivity, OverlayGuide::class.java))
                finish()
            }, onPurchase = { sku ->
                billingManager.launchPurchase(sku)
            })
        }
    }
}

@Composable
fun QuantraVisionIntroScreen(onContinue: () -> Unit, onPurchase: (String) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("QuantraVision", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))
            Text("Visual AI Overlay for Traders — See patterns directly on your charts.")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onContinue) { Text("Start Free Mode (2 Highlights/day)") }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onPurchase("standard_tier") }) { Text("Upgrade to Standard – \$19.99") }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onPurchase("pro_tier") }) { Text("Upgrade to Pro – \$49.99") }
        }
    }
}
