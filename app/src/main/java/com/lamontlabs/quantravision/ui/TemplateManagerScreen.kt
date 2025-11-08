package com.lamontlabs.quantravision.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.templates.PatternCatalog

/**
 * TemplateManagerScreen
 * - Searchable list of all templates
 * - Toggle enable/disable per pattern
 * - Bulk enable/disable
 * - 100% offline and deterministic
 */
@Composable
fun TemplateManagerScreen(
    context: Context,
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var refresh by remember { mutableStateOf(0) }

    val items = remember(query.text, refresh) {
        PatternCatalog.list(context, query.text)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Template Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    TextButton(onClick = {
                        PatternCatalog.enableAll(context); refresh++
                    }) { Text("Enable All") }
                    TextButton(onClick = {
                        PatternCatalog.disableAll(context); refresh++
                    }) { Text("Disable All") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search patterns") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Text("Total: ${items.size}   Enabled: ${items.count { it.enabled }}", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(8.dp))
            Divider()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items, key = { it.id }) { entry ->
                    TemplateRow(
                        name = entry.name,
                        id = entry.id,
                        enabled = entry.enabled
                    ) {
                        PatternCatalog.setEnabled(context, entry.id, it)
                        refresh++
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateRow(
    name: String,
    id: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    MenuItemCard(
        title = name,
        subtitle = id,
        onClick = { onToggle(!enabled) },
        icon = {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = if (enabled) NeonCyan else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        },
        badge = if (enabled) "✓ ENABLED" else null,
        badgeColor = if (enabled) Color(0xFF00FF88) else Color.Gray,
        showArrow = false
    )
}
