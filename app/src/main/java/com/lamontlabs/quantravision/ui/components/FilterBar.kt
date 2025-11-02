package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.detection.filtering.PatternFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    currentFilter: PatternFilter,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFiltersCount = countActiveFilters(currentFilter)
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = activeFiltersCount > 0,
            onClick = onFilterClick,
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        if (activeFiltersCount > 0) {
                            "Filters ($activeFiltersCount)"
                        } else {
                            "Filters"
                        }
                    )
                }
            },
            leadingIcon = if (activeFiltersCount > 0) {
                {
                    Badge {
                        Text(activeFiltersCount.toString())
                    }
                }
            } else null
        )
        
        if (!currentFilter.patternTypes.contains(com.lamontlabs.quantravision.detection.filtering.PatternType.ALL)) {
            currentFilter.patternTypes.forEach { type ->
                if (type != com.lamontlabs.quantravision.detection.filtering.PatternType.ALL) {
                    FilterChip(
                        selected = true,
                        onClick = onFilterClick,
                        label = { Text(type.name.lowercase().capitalize()) }
                    )
                }
            }
        }
        
        if (!currentFilter.confidenceLevels.contains(com.lamontlabs.quantravision.detection.filtering.ConfidenceLevel.ALL)) {
            currentFilter.confidenceLevels.forEach { level ->
                if (level != com.lamontlabs.quantravision.detection.filtering.ConfidenceLevel.ALL) {
                    FilterChip(
                        selected = true,
                        onClick = onFilterClick,
                        label = { Text("${level.name.lowercase().capitalize()} conf.") }
                    )
                }
            }
        }
        
        if (currentFilter.timeframes.isNotEmpty()) {
            FilterChip(
                selected = true,
                onClick = onFilterClick,
                label = { Text("${currentFilter.timeframes.size} timeframe(s)") }
            )
        }
    }
}

private fun countActiveFilters(filter: PatternFilter): Int {
    var count = 0
    
    if (!filter.patternTypes.contains(com.lamontlabs.quantravision.detection.filtering.PatternType.ALL)) {
        count += filter.patternTypes.size
    }
    
    if (!filter.confidenceLevels.contains(com.lamontlabs.quantravision.detection.filtering.ConfidenceLevel.ALL)) {
        count += filter.confidenceLevels.size
    }
    
    if (filter.timeframes.isNotEmpty()) {
        count += filter.timeframes.size
    }
    
    if (!filter.invalidationStatus.contains(com.lamontlabs.quantravision.detection.filtering.InvalidationStatus.ALL)) {
        count += filter.invalidationStatus.size
    }
    
    return count
}
