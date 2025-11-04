package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.templates.PatternCatalog
import com.lamontlabs.quantravision.templates.TemplateEditor

/**
 * TemplateEditorScreen
 * Per-pattern editor for threshold and scale parameters.
 */
@Composable
fun TemplateEditorScreen(context: Context, onBack: () -> Unit) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    val items = remember(query.text) { PatternCatalog.list(context, query.text) }

    var selected by remember { mutableStateOf<String?>(null) }
    var threshold by remember { mutableStateOf("0.72") }
    var min by remember { mutableStateOf("0.60") }
    var max by remember { mutableStateOf("1.80") }
    var stride by remember { mutableStateOf("0.15") }
    var status by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Template Editor",
                        style = MaterialTheme.typography.titleLarge.copy(
                            shadow = CyanGlowShadow
                        )
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back") 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = ElectricCyan
                )
            )
        },
        containerColor = DeepNavyBackground
    ) { padding ->
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = query, 
                onValueChange = { query = it }, 
                label = { Text("Search") }, 
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    focusedLabelColor = ElectricCyan
                )
            )
            ExposedDropdownMenuBox(expanded = true, onExpandedChange = {}) {
                // Simple list pick
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = threshold, 
                    onValueChange = { threshold = it }, 
                    label = { Text("Threshold") }, 
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        focusedLabelColor = ElectricCyan
                    )
                )
                OutlinedTextField(
                    value = min, 
                    onValueChange = { min = it }, 
                    label = { Text("Scale Min") }, 
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        focusedLabelColor = ElectricCyan
                    )
                )
                OutlinedTextField(
                    value = max, 
                    onValueChange = { max = it }, 
                    label = { Text("Scale Max") }, 
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        focusedLabelColor = ElectricCyan
                    )
                )
                OutlinedTextField(
                    value = stride, 
                    onValueChange = { stride = it }, 
                    label = { Text("Stride") }, 
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        focusedLabelColor = ElectricCyan
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val id = selected ?: items.firstOrNull()?.id
                        if (id != null) {
                            val ok = TemplateEditor.apply(
                                context,
                                id,
                                TemplateEditor.Edit(
                                    threshold = threshold.toDoubleOrNull(),
                                    scaleMin = min.toDoubleOrNull(),
                                    scaleMax = max.toDoubleOrNull(),
                                    scaleStride = stride.toDoubleOrNull()
                                )
                            )
                            status = if (ok) "Saved edits for $id" else "Failed to edit $id"
                        } else status = "No template selected"
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan
                    )
                ) { Text("Save Changes", color = DeepNavyBackground) }
                status?.let { Text(it, color = CrispWhite) }
            }
            Divider(color = ElectricCyan.copy(alpha = 0.3f))
            Text(
                "Tip: use Template Manager to enable/disable patterns globally.", 
                style = MaterialTheme.typography.bodySmall,
                color = MetallicSilver
            )
        }
    }
}
