package com.lamontlabs.quantravision.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.backtesting.BacktestEngine
import com.lamontlabs.quantravision.integration.FeatureIntegration
import com.lamontlabs.quantravision.ui.paywall.ProUpgradePrompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BacktestScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isProActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf<List<BacktestEngine.BacktestResult>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        isProActive = FeatureIntegration.canAccessProFeature(context)
    }
    
    val csvPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                errorMessage = null
                try {
                    val fileName = copyUriToFile(context, uri)
                    selectedFileName = fileName
                    
                    val patterns = listOf(
                        "head_shoulders", "inverse_hs", "double_top", "double_bottom",
                        "bull_flag", "bear_flag", "ascending_triangle", "descending_triangle"
                    )
                    
                    val backtestResults = withContext(Dispatchers.IO) {
                        BacktestEngine.runBacktest(context, fileName, patterns, lookaheadBars = 10)
                    }
                    
                    results = backtestResults
                    
                } catch (e: Exception) {
                    errorMessage = "Failed to process CSV: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pattern Backtesting") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (!isProActive) {
            ProUpgradePrompt(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                featureName = "Pattern Backtesting",
                description = "Test pattern accuracy against historical data. Unlock with Pro to validate strategies before trading."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Import Historical Data",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Upload a CSV file with columns: timestamp, open, high, low, close, volume",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = { csvPicker.launch("*/*") },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Upload, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (selectedFileName != null) "Change CSV File" else "Import CSV File")
                            }
                            
                            if (selectedFileName != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Loaded: $selectedFileName",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                if (isLoading) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator()
                                    Text(
                                        "Running backtest...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                
                errorMessage?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                if (results.isNotEmpty() && !isLoading) {
                    item {
                        Text(
                            "Backtest Results",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    item {
                        BacktestSummaryCard(results)
                    }
                    
                    items(results) { result ->
                        BacktestResultCard(result)
                    }
                }
                
                if (results.isEmpty() && !isLoading && selectedFileName == null && errorMessage == null) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "No backtest data yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Import a CSV file to start backtesting patterns",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BacktestSummaryCard(results: List<BacktestEngine.BacktestResult>) {
    val avgAccuracy = results.map { it.accuracy }.average()
    val totalDetections = results.sumOf { it.totalDetections }
    val avgProfitability = results.map { it.profitability }.average()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Patterns Tested", "${results.size}")
                SummaryItem("Total Detections", "$totalDetections")
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Avg Accuracy", String.format("%.1f%%", avgAccuracy))
                SummaryItem(
                    "Avg Profit/Loss",
                    String.format("%.2f%%", avgProfitability),
                    color = if (avgProfitability >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun BacktestResultCard(result: BacktestEngine.BacktestResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        result.patternName.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${result.totalDetections} detections",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                </Column>
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        result.accuracy >= 70 -> Color(0xFF4CAF50)
                        result.accuracy >= 50 -> Color(0xFFFFC107)
                        else -> Color(0xFFF44336)
                    }
                ) {
                    Text(
                        String.format("%.1f%%", result.accuracy),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Avg Confidence", String.format("%.2f", result.avgConfidence))
                MetricItem(
                    "Profitability",
                    String.format("%.2f%%", result.profitability),
                    color = if (result.profitability >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem("Best Timeframe", result.bestTimeframe)
                MetricItem("Worst Timeframe", result.worstTimeframe)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onTertiaryContainer) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

private fun copyUriToFile(context: android.content.Context, uri: Uri): String {
    val fileName = "backtest_data_${System.currentTimeMillis()}.csv"
    val targetDir = File(context.filesDir, "historical_data")
    if (!targetDir.exists()) targetDir.mkdirs()
    
    val targetFile = File(targetDir, fileName)
    
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
            input.copyTo(output)
        }
    }
    
    return fileName
}
