package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.ui.components.PatternCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionListScreen(
    db: PatternDatabase,
    onBack: () -> Unit,
    onShowPaywall: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var detections by remember { mutableStateOf(listOf<PatternMatch>()) }

    LaunchedEffect(Unit) {
        scope.launch { detections = db.patternDao().getAll() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detected Patterns") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (detections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No detections yet. Use 'Start Detection' to scan charts.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(detections) { pattern ->
                        PatternCard(
                            match = pattern,
                            showIntelligence = true,
                            onShowPaywall = onShowPaywall
                        )
                    }
                }
            }
        }
    }
}
