package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.R

/**
 * OnboardingWizard
 * First-run walkthrough explaining purpose, privacy, and disclaimer.
 * Deterministic; no network or analytics.
 */
@Composable
fun OnboardingWizard(onFinish: () -> Unit) {
    var page by remember { mutableStateOf(0) }

    val screens = listOf(
        Triple("Welcome to QuantraVision", "Visual AI companion for traders. See patterns your platform can't.", R.drawable.ic_eye),
        Triple("Privacy First", "Runs fully offline. No data leaves your device.", R.drawable.ic_lock),
        Triple("Illustrative Only", "QuantraVision never trades or advises. All detections are educational.", R.drawable.ic_warning),
        Triple("Proof by Design", "Every session is logged deterministically for verifiable results.", R.drawable.ic_proof)
    )

    val (title, desc, image) = screens[page]

    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(id = image), contentDescription = null, modifier = Modifier.size(128.dp))
            Spacer(Modifier.height(24.dp))
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(desc, fontSize = 16.sp, lineHeight = 22.sp)
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = {
                    if (page < screens.lastIndex) page++ else onFinish()
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(if (page < screens.lastIndex) "Next" else "Finish")
            }
        }
    }
}
