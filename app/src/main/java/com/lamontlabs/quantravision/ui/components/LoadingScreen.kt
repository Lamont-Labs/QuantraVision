package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography

/**
 * LoadingScreen - Full-screen loading indicator with optional message
 * 
 * Provides a consistent loading experience across all screens in the app.
 * Displays a centered circular progress indicator with optional loading message.
 * 
 * @param message Loading message to display below the indicator
 * @param modifier Modifier for customization
 */
@Composable
fun LoadingScreen(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = AppTypography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * LoadingIndicator - Inline loading indicator with optional message
 * 
 * Smaller loading indicator suitable for inline use within cards or sections.
 * Displays a horizontal row with a small circular progress indicator and optional text.
 * 
 * @param message Optional loading message to display next to the indicator
 * @param modifier Modifier for customization
 */
@Composable
fun LoadingIndicator(
    message: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp)
        )
        if (message != null) {
            Text(
                text = message,
                style = AppTypography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
