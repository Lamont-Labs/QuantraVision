package com.lamontlabs.quantravision.ui.screens.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.lamontlabs.quantravision.ui.MenuItemCard
import com.lamontlabs.quantravision.ui.NeonCyan
import timber.log.Timber

/**
 * Export screen for generating and sharing pattern reports.
 * Pro-tier feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Report") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pro feature gate
            if (!uiState.hasProAccess) {
                ProFeatureCard()
                return@Column
            }
            
            // Export format selector
            FormatSelector(
                selectedFormat = uiState.selectedFormat,
                onFormatSelected = viewModel::onFormatChanged
            )
            
            // Date range selector
            DateRangeSelector(
                selectedRange = uiState.selectedDateRange,
                onRangeSelected = viewModel::onDateRangeChanged
            )
            
            // Confidence filter
            ConfidenceFilter(
                minConfidence = uiState.minConfidence,
                onConfidenceChanged = viewModel::onMinConfidenceChanged
            )
            
            // Pattern count preview
            PatternCountCard(count = uiState.patternCount)
            
            // Generate button
            if (uiState.isGenerating) {
                GeneratingProgress(progress = uiState.progress)
            } else if (uiState.generatedFile != null) {
                ShareButton(
                    file = uiState.generatedFile!!,
                    format = uiState.selectedFormat,
                    onDismiss = viewModel::clearGeneratedFile
                )
            } else {
                Button(
                    onClick = viewModel::generateReport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.patternCount > 0
                ) {
                    Text("Generate Report")
                }
            }
            
            // Error display
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ProFeatureCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pro Feature",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Report export is available with QuantraVision Pro. Upgrade to unlock PDF and CSV exports of your pattern detections.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FormatSelector(
    selectedFormat: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Export Format",
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan
        )
        
        ExportFormat.values().forEach { format ->
            MenuItemCard(
                title = format.name,
                subtitle = when (format) {
                    ExportFormat.PDF -> "Professional PDF report with charts"
                    ExportFormat.CSV -> "Spreadsheet-compatible data export"
                },
                onClick = { onFormatSelected(format) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = if (selectedFormat == format) NeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                badge = if (selectedFormat == format) "✓ SELECTED" else null,
                badgeColor = if (selectedFormat == format) NeonCyan else Color.Gray
            )
        }
    }
}

@Composable
private fun DateRangeSelector(
    selectedRange: DateRange,
    onRangeSelected: (DateRange) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Date Range",
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan
        )
        
        DateRange.values().forEach { range ->
            MenuItemCard(
                title = when (range) {
                    DateRange.LAST_7_DAYS -> "Last 7 days"
                    DateRange.LAST_30_DAYS -> "Last 30 days"
                    DateRange.ALL_TIME -> "All time"
                },
                onClick = { onRangeSelected(range) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (selectedRange == range) NeonCyan else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                badge = if (selectedRange == range) "✓ SELECTED" else null,
                badgeColor = if (selectedRange == range) NeonCyan else Color.Gray
            )
        }
    }
}

@Composable
private fun ConfidenceFilter(
    minConfidence: Float,
    onConfidenceChanged: (Float) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Minimum Confidence", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = minConfidence,
                    onValueChange = onConfidenceChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(minConfidence * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun PatternCountCard(count: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Patterns to export:", style = MaterialTheme.typography.titleMedium)
            Text("$count", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun GeneratingProgress(progress: Float) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Generating report... ${(progress * 100).toInt()}%")
    }
}

@Composable
private fun ShareButton(
    file: java.io.File,
    format: ExportFormat,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = "Report generated successfully!",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        Button(
            onClick = {
                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = when (format) {
                            ExportFormat.PDF -> "application/pdf"
                            ExportFormat.CSV -> "text/csv"
                        }
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    
                    context.startActivity(
                        android.content.Intent.createChooser(intent, "Share Report")
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Failed to share file")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Report")
        }
        
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Another")
        }
    }
}
