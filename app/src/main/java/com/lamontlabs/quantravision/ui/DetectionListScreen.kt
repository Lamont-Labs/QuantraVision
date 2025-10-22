package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import kotlinx.coroutines.launch

@Composable
fun DetectionListScreen(db: PatternDatabase) {
    val scope = rememberCoroutineScope()
    var detections by remember { mutableStateOf(listOf<PatternMatch>()) }

    LaunchedEffect(Unit) {
        scope.launch { detections = db.patternDao().getAll() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Detected Patterns", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (detections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No detections yet.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(detections) { pattern ->
                    PatternCard(pattern)
                }
            }
        }
    }
}
