package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PredictedPattern
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.integration.FeatureIntegration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Helper function to load predictions from database
private suspend fun loadPredictions(context: android.content.Context): List<PredictedPattern> {
    return withContext(Dispatchers.IO) {
        val db = PatternDatabase.getInstance(context)
        val oneHourAgo = System.currentTimeMillis() - 3600000L
        db.predictedPatternDao().getRecent(oneHourAgo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var predictions by remember { mutableStateOf<List<PredictedPattern>>(emptyList()) }
    var isProActive by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Load predictions and check Pro status on initial composition
    LaunchedEffect(Unit) {
        isProActive = FeatureIntegration.canAccessProFeature(context)
        if (isProActive) {
            predictions = loadPredictions(context)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pattern Predictions",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            shadow = SubtleGlowShadow
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!isProActive) {
            // Show Pro upgrade prompt for free users
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🔮",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Pro Feature",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Pattern predictions are available to Pro users only",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { /* Navigate to upgrade screen */ }) {
                            Text("Upgrade to Pro")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "🔮 Forming Patterns",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    shadow = CyanGlowShadow
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Early detection of patterns before they complete",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.textSecondary
                            )
                            if (predictions.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "${predictions.size} forming patterns detected",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.amber,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (predictions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "No forming patterns detected",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "Patterns will appear here as they begin to form",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(predictions) { prediction ->
                        PredictionCard(prediction)
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "How It Works",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    shadow = SubtleGlowShadow
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "• Early: Pattern is just starting (40-50% complete)\n" +
                                "• Developing: Pattern structure forming (50-70%)\n" +
                                "• Nearly Complete: Pattern about to trigger (70-85%)",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionCard(prediction: PredictedPattern) {
    val cardColor = when (prediction.stage) {
        "nearly_complete" -> MaterialTheme.colorScheme.amber.copy(alpha = 0.12f)
        "developing" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val accentColor = when (prediction.stage) {
        "nearly_complete" -> MaterialTheme.colorScheme.amber
        "developing" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.metallic
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        prediction.patternName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            prediction.stage.replace("_", " ").uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    "${prediction.completionPercent.toInt()}%",
                    style = MaterialTheme.typography.displaySmall.copy(
                        shadow = SubtleGlowShadow
                    ),
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(Modifier.height(16.dp))

            Column {
                LinearProgressIndicator(
                    progress = (prediction.completionPercent / 100f).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${prediction.completionPercent.toInt()}% complete",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.textSecondary
                )
            }

            Spacer(Modifier.height(16.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem("Confidence", "${(prediction.confidence * 100).toInt()}%")
                DetailItem("Est. Completion", prediction.estimatedCompletion)
            }
            
            if (prediction.formationVelocity > 0) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Formation Speed:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.textSecondary
                    )
                    Text(
                        when {
                            prediction.formationVelocity > 0.01 -> "⚡ Fast"
                            prediction.formationVelocity > 0.005 -> "→ Moderate"
                            else -> "🐌 Slow"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.textSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
