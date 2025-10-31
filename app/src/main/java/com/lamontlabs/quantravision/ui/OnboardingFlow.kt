package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lamontlabs.quantravision.system.PermissionHelper

/**
 * OnboardingFlow
 * Runs on first launch to guide user through overlay, privacy and disclaimer acceptance.
 * CRITICAL: Dialog CANNOT be dismissed without accepting disclaimer (legal requirement)
 */
@Composable
fun OnboardingFlow(context: Context, onComplete: () -> Unit) {
    // CRITICAL: Save and restore onboarding progress to handle force-close scenarios
    val prefs = remember { context.getSharedPreferences("qv_onboarding_prefs", Context.MODE_PRIVATE) }
    var step by remember { mutableStateOf(loadOnboardingProgress(prefs)) }
    
    // Save progress whenever step changes
    DisposableEffect(step) {
        saveOnboardingProgress(prefs, step)
        onDispose { }
    }

    Dialog(
        onDismissRequest = { /* DO NOTHING - prevent dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (step) {
                    0 -> {
                        Text("Welcome to QuantraVision", style = MaterialTheme.typography.headlineSmall)
                        Text("This app highlights technical chart patterns directly on your screen, privately and deterministically.")
                        Button(onClick = { step++ }) { Text("Continue") }
                    }
                    1 -> {
                        Text("Permissions", style = MaterialTheme.typography.titleLarge)
                        Text("We require overlay permission to display highlights and storage to save proof exports.")
                        Button(onClick = { PermissionHelper.requestAll(context); step++ }) { Text("Grant") }
                    }
                    2 -> {
                        Text("⚠️ Legal Disclaimer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                        Column(
                            Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "NOT FINANCIAL ADVICE\n\n" +
                                "QuantraVision is an EDUCATIONAL tool only. It does NOT provide financial advice, investment recommendations, or trading signals.\n\n" +
                                "⚠️ TRADING IS RISKY. YOU CAN LOSE MONEY.\n\n" +
                                "• AI pattern detection may produce false positives/negatives\n" +
                                "• Past performance does NOT predict future results\n" +
                                "• You are SOLELY responsible for all trading decisions\n" +
                                "• Consult a licensed financial advisor before investing\n\n" +
                                "LIMITATION OF LIABILITY:\n" +
                                "Lamont Labs is NOT liable for any trading losses, missed opportunities, or financial damages arising from use of this app.\n\n" +
                                "By clicking 'I Agree', you acknowledge:\n" +
                                "1. You have read the full Terms of Use\n" +
                                "2. You understand this is NOT financial advice\n" +
                                "3. You accept all trading risks\n" +
                                "4. You will not hold Lamont Labs liable for losses",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                DisclaimerManager.setAccepted(context, true)
                                step++ 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) { 
                            Text("I Understand the Risks & Agree") 
                        }
                    }
                    else -> {
                        Text("Setup complete", style = MaterialTheme.typography.titleLarge)
                        Button(onClick = {
                            // Clear onboarding progress on completion
                            prefs.edit().clear().apply()
                            onComplete()
                        }) { Text("Start") }
                    }
                }
            }
        }
    }
}

/**
 * Load saved onboarding progress (for force-close recovery)
 */
private fun loadOnboardingProgress(prefs: android.content.SharedPreferences): Int {
    val savedStep = prefs.getInt("onboarding_step", 0)
    android.util.Log.d("OnboardingFlow", "Loaded onboarding progress: step $savedStep")
    return savedStep
}

/**
 * Save onboarding progress (for force-close recovery)
 */
private fun saveOnboardingProgress(prefs: android.content.SharedPreferences, step: Int) {
    prefs.edit().putInt("onboarding_step", step).apply()
    android.util.Log.d("OnboardingFlow", "Saved onboarding progress: step $step")
}
