package com.lamontlabs.quantravision.ui.screens.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lamontlabs.quantravision.learning.RecommendationType
import com.lamontlabs.quantravision.learning.model.Difficulty
import com.lamontlabs.quantravision.ui.components.PersonalizedRecommendationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningDashboardScreen(
    viewModel: LearningDashboardViewModel = viewModel()
) {
    val stats by viewModel.learningStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Learning Dashboard") },
                actions = {
                    IconButton(onClick = { viewModel.refreshStats() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    EducationalDisclaimer()
                }
                
                item {
                    OverviewCard(stats)
                }
                
                item {
                    PersonalizedRecommendationCard(
                        recommendations = stats.recommendations
                    )
                }
                
                if (stats.bestPatterns.isNotEmpty()) {
                    item {
                        PatternListCard(
                            title = "⭐ Your Best Patterns",
                            patterns = stats.bestPatterns
                        )
                    }
                }
                
                if (stats.improvingPatterns.isNotEmpty()) {
                    item {
                        PatternListCard(
                            title = "📈 Improving Patterns",
                            patterns = stats.improvingPatterns
                        )
                    }
                }
                
                if (stats.needsPractice.isNotEmpty()) {
                    item {
                        PatternListCard(
                            title = "📚 Needs Practice",
                            patterns = stats.needsPractice
                        )
                    }
                }
                
                item {
                    DifficultyBreakdownCard(stats.difficultyBreakdown)
                }
                
                item {
                    TrendAnalysisCard(viewModel.getTrendAnalysis())
                }
            }
        }
    }
}

@Composable
private fun EducationalDisclaimer() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ Educational Tool Only",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "This dashboard shows personalized educational statistics only. Not financial advice. Past patterns don't predict future performance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun OverviewCard(stats: LearningStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Learning Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Patterns Learned From",
                    value = stats.totalFeedbackCount.toString()
                )
                StatItem(
                    label = "Overall Win Rate",
                    value = "${(stats.overallWinRate * 100).toInt()}%"
                )
                StatItem(
                    label = "Recent Win Rate (30d)",
                    value = "${(stats.recentWinRate * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PatternListCard(
    title: String,
    patterns: List<PatternPerformance>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            patterns.forEach { pattern ->
                PatternPerformanceItem(pattern)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PatternPerformanceItem(pattern: PatternPerformance) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val difficultyEmoji = when (pattern.difficulty) {
            Difficulty.EASY -> "🟢"
            Difficulty.MEDIUM -> "🟡"
            Difficulty.HARD -> "🔴"
            Difficulty.UNKNOWN -> "⚪"
        }
        
        Text(
            text = difficultyEmoji,
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pattern.patternType,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${pattern.totalOutcomes} outcomes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${(pattern.winRate * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (pattern.winRate >= 0.6) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DifficultyBreakdownCard(breakdown: Map<Difficulty, Int>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pattern Difficulty Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            listOf(
                Difficulty.EASY to "🟢 Easy",
                Difficulty.MEDIUM to "🟡 Medium",
                Difficulty.HARD to "🔴 Hard",
                Difficulty.UNKNOWN to "⚪ Unknown"
            ).forEach { (difficulty, label) ->
                val count = breakdown[difficulty] ?: 0
                if (count > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendAnalysisCard(analysis: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Trend Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = analysis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
