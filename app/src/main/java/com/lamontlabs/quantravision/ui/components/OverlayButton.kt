package com.lamontlabs.quantravision.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lamontlabs.quantravision.R

/**
 * Floating neon toggle for the overlay HUD.
 * - isActive: whether the overlay service is running
 * - remainingHighlights: show remaining free pattern highlights (nullable hides badge)
 * - onToggle: start/stop overlay
 * - onUpgrade: called when user taps "Upgrade" pill (shown when remainingHighlights == 0)
 */
@Composable
fun OverlayButton(
    isActive: Boolean,
    remainingHighlights: Int?,
    onToggle: () -> Unit,
    onUpgrade: () -> Unit,
    size: Dp = 56.dp,
    glow: Boolean = true,
) {
    val onCol = MaterialTheme.colorScheme.primary // neon cyan
    val offCol = MaterialTheme.colorScheme.surfaceVariant
    val tint by animateColorAsState(
        if (isActive) onCol else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "tint"
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .wrapContentSize()
    ) {
        BadgedBox(
            badge = {
                AnimatedVisibility(visible = (remainingHighlights ?: -1) >= 0) {
                    Badge(
                        containerColor = if ((remainingHighlights ?: 0) > 0) onCol else MaterialTheme.colorScheme.error
                    ) {
                        Text(
                            text = "${remainingHighlights ?: 0}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        ) {
            NeonCircle(
                size = size,
                glowColor = onCol,
                background = if (isActive) onCol.copy(alpha = 0.12f) else offCol,
                glow = glow
            ) {
                val noRipple = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = noRipple,
                            indication = null,
                            role = Role.Button,
                            onClick = onToggle
                        )
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_qv_logo),
                        contentDescription = if (isActive) "Stop overlay" else "Start overlay",
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                alpha = if (isActive) 1.0f else 0.6f
                            }
                    )
                }
            }
        }

        // Upgrade pill when quota exhausted
        AnimatedVisibility(
            visible = (remainingHighlights ?: 1) == 0,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (size / 2) + 12.dp)
        ) {
            UpgradePill(onUpgrade = onUpgrade)
        }
    }
}

@Composable
private fun NeonCircle(
    size: Dp,
    glowColor: Color,
    background: Color,
    glow: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    val grad = Brush.radialGradient(
        colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
        radius = size.toPx() * 1.2f
    )
    Box(
        modifier = Modifier
            .size(size + 18.dp)
            .graphicsLayer { alpha = 1f }
            .then(
                if (glow) Modifier
                    .blur(16.dp)
                    .background(grad, shape = CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = background,
            contentColor = glowColor,
            shape = CircleShape,
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            modifier = Modifier
                .size(size)
                .shadow(12.dp, CircleShape, clip = false)
        ) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
    }
}

@Composable
private fun UpgradePill(onUpgrade: () -> Unit) {
    val cyan = MaterialTheme.colorScheme.primary
    val dark = MaterialTheme.colorScheme.surface
    Surface(
        shape = CircleShape,
        color = dark.copy(alpha = 0.9f),
        border = null,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onUpgrade)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Upgrade, contentDescription = null, tint = cyan, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Upgrade",
                color = cyan,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
