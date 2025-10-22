package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PatternCard(match: PatternMatch, onClick: (() -> Unit)? = null) {
    val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(match.timestamp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Column {
            Text(match.patternName, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Confidence: ${(match.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
            Text("Time: $time", style = MaterialTheme.typography.bodySmall)
        }
    }
}
