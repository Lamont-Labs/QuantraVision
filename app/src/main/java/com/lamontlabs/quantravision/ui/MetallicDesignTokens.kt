package com.lamontlabs.quantravision.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Metallic Design System for QuantraVision
 * Premium, shiny UI components with gradient effects that simulate reflective metal surfaces
 */

// ============================================================================
// METALLIC GRADIENT BRUSHES - CHROME/STEEL
// ============================================================================

/**
 * Chrome metal gradient: polished steel with bright white reflections
 * Simulates reflective chrome surface like a car bumper
 */
val metallicCyanBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A0C10),  // Deep shadow
        Color(0xFF2C3038),  // Mid metal
        Color(0xFFE8E8E8),  // Bright reflection
        Color(0xFFD0F0F5),  // Subtle cyan tint
        Color(0xFFE8E8E8),  // Bright reflection
        Color(0xFF2C3038),  // Mid metal
        Color(0xFF0A0C10)   // Deep shadow
    )
)

/**
 * Steel metal gradient - darker polished metal areas
 */
val steelMetalBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A0C10),  // Deep black
        Color(0xFF1A1D23),  // Dark gray
        Color(0xFF2C3038),  // Mid gray
        Color(0xFF1A1D23),  // Dark gray
        Color(0xFF0A0C10)   // Deep black
    )
)

/**
 * Horizontal reflection sweep - simulates light sweeping across metal
 */
val horizontalReflectionBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1A1D23),  // Dark metal
        Color(0xFFFFFFFF).copy(alpha = 0.3f),  // White reflection
        Color(0xFF1A1D23)   // Dark metal
    )
)

/**
 * Chrome border gradient - silver/white shimmer
 */
val chromeBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFE8E8E8),  // Light silver
        Color(0xFFFFFFFF),  // Bright white
        Color(0xFFD0F0F5)   // Subtle cyan accent
    )
)

/**
 * Specular highlight overlay - radial white glow for premium shine effect
 */
val specularHighlight = Brush.radialGradient(
    colors = listOf(
        Color(0x44FFFFFF),  // Center white glow
        Color(0x00FFFFFF)   // Fade out
    )
)

/**
 * Darker metallic gradient for pressed state - dimmed chrome
 */
val metallicCyanBrushDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF000000),  // Pure black
        Color(0xFF1A1D23),  // Dark gray
        Color(0xFF888888),  // Dimmed reflection
        Color(0xFF1A1D23),  // Dark gray
        Color(0xFF000000)   // Pure black
    )
)

/**
 * Angular gradient for card backgrounds - creates beveled polished metal look
 */
val metallicAngularBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1A1D23),  // Dark metal
        Color(0xFF2C3038),  // Mid tone
        Color(0xFFE8E8E8).copy(alpha = 0.2f),  // Subtle reflection
        Color(0xFF2C3038),  // Mid tone
        Color(0xFF1A1D23)   // Dark metal
    ),
    start = Offset.Zero,
    end = Offset.Infinite
)

/**
 * Corner radius constants for rounded metal design
 */
private val METAL_BUTTON_CORNER_RADIUS = 20.dp
private val METAL_CARD_CORNER_RADIUS = 16.dp

// ============================================================================
// METALLIC BUTTON COMPOSABLE
// ============================================================================

/**
 * Premium metallic button with gradient background, chrome border, and press effects
 * 
 * @param onClick Click handler
 * @param modifier Modifier for customization
 * @param enabled Whether button is enabled
 * @param contentPadding Internal padding
 * @param showTopStrip Whether to show bright edge light strip
 * @param content Button content (typically Text and Icon)
 */
@Composable
fun MetallicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    showTopStrip: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Box(
        modifier = modifier
            .metallicBorder(
                width = 2.dp,
                brush = chromeBorderBrush,
                shape = RoundedCornerShape(METAL_BUTTON_CORNER_RADIUS)
            )
            .background(
                brush = if (isPressed) metallicCyanBrushDark else metallicCyanBrush,
                shape = RoundedCornerShape(METAL_BUTTON_CORNER_RADIUS)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .drawBehind {
                drawRect(brush = horizontalReflectionBrush, alpha = 0.2f)
            }
    ) {
        // Top edge light strip with rounded ends
        if (showTopStrip) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFFFFF),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(topStart = METAL_BUTTON_CORNER_RADIUS, topEnd = METAL_BUTTON_CORNER_RADIUS)
                    )
                    .align(Alignment.TopCenter)
            )
        }
        
        // Content with specular highlight overlay
        Row(
            modifier = Modifier
                .padding(contentPadding)
                .drawBehind {
                    drawRect(
                        brush = specularHighlight,
                        alpha = if (isPressed) 0.3f else 0.5f
                    )
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// ============================================================================
// METALLIC CARD COMPOSABLE
// ============================================================================

/**
 * Premium metallic card with beveled edges and optional shimmer animation
 * 
 * @param modifier Modifier for customization
 * @param onClick Optional click handler (makes card clickable)
 * @param enableShimmer Whether to enable shimmer animation (use sparingly - hero CTAs only)
 * @param elevation Card elevation
 * @param content Card content
 */
@Composable
fun MetallicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enableShimmer: Boolean = false,
    elevation: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shimmerState = rememberMetallicShimmer(enabled = enableShimmer)
    
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    Card(
        modifier = cardModifier
            .then(
                if (enableShimmer) {
                    Modifier.drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x66FFFFFF),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerState * size.width, 0f),
                                end = Offset(shimmerState * size.width + size.width * 0.3f, size.height)
                            )
                        )
                    }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(METAL_CARD_CORNER_RADIUS),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE8E8E8).copy(alpha = 0.6f),
                    Color(0xFFFFFFFF).copy(alpha = 0.8f),
                    Color(0xFFD0F0F5).copy(alpha = 0.5f),
                    Color(0xFFE8E8E8).copy(alpha = 0.6f)
                )
            )
        )
    ) {
        // Polished metal gradient background overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = metallicAngularBrush)
                .drawBehind {
                    drawRect(brush = horizontalReflectionBrush, alpha = 0.15f)
                }
        ) {
            // Inner stroke for beveled chrome look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(1.dp)
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(METAL_CARD_CORNER_RADIUS - 1.dp)
                    )
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0x60FFFFFF),
                                    Color(0x30FFFFFF)
                                )
                            ),
                            style = Stroke(width = 1.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius((METAL_CARD_CORNER_RADIUS - 1.dp).toPx())
                        )
                    }
            ) {
                Column(content = content)
            }
        }
    }
}

