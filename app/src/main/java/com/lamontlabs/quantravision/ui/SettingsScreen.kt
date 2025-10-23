package com.lamontlabs.quantravision.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.quota.HighlightQuota
import java.io.File

/**
 * SettingsScreen
 * Preferences, quota reset, legal links, version info.
 */
@Composable
fun SettingsScreen(context: Context, onBack: () -> Unit) {
    val scroll = rememberScrollState()
    var resetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") }, navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) }
            })
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("General", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { resetDialog = true }) { Text("Reset Highlight Quota") }
            Button(onClick = { showLegal(context, "PRIVACY_POLICY.html") }) { Text("Privacy Policy") }
            Button(onClick = { showLegal(context, "TERMS_OF_USE.html") }) { Text("Terms of Use") }
            Button(onClick = { showLegal(context, "DISCLAIMER.txt") }) { Text("Disclaimer") }
            Spacer(Modifier.height(8.dp))
            Text("Version 1.1", style = MaterialTheme.typography.bodySmall)
            Text("© 2025 Jesse J. Lamont / Lamont Labs", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (resetDialog) {
        AlertDialog(
            onDismissRequest = { resetDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    File(context.filesDir, "highlight_quota.json").delete()
                    Toast.makeText(context, "Quota reset.", Toast.LENGTH_SHORT).show()
                    resetDialog = false
                }) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { resetDialog = false }) { Text("Cancel") } },
            title = { Text("Reset highlight quota?") },
            text = { Text("You will regain 5 free highlights.") }
        )
    }
}

private fun showLegal(context: Context, assetName: String) {
    // Launch HelpViewer with asset
    Toast.makeText(context, "Open: $assetName", Toast.LENGTH_SHORT).show()
}
