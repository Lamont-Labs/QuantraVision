package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * HelpScreen
 * Renders offline markdown help from /files/help/.
 * Deterministic; never loads remote docs.
 */
@Composable
fun HelpScreen(helpFiles: List<File>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Documentation") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            helpFiles.forEach { f ->
                Text(f.nameWithoutExtension, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(f.readText())
                Spacer(Modifier.height(24.dp))
                Divider()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
