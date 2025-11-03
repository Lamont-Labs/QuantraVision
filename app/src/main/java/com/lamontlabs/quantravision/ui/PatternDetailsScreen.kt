package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.explanation

/**
 * PatternDetailsScreen
 * Clean breakdown of a detection with full transparency and disclaimer.
 */
@Composable
fun PatternDetailsScreen(match: PatternMatch, imageRes: Int, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(match.patternName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(android.R.drawable.ic_menu_revert), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painterResource(id = imageRes), contentDescription = match.patternName, modifier = Modifier.size(240.dp))
            Spacer(Modifier.height(16.dp))
            Text("Confidence: ${(match.confidence * 100).toInt()}%", fontWeight = FontWeight.Medium)
            Text("Timeframe: ${match.timeframe}")
            Text("Detected: ${match.timestamp}")
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(8.dp))
            Text("⚠ Illustrative Only — Not Financial Advice", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(24.dp))
            Text("Explanation", fontWeight = FontWeight.Bold)
            Text(match.explanation ?: "Pattern identified deterministically via template matching. No predictive modeling used.")
        }
    }
}
