package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.learning.Recommendation
import com.lamontlabs.quantravision.learning.RecommendationType

@Composable
fun PersonalizedRecommendationCard(
    recommendations: List<Recommendation>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📚 Personalized Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Based on your educational practice patterns only",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (recommendations.isEmpty()) {
                EmptyRecommendationsState()
            } else {
                recommendations.forEach { recommendation ->
                    RecommendationItem(recommendation = recommendation)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "⚠️ Not financial advice • For learning purposes only",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyRecommendationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎯",
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Keep Learning!",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Record more pattern outcomes to get personalized insights",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecommendationItem(recommendation: Recommendation) {
    val backgroundColor = when (recommendation.type) {
        RecommendationType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        RecommendationType.IMPROVEMENT -> MaterialTheme.colorScheme.tertiaryContainer
        RecommendationType.WARNING -> MaterialTheme.colorScheme.errorContainer
        RecommendationType.PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
    }
    
    val contentColor = when (recommendation.type) {
        RecommendationType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        RecommendationType.IMPROVEMENT -> MaterialTheme.colorScheme.onTertiaryContainer
        RecommendationType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        RecommendationType.PROGRESS -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = recommendation.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = recommendation.message,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor
        )
    }
}
