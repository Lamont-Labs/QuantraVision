package com.lamontlabs.quantravision.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * FirstRunTour
 * Step-based guide after installation.
 * Explains overlays, privacy, and determinism in plain language.
 */
@Composable
fun FirstRunTour(onFinish: () -> Unit) {
    val steps = listOf(
        "QuantraVision overlays your charts to highlight patterns.",
        "All analysis happens locally — nothing leaves your device.",
        "Every detection is logged and reproducible.",
        "This tool is educational only. You remain in control.",
        "You're ready to explore."
    )
    var index by remember { mutableStateOf(0) }

    Box(
        Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(steps[index], style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    if (index < steps.lastIndex) index++ else onFinish()
                }) {
                    Text(if (index < steps.lastIndex) "Next" else "Finish")
                }
            }
        }
    }
}
