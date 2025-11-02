package com.lamontlabs.quantravision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.cache.ChartHashCache
import com.lamontlabs.quantravision.cache.DetectionCache
import com.lamontlabs.quantravision.memory.BitmapPool
import com.lamontlabs.quantravision.memory.MatPool
import com.lamontlabs.quantravision.memory.MemoryMonitor
import com.lamontlabs.quantravision.performance.PerformanceProfiler
import com.lamontlabs.quantravision.performance.PowerPolicyApplicator

/**
 * Performance monitoring dashboard screen.
 * Shows real-time performance metrics and cache statistics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Refresh"
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
            // Force recomposition on refresh
            key(refreshTrigger) {
                // Performance metrics
                PerformanceMetricsCard()
                
                // Memory usage
                MemoryUsageCard(context)
                
                // Cache statistics
                CacheStatisticsCard()
                
                // Power policy
                PowerPolicyCard(context)
                
                // Pool statistics
                PoolStatisticsCard()
            }
        }
    }
}

@Composable
private fun PerformanceMetricsCard() {
    val metrics = remember { PerformanceProfiler.getMetrics() }
    
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detection Performance", style = MaterialTheme.typography.titleMedium)
            
            MetricRow("Avg Detection Time", "${String.format("%.2f", metrics.averageDetectionTimeMs)}ms")
            MetricRow("Current FPS", String.format("%.1f", metrics.currentFps))
            MetricRow("Total Detections", "${metrics.totalDetections}")
            MetricRow("Slow Operations", "${metrics.slowOperationCount}")
            
            if (metrics.operationBreakdown.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Top Operations:", style = MaterialTheme.typography.bodySmall)
                
                metrics.operationBreakdown.entries
                    .sortedByDescending { it.value.totalDurationMs }
                    .take(3)
                    .forEach { (name, stats) ->
                        Text(
                            "$name: ${String.format("%.2f", stats.averageDurationMs)}ms avg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
            }
        }
    }
}

@Composable
private fun MemoryUsageCard(context: android.content.Context) {
    val memoryMonitor = remember { MemoryMonitor(context) }
    val usage = remember { memoryMonitor.getUsage() }
    val level = remember { memoryMonitor.getMemoryLevel() }
    
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Memory Usage", style = MaterialTheme.typography.titleMedium)
            
            LinearProgressIndicator(
                progress = (usage.usagePercent / 100).toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
            
            MetricRow("Used", "${String.format("%.2f", usage.usedMB)}MB")
            MetricRow("Available", "${String.format("%.2f", usage.availableMB)}MB")
            MetricRow("Max", "${String.format("%.2f", usage.maxMB)}MB")
            MetricRow("Usage", "${String.format("%.1f", usage.usagePercent)}%")
            MetricRow("Level", level.toString())
            
            if (usage.isLow) {
                Text(
                    "⚠️ Low memory warning",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CacheStatisticsCard() {
    val detectionCache = remember { DetectionCache() }
    val chartCache = remember { ChartHashCache() }
    
    val detectionStats = remember { detectionCache.getStats() }
    val chartStats = remember { chartCache.getStats() }
    
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cache Statistics", style = MaterialTheme.typography.titleMedium)
            
            Text("Detection Cache:", style = MaterialTheme.typography.bodyMedium)
            MetricRow("Hit Rate", "${String.format("%.1f", detectionStats.hitRate * 100)}%")
            MetricRow("Size", "${detectionStats.size} entries")
            MetricRow("Hits", "${detectionStats.hits}")
            MetricRow("Misses", "${detectionStats.misses}")
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Chart Hash Cache:", style = MaterialTheme.typography.bodyMedium)
            MetricRow("Hit Rate", "${String.format("%.1f", chartStats.hitRate * 100)}%")
            MetricRow("Size", "${chartStats.size} entries")
            MetricRow("Invalidations", "${chartStats.invalidations}")
        }
    }
}

@Composable
private fun PowerPolicyCard(context: android.content.Context) {
    val applicator = remember { PowerPolicyApplicator(context) }
    val policy = remember { applicator.getCurrentPolicy() }
    val batteryLevel = remember { applicator.getBatteryLevel() }
    
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Power Policy", style = MaterialTheme.typography.titleMedium)
            
            MetricRow("Battery Level", "$batteryLevel%")
            MetricRow("Target FPS", "${policy.targetFps}")
            MetricRow("Scale Iterations", "${policy.scaleIterations}")
            MetricRow("Parallel Processing", if (policy.enableParallelProcessing) "Enabled" else "Disabled")
            
            Text(
                "Policy: ${policy.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PoolStatisticsCard() {
    val matStats = remember { MatPool.getStats() }
    val bitmapStats = remember { BitmapPool.getStats() }
    
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Object Pools", style = MaterialTheme.typography.titleMedium)
            
            Text("Mat Pool:", style = MaterialTheme.typography.bodyMedium)
            MetricRow("Total Mats", "${matStats.totalMats}")
            MetricRow("Acquisitions", "${matStats.acquisitions}")
            MetricRow("Releases", "${matStats.releases}")
            MetricRow("Memory", "${String.format("%.2f", matStats.memoryUsageMB)}MB")
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Bitmap Pool:", style = MaterialTheme.typography.bodyMedium)
            MetricRow("Total Bitmaps", "${bitmapStats.totalBitmaps}")
            MetricRow("Acquisitions", "${bitmapStats.acquisitions}")
            MetricRow("Releases", "${bitmapStats.releases}")
            MetricRow("Memory", "${String.format("%.2f", bitmapStats.memoryUsageMB)}MB")
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}
