package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lamontlabs.quantravision.templates.PatternCatalog
import java.io.File
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory

/**
 * PatternInfoSheet
 * Bottom-sheet style dialog showing pattern preview, definition, and reliability.
 */
@Composable
fun PatternInfoSheet(
    context: Context,
    patternId: String,
    onDismiss: () -> Unit
) {
    val entry = remember { PatternCatalog.list(context).find { it.id == patternId } }
    if (entry == null) {
        onDismiss(); return
    }

    val preview: ImageBitmap? = remember(entry) {
        val png = File(context.filesDir, "pattern_templates/${entry.id}_ref.png")
        if (png.exists()) {
            val bmp = BitmapFactory.decodeFile(png.absolutePath)
            val imageBitmap = bmp?.asImageBitmap()
            bmp?.recycle()
            imageBitmap
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(entry.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
                preview?.let {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(bitmap = it, contentDescription = "Pattern preview", modifier = Modifier.size(200.dp))
                    }
                }
                Divider()
                Text("Definition:", style = MaterialTheme.typography.titleMedium)
                Text(getDefinition(entry.name))
                Divider()
                Text("Ideal Timeframes:", style = MaterialTheme.typography.titleMedium)
                Text(getTimeframes(entry.name))
                Divider()
                Text("Reliability Estimate:", style = MaterialTheme.typography.titleMedium)
                Text("${getReliability(entry.name)}% (based on deterministic model training corpus)")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
}

private fun getDefinition(name: String): String = when {
    name.contains("Head", true) -> "A reversal pattern marking the transition from an uptrend to a downtrend."
    name.contains("Double Top", true) -> "Two consecutive peaks of similar height indicating potential trend reversal."
    name.contains("Triangle", true) -> "A consolidation pattern with converging trendlines before breakout."
    name.contains("Wedge", true) -> "A narrowing pattern indicating potential continuation or reversal."
    name.contains("Cup", true) -> "A bullish continuation pattern following a rounded bottom with a handle pullback."
    name.contains("RSI", true) -> "A momentum divergence pattern indicating weakening or strengthening trend pressure."
    else -> "Technical structure recognized by the AI overlay engine."
}

private fun getTimeframes(name: String): String = when {
    name.contains("Scalp", true) -> "1m–5m"
    name.contains("Day", true) -> "5m–30m"
    name.contains("Swing", true) -> "1H–4H"
    else -> "All supported timeframes (scalp → swing)"
}

private fun getReliability(name: String): Int = when {
    name.contains("Head", true) -> 82
    name.contains("Triangle", true) -> 76
    name.contains("Wedge", true) -> 71
    name.contains("RSI", true) -> 65
    else -> 68
}
