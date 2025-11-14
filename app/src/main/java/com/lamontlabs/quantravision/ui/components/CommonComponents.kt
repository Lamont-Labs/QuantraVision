package com.lamontlabs.quantravision.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography

/**
 * Common reusable UI components for QuantraVision screens
 */

/**
 * SectionHeader - Displays a section title with optional action button
 * 
 * @param title Section title text
 * @param actionText Optional action button text (e.g., "View All")
 * @param onActionClick Optional callback for action button
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.titleLarge,
            color = Color.White
        )
        
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = AppTypography.labelMedium,
                    color = AppColors.NeonCyan
                )
            }
        }
    }
}

/**
 * EmptyState - Displays when no data is available
 * 
 * @param icon Icon to display
 * @param message Message to show user
 * @param actionText Optional action button text
 * @param onActionClick Optional callback for action button
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )
        
        Text(
            text = message,
            style = AppTypography.bodyLarge,
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        
        if (actionText != null && onActionClick != null) {
            Button(onClick = onActionClick) {
                Text(actionText)
            }
        }
    }
}

/**
 * ErrorState - Displays error message with retry button
 * 
 * @param message Error message to display
 * @param onRetry Optional retry callback
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = AppColors.Error
        )
        
        Text(
            text = message,
            style = AppTypography.bodyLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Text("Retry")
            }
        }
    }
}
