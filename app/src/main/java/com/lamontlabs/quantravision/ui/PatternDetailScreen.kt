package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatternDetailScreen(match: PatternMatch, onBack: () -> Unit) {
    val time = remember(match.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(match.timestamp))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(match.patternName) })
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Pattern: ${match.patternName}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Confidence: ${(match.confidence * 100).toInt()}%")
            Spacer(Modifier.height(8.dp))
            Text("Timestamp: $time")
            Spacer(Modifier.height(24.dp))
            Button(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Back")
            }
        }
    }
}
