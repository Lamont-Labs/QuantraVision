package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * HelpViewer
 * Simple markdown/text asset viewer.
 */
@Composable
fun HelpViewer(context: Context, asset: String, onBack: () -> Unit) {
    val text = readAsset(context, asset)
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(asset) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } })
    }) { pad ->
        Box(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text(AnnotatedString(text))
        }
    }
}

private fun readAsset(context: Context, name: String): String =
    runCatching {
        context.assets.open("legal/$name").use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }
    }.getOrElse { "Document not found: $name" }
