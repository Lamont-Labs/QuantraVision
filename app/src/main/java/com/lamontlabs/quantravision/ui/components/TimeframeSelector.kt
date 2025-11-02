package com.lamontlabs.quantravision.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.detection.model.Timeframe
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeframeSelector(
    context: Context,
    selectedTimeframes: Set<Timeframe>,
    onSelectionChanged: (Set<Timeframe>) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    Column(modifier = modifier) {
        Text(
            "Select Timeframes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Timeframe.values()) { timeframe ->
                FilterChip(
                    selected = timeframe in selectedTimeframes,
                    onClick = {
                        val newSelection = if (timeframe in selectedTimeframes) {
                            selectedTimeframes - timeframe
                        } else {
                            selectedTimeframes + timeframe
                        }
                        onSelectionChanged(newSelection)
                        scope.launch {
                            saveTimeframeSelection(context, newSelection)
                        }
                    },
                    label = { Text(timeframe.displayName) }
                )
            }
        }
        
        if (selectedTimeframes.isEmpty()) {
            Text(
                "Select at least one timeframe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private suspend fun saveTimeframeSelection(context: Context, timeframes: Set<Timeframe>) {
    try {
        val file = File(context.filesDir, "timeframe_selection.json")
        val jsonArray = JSONArray()
        timeframes.forEach { jsonArray.put(it.name) }
        file.writeText(jsonArray.toString(2))
    } catch (e: Exception) {
        timber.log.Timber.e(e, "Failed to save timeframe selection")
    }
}

fun loadTimeframeSelection(context: Context): Set<Timeframe> {
    return try {
        val file = File(context.filesDir, "timeframe_selection.json")
        if (!file.exists()) {
            setOf(Timeframe.M5, Timeframe.H1)
        } else {
            val jsonArray = JSONArray(file.readText())
            val timeframes = mutableSetOf<Timeframe>()
            for (i in 0 until jsonArray.length()) {
                val name = jsonArray.getString(i)
                Timeframe.fromString(name)?.let { timeframes.add(it) }
            }
            if (timeframes.isEmpty()) setOf(Timeframe.M5, Timeframe.H1) else timeframes
        }
    } catch (e: Exception) {
        timber.log.Timber.e(e, "Failed to load timeframe selection")
        setOf(Timeframe.M5, Timeframe.H1)
    }
}
