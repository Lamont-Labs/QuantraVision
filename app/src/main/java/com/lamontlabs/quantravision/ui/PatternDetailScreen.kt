package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.PatternMatch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternDetailScreen(match: PatternMatch, onBack: () -> Unit) {
    val time = remember(match.timestamp) {
        SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(match.timestamp))
    }
    
    val confidencePercent = (match.confidence * 100).roundToInt()
    val confidenceColor = when {
        confidencePercent >= 80 -> ElectricCyan
        confidencePercent >= 60 -> AmberAccent
        else -> NeonRed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        match.patternName,
                        style = MaterialTheme.typography.headlineLarge.copy(shadow = CyanGlowShadow)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = ElectricCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkSurface
                )
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Pattern Detected",
                        style = MaterialTheme.typography.headlineSmall.copy(shadow = SubtleGlowShadow),
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        match.patternName,
                        style = MaterialTheme.typography.titleLarge,
                        color = CrispWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = match.confidence.toFloat(),
                            modifier = Modifier.fillMaxSize(),
                            color = confidenceColor,
                            strokeWidth = 12.dp,
                            trackColor = Gunmetal
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$confidencePercent%",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    shadow = CyanGlowShadow
                                ),
                                color = confidenceColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Confidence",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MetallicSilver
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        confidenceColor.copy(alpha = 0.3f),
                                        confidenceColor.copy(alpha = 0.1f)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            when {
                                confidencePercent >= 80 -> "High Confidence"
                                confidencePercent >= 60 -> "Medium Confidence"
                                else -> "Low Confidence"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = confidenceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Divider(color = ElectricCyan.copy(alpha = 0.2f))
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = MetallicSilver,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            time,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MetallicSilver
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan
                )
            ) {
                Text(
                    "Back to Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
