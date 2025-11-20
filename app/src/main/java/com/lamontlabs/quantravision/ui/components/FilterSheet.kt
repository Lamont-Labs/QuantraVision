package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.detection.filtering.*
import com.lamontlabs.quantravision.ui.MenuItemCard
import com.lamontlabs.quantravision.ui.NeonCyan
import com.lamontlabs.quantravision.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    currentFilter: PatternFilter,
    onApply: (PatternFilter) -> Unit,
    onDismiss: () -> Unit,
    patternCount: Int = 0
) {
    var filter by remember { mutableStateOf(currentFilter) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppColors.Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter Patterns",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$patternCount pattern(s) match current filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Pattern Type",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                PatternType.values().forEach { type ->
                    val isSelected = filter.patternTypes.contains(type)
                    MenuItemCard(
                        title = type.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = {
                            filter = if (isSelected) {
                                filter.copy(patternTypes = filter.patternTypes - type)
                            } else {
                                filter.copy(patternTypes = filter.patternTypes + type)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        badge = if (isSelected) "✓ SELECTED" else null,
                        badgeColor = if (isSelected) NeonCyan else Color.Gray,
                        showArrow = false,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Confidence Level",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ConfidenceLevel.values().forEach { level ->
                    val isSelected = filter.confidenceLevels.contains(level)
                    MenuItemCard(
                        title = level.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = {
                            filter = if (isSelected) {
                                filter.copy(confidenceLevels = filter.confidenceLevels - level)
                            } else {
                                filter.copy(confidenceLevels = filter.confidenceLevels + level)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        badge = if (isSelected) "✓ SELECTED" else null,
                        badgeColor = if (isSelected) NeonCyan else Color.Gray,
                        showArrow = false,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Timeframes",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val timeframes = listOf("1m", "5m", "15m", "1h", "4h", "daily")
                timeframes.forEach { timeframe ->
                    val isSelected = filter.timeframes.contains(timeframe)
                    MenuItemCard(
                        title = timeframe.uppercase(),
                        onClick = {
                            filter = if (isSelected) {
                                filter.copy(timeframes = filter.timeframes - timeframe)
                            } else {
                                filter.copy(timeframes = filter.timeframes + timeframe)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (isSelected) NeonCyan else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        badge = if (isSelected) "✓ SELECTED" else null,
                        badgeColor = if (isSelected) NeonCyan else Color.Gray,
                        showArrow = false,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        filter = PatternFilter.default()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
                
                Button(
                    onClick = {
                        onApply(filter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