// ============================================================================
// GRADIENT BORDER UTILITIES
// ============================================================================

/**
 * Extension modifier to add metallic gradient border
 * Creates dual-stroke borders (outer silver + inner white) for premium chrome effect
 * 
 * @param width Border width
 * @param brush Gradient brush for border
 * @param shape Border shape
 */
fun Modifier.metallicBorder(
    width: Dp = 2.dp,
    brush: Brush = chromeBorderBrush,
    shape: Shape = RoundedCornerShape(METAL_BUTTON_CORNER_RADIUS)
): Modifier = this
    .drawBehind {
        val strokeWidth = width.toPx()
        val outline = shape.createOutline(size, layoutDirection, this)
        
        // Outer gradient border
        drawOutline(
            outline = outline,
            brush = brush,
            style = Stroke(width = strokeWidth)
        )
        
        // Inner white highlight
        drawOutline(
            outline = outline,
            color = Color(0x60FFFFFF),
            style = Stroke(width = strokeWidth * 0.5f)
        )
    }

/**
 * Dual-stroke metallic border with separate colors
 */
fun Modifier.metallicDualBorder(
    outerWidth: Dp = 2.dp,
    innerWidth: Dp = 1.dp,
    outerColor: Color = Color(0xFFE8E8E8),
    innerColor: Color = Color(0xFFFFFFFF),
    shape: Shape = RoundedCornerShape(METAL_BUTTON_CORNER_RADIUS)
): Modifier = this.drawBehind {
    val outline = shape.createOutline(size, layoutDirection, this)
    
    // Outer silver border
    drawOutline(
        outline = outline,
        color = outerColor,
        style = Stroke(width = outerWidth.toPx())
    )
    
    // Inner white border
    drawOutline(
        outline = outline,
        color = innerColor,
        style = Stroke(width = innerWidth.toPx())
    )
}

// ============================================================================
// TYPOGRAPHY SHADOW EFFECTS
// ============================================================================

/**
 * Text style with metallic glow shadow effect - white/silver glow
 * Use for headers and important text
 */
@Composable
fun rememberMetallicTextStyle(
    baseStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    glowColor: Color = Color(0x88FFFFFF),
    glowRadius: Float = 12f
): TextStyle {
    return baseStyle.copy(
        shadow = Shadow(
            color = glowColor,
            blurRadius = glowRadius
        ),
        fontWeight = FontWeight.ExtraBold
    )
}

/**
 * Intense white glow for hero text - chrome reflection effect
 */
val HeroTextShadow = Shadow(
    color = Color(0xAAFFFFFF),
    blurRadius = 12f,
    offset = Offset(0f, 0f)
)

/**
 * Subtle white glow for secondary text
 */
val SubtleTextShadow = Shadow(
    color = Color(0x66FFFFFF),
    blurRadius = 6f,
    offset = Offset(0f, 0f)
)

// ============================================================================
// SHIMMER ANIMATION
// ============================================================================

/**
 * Remembers metallic shimmer animation state
 * Creates sweeping light effect across surface
 * 
 * @param enabled Whether shimmer is enabled
 * @param durationMillis Animation duration
 * @return Animated float value from 0f to 1f
 */
@Composable
fun rememberMetallicShimmer(
    enabled: Boolean = true,
    durationMillis: Int = 2000
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerValue by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnimation"
    )
    
    return if (enabled) shimmerValue else 0f
}

/**
 * Pulsing glow animation for important elements
 */
@Composable
fun rememberMetallicPulse(
    enabled: Boolean = true,
    durationMillis: Int = 1500
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseValue by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )
    
    return if (enabled) pulseValue else 1f
}

// ============================================================================
// HELPER COMPOSABLES
// ============================================================================

/**
 * Metallic divider with chrome gradient
 */
@Composable
fun MetallicDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFE8E8E8).copy(alpha = 0.7f),
                        Color(0xFFFFFFFF).copy(alpha = 0.9f),
                        Color(0xFFD0F0F5).copy(alpha = 0.5f),
                        Color(0xFFE8E8E8).copy(alpha = 0.7f),
                        Color.Transparent
                    )
                )
            )
    )
}

/**
 * Metallic text with white chrome glow effect
 */
@Composable
fun MetallicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    glowIntensity: Float = 0.5f,
    color: Color = Color.White  // Bright white text for HD contrast
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,  // CRITICAL: Explicit color for visibility on dark backgrounds
        style = style.copy(
            shadow = Shadow(
                color = Color(0xFFFFFFFF).copy(alpha = glowIntensity),
                blurRadius = 12f * glowIntensity
            ),
            fontWeight = FontWeight.ExtraBold
        )
    )
}
