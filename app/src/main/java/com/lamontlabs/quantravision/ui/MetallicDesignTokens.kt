package com.lamontlabs.quantravision.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Metallic Design System for QuantraVision
 * Premium, shiny UI components with gradient effects that simulate reflective metal surfaces
 */

// ============================================================================
// PERFORMANCE PROFILE SYSTEM
// ============================================================================

/**
 * Performance profile for throttling animations on low-end devices
 * 
 * @param enableSecondaryAnimations Enable decorative animations (particles, shimmer, etc.)
 * @param enableParticleStarfield Enable particle starfield background
 * @param particleCount Number of particles to render (low-end uses fewer)
 * @param enableGridAnimation Enable grid scrolling animation
 * @param enablePulseAnimations Enable pulsing glow effects
 * @param animationFrameRateMultiplier Multiplier for animation duration (>1.0 = slower on battery saver)
 */
data class PerformanceProfile(
    val enableSecondaryAnimations: Boolean = true,
    val enableParticleStarfield: Boolean = true,
    val particleCount: Int = 50,
    val enableGridAnimation: Boolean = true,
    val enablePulseAnimations: Boolean = true,
    val animationFrameRateMultiplier: Float = 1.0f
) {
    companion object {
        /**
         * High performance profile - all animations enabled, maximum visual fidelity
         */
        val High = PerformanceProfile(
            enableSecondaryAnimations = true,
            enableParticleStarfield = true,
            particleCount = 50,
            enableGridAnimation = true,
            enablePulseAnimations = true,
            animationFrameRateMultiplier = 1.0f
        )
        
        /**
         * Medium performance profile - reduced particle count, all other animations enabled
         */
        val Medium = PerformanceProfile(
            enableSecondaryAnimations = true,
            enableParticleStarfield = true,
            particleCount = 30,
            enableGridAnimation = true,
            enablePulseAnimations = true,
            animationFrameRateMultiplier = 1.0f
        )
        
        /**
         * Low performance profile - minimal animations for low-end devices
         */
        val Low = PerformanceProfile(
            enableSecondaryAnimations = false,
            enableParticleStarfield = false,
            particleCount = 20,
            enableGridAnimation = false,
            enablePulseAnimations = false,
            animationFrameRateMultiplier = 1.0f
        )
        
        /**
         * Battery saver profile - reduced frame rates and minimal decorative animations
         */
        val BatterySaver = PerformanceProfile(
            enableSecondaryAnimations = false,
            enableParticleStarfield = false,
            particleCount = 20,
            enableGridAnimation = false,
            enablePulseAnimations = false,
            animationFrameRateMultiplier = 1.5f // Slower animations
        )
    }
}

/**
 * CompositionLocal for accessing the current performance profile
 */
val LocalPerformanceProfile = compositionLocalOf { PerformanceProfile.High }

// ============================================================================
// ANIMATION CONSTANTS - CENTRALIZED CONFIGURATION
// ============================================================================

/**
 * Centralized animation specifications for consistency and easy tuning
 */
object AnimationSpecs {
    // Particle System
    const val PARTICLE_COUNT_HIGH = 50
    const val PARTICLE_COUNT_MEDIUM = 30
    const val PARTICLE_COUNT_LOW = 20
    
    // Grid Animation
    const val GRID_ANIMATION_DURATION = 3000
    const val GRID_SPACING_DP = 50f
    
    // Pulse Animations
    const val PULSE_DURATION = 2000
    const val PULSE_DURATION_FAST = 1500
    const val PULSE_DURATION_SLOW = 3000
    
    // Shimmer Effects
    const val SHIMMER_DURATION = 2000
    
    // Scanline Effects
    const val SCANLINE_DURATION = 2000
    
    // Badge Pulse
    const val BADGE_PULSE_DURATION = 2000
    
    // Candlestick Parallax
    const val CANDLESTICK_SCROLL_DURATION = 8000
    
    // Neon Bounding Box
    const val BOUNDING_BOX_CORNER_ANIMATION_DURATION = 1000
    
    // Ticker Scroll
    const val TICKER_SCROLL_DURATION = 10000
    
    // Heatmap Pulse
    const val HEATMAP_PULSE_DURATION = 1500
    
    // Circular Progress
    const val CIRCULAR_PROGRESS_BASE_DURATION = 1000
    const val CIRCULAR_PROGRESS_DELAY_PER_RING = 200
}

// ============================================================================
// METALLIC GRADIENT BRUSHES - CHROME/STEEL
// ============================================================================

/**
 * Chrome metal gradient: polished steel with bright white reflections
 * Simulates reflective chrome surface - IMPROVED for better text contrast
 */
val metallicCyanBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF05080C),  // Deeper shadow
        Color(0xFF1A1E24),  // Dark metal
        Color(0xFF3A4550),  // Mid metal (DARKER than before)
        Color(0xFF5FDDEB),  // Cyan reflection (was white, now cyan)
        Color(0xFF3A4550),  // Mid metal
        Color(0xFF1A1E24),  // Dark metal
        Color(0xFF05080C)   // Deep shadow
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
 * Darker metallic gradient for pressed state - dimmed chrome with cyan hint
 */
val metallicCyanBrushDark = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF000000),  // Pure black
        Color(0xFF0D1219),  // Very dark
        Color(0xFF2A3540),  // Dim metal
        Color(0xFF00A8B8),  // Dimmed cyan reflection
        Color(0xFF2A3540),  // Dim metal
        Color(0xFF0D1219),  // Very dark
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
// NEON GLOW SYSTEM - FUTURISTIC TEXT & ICONS
// ============================================================================

/**
 * Neon glow colors matching QuantraCore logo aesthetic
 */
val NeonCyan = Color(0xFF00F0FF)         // Bright cyan
val NeonCyanBright = Color(0xFF5FDDEB)   // Lighter cyan for highlights
val NeonGold = Color(0xFFFFB347)         // Gold/orange accent

/**
 * Futuristic neon text with layered blur effect - matches QuantraCore logo aesthetic
 * 
 * Creates a glowing "bloom" effect by layering multiple blurred text copies with
 * progressively increasing blur radius, topped with a sharp foreground text.
 * 
 * @param text Text to display
 * @param modifier Modifier for customization
 * @param style Text style (font, size, weight)
 * @param glowColor Primary glow color (default: cyan)
 * @param textColor Foreground text color (default: white)
 * @param glowIntensity Glow strength from 0f to 1f (default: 0.8f)
 * @param enablePulse Whether to animate pulsing glow effect
 */
@Composable
fun NeonText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    glowColor: Color = NeonCyan,
    textColor: Color = Color.White,
    glowIntensity: Float = 0.8f,
    enablePulse: Boolean = false
) {
    // Optional pulsing animation
    val animatedIntensity = if (enablePulse) {
        val infiniteTransition = rememberInfiniteTransition(label = "neonPulse")
        infiniteTransition.animateFloat(
            initialValue = glowIntensity * 0.7f,
            targetValue = glowIntensity,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowPulse"
        ).value
    } else {
        glowIntensity
    }
    
    // Clean professional text with subtle shadow - no excessive blur layers
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            shadow = Shadow(
                color = glowColor.copy(alpha = animatedIntensity * 0.3f),
                blurRadius = 8f,
                offset = Offset(0f, 0f)
            )
        ),
        color = textColor
    )
}

/**
 * Glowing icon with neon bloom effect
 * 
 * @param imageVector Icon to display
 * @param contentDescription Accessibility description
 * @param modifier Modifier for customization
 * @param glowColor Glow color (default: cyan)
 * @param iconColor Foreground icon color (default: white)
 * @param size Icon size
 * @param glowIntensity Glow strength 0f to 1f
 */
@Composable
fun GlowingIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    iconColor: Color = Color.White,
    size: Dp = 24.dp,
    glowIntensity: Float = 0.8f
) {
    // Clean professional icon - no excessive blur layers
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = iconColor,
        modifier = modifier.size(size)
    )
}

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
            .background(
                brush = if (isPressed) metallicCyanBrushDark else metallicCyanBrush,
                shape = RoundedCornerShape(METAL_BUTTON_CORNER_RADIUS)
            )
            .drawBehind {
                // Horizontal reflection overlay for glassmorphic shimmer effect
                drawRect(
                    brush = horizontalReflectionBrush,
                    alpha = 0.6f
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
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
        
        // Content - clean professional appearance, no background boxes
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Row(
                modifier = Modifier.padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
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
// MULTI-LAYER 3D SHADOW SYSTEM
// ============================================================================

/**
 * Shadow definition for layered 3D effects
 * 
 * @param color Shadow color
 * @param radius Blur radius (larger = softer shadow)
 * @param spread How much shadow expands beyond shape
 * @param offset X/Y position offset
 */
data class LayeredShadow(
    val color: Color,
    val radius: Dp,
    val spread: Dp = 0.dp,
    val offset: Offset = Offset.Zero
)

/**
 * Adds layered depth shadows for 3D floating effect
 * Stacks multiple shadow layers to create realistic depth matching QuantraCore logo
 * 
 * @param shadows List of shadow layers (bottom to top)
 * @param shape Shadow shape
 */
fun Modifier.layeredShadow(
    shadows: List<LayeredShadow>,
    shape: Shape = RoundedCornerShape(METAL_CARD_CORNER_RADIUS)
): Modifier = this.drawBehind {
    val path = shape.createOutline(size, layoutDirection, this).let {
        when (it) {
            is Outline.Generic -> it.path
            is Outline.Rounded -> Path().apply { addRoundRect(it.roundRect) }
            is Outline.Rectangle -> Path().apply { addRect(it.rect) }
        }
    }
    
    shadows.forEach { shadow ->
        val shadowPath = Path().apply {
            addPath(path)
            // Apply offset
            translate(Offset(shadow.offset.x, shadow.offset.y))
        }
        
        drawPath(
            path = shadowPath,
            color = shadow.color,
            style = androidx.compose.ui.graphics.drawscope.Fill,
            alpha = shadow.color.alpha
        )
    }
}

/**
 * Pre-defined 3D depth shadow set - subtle floating effect
 */
val subtleDepthShadows = listOf(
    LayeredShadow(
        color = Color.Black.copy(alpha = 0.3f),
        radius = 16.dp,
        offset = Offset(0f, 8f)
    ),
    LayeredShadow(
        color = Color.Black.copy(alpha = 0.2f),
        radius = 24.dp,
        offset = Offset(0f, 12f)
    ),
    LayeredShadow(
        color = Color(0xFF00F0FF).copy(alpha = 0.1f), // Cyan glow
        radius = 32.dp,
        offset = Offset(0f, 0f)
    )
)

/**
 * Pre-defined 3D depth shadow set - dramatic floating effect
 */
val dramaticDepthShadows = listOf(
    LayeredShadow(
        color = Color.Black.copy(alpha = 0.4f),
        radius = 20.dp,
        offset = Offset(0f, 12f)
    ),
    LayeredShadow(
        color = Color.Black.copy(alpha = 0.3f),
        radius = 32.dp,
        offset = Offset(0f, 18f)
    ),
    LayeredShadow(
        color = Color.Black.copy(alpha = 0.2f),
        radius = 48.dp,
        offset = Offset(0f, 24f)
    ),
    LayeredShadow(
        color = NeonCyan.copy(alpha = 0.15f), // Cyan glow underneath
        radius = 40.dp,
        offset = Offset(0f, 0f)
    )
)

/**
 * Neon glow shadow - cyan bloom underneath element
 */
val neonGlowShadows = listOf(
    LayeredShadow(
        color = NeonCyan.copy(alpha = 0.3f),
        radius = 16.dp,
        offset = Offset(0f, 0f)
    ),
    LayeredShadow(
        color = NeonCyan.copy(alpha = 0.2f),
        radius = 28.dp,
        offset = Offset(0f, 0f)
    ),
    LayeredShadow(
        color = NeonCyan.copy(alpha = 0.1f),
        radius = 40.dp,
        offset = Offset(0f, 0f)
    )
)

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
 * Respects PerformanceProfile for low-end device optimization
 * 
 * @param enabled Whether shimmer is enabled
 * @param durationMillis Animation duration (overridden by performance profile)
 * @return Animated float value from 0f to 1f
 */
@Composable
fun rememberMetallicShimmer(
    enabled: Boolean = true,
    durationMillis: Int = AnimationSpecs.SHIMMER_DURATION
): Float {
    val performanceProfile = LocalPerformanceProfile.current
    
    // Disable shimmer on low performance mode
    val isEnabled = enabled && performanceProfile.enableSecondaryAnimations
    
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerValue by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (durationMillis * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnimation"
    )
    
    return if (isEnabled) shimmerValue else 0f
}

/**
 * Pulsing glow animation for important elements
 * Respects PerformanceProfile for low-end device optimization
 */
@Composable
fun rememberMetallicPulse(
    enabled: Boolean = true,
    durationMillis: Int = AnimationSpecs.PULSE_DURATION_FAST
): Float {
    val performanceProfile = LocalPerformanceProfile.current
    
    // Disable pulse on low performance mode
    val isEnabled = enabled && performanceProfile.enablePulseAnimations
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val pulseValue by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (durationMillis * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnimation"
    )
    
    return if (isEnabled) pulseValue else 1f
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

// ============================================================================
// METALLIC ACCORDION COMPOSABLE
// ============================================================================

/**
 * Premium metallic accordion with expandable/collapsible chrome sections
 * 
 * @param title Accordion header text
 * @param modifier Modifier for customization
 * @param expanded Whether the accordion is expanded
 * @param onToggle Callback when accordion is clicked
 * @param badge Optional badge count to display (e.g., notifications, achievements)
 * @param icon Optional leading icon
 * @param content Content to show when expanded
 */
@Composable
fun MetallicAccordion(
    title: String,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    onToggle: () -> Unit,
    badge: Int? = null,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "chevronRotation"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = metallicAngularBrush,
                shape = RoundedCornerShape(METAL_CARD_CORNER_RADIUS)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .background(
                    brush = if (expanded) metallicCyanBrush else steelMetalBrush,
                    shape = RoundedCornerShape(
                        topStart = METAL_CARD_CORNER_RADIUS,
                        topEnd = METAL_CARD_CORNER_RADIUS,
                        bottomStart = if (expanded) 0.dp else METAL_CARD_CORNER_RADIUS,
                        bottomEnd = if (expanded) 0.dp else METAL_CARD_CORNER_RADIUS
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        icon()
                        Spacer(Modifier.width(12.dp))
                    }
                    
                    MetallicText(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        glowIntensity = if (expanded) 0.8f else 0.5f
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (badge != null && badge > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Text(
                                text = badge.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                    )
                }
            }
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

// ============================================================================
// CIRCULAR HUD COMPONENTS - FUTURISTIC ARC VISUALIZATION
// ============================================================================

/**
 * Circular HUD progress indicator with gradient arc - matches QuantraCore logo aesthetic
 * 
 * Features:
 * - Animated gradient arc progress
 * - Segmented tick markers like a speedometer
 * - Outer glow ring for depth
 * - Center content display
 * 
 * @param progress Progress value (0f to 1f)
 * @param modifier Modifier for customization
 * @param startAngle Starting angle in degrees (0° = 3 o'clock)
 * @param sweepAngle Total arc angle to sweep
 * @param strokeWidth Arc thickness
 * @param size Component size
 * @param showTicks Whether to show segmented markers
 * @param centerContent Optional content to display in center
 */
@Composable
fun CircularHUDProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    strokeWidth: Dp = 12.dp,
    size: Dp = 200.dp,
    showTicks: Boolean = true,
    centerContent: @Composable (BoxScope.() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "hudProgress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val stroke = strokeWidth.toPx()
            val radius = (canvasSize - stroke) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Outer glow ring (cyan bloom)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.1f),
                        NeonCyan.copy(alpha = 0.3f),
                        NeonCyan.copy(alpha = 0.1f)
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke * 2f, cap = StrokeCap.Round),
                size = Size(canvasSize * 1.08f, canvasSize * 1.08f),
                topLeft = Offset(
                    (this.size.width - canvasSize * 1.08f) / 2f,
                    (this.size.height - canvasSize * 1.08f) / 2f
                )
            )

            // Background track (dark gray arc)
            drawArc(
                color = Color(0xFF1A1A2E),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize),
                topLeft = Offset(
                    (this.size.width - canvasSize) / 2f,
                    (this.size.height - canvasSize) / 2f
                )
            )

            // Gradient progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0f to NeonCyan,
                        0.5f to NeonCyanBright,
                        1f to NeonCyan
                    ),
                    center = center
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = Size(canvasSize, canvasSize),
                topLeft = Offset(
                    (this.size.width - canvasSize) / 2f,
                    (this.size.height - canvasSize) / 2f
                )
            )

            // Inner ring accent (subtle glow)
            drawArc(
                color = NeonCyan.copy(alpha = 0.2f),
                startAngle = startAngle,
                sweepAngle = sweepAngle * animatedProgress,
                useCenter = false,
                style = Stroke(width = stroke / 2f, cap = StrokeCap.Round),
                size = Size(canvasSize * 0.85f, canvasSize * 0.85f),
                topLeft = Offset(
                    (this.size.width - canvasSize * 0.85f) / 2f,
                    (this.size.height - canvasSize * 0.85f) / 2f
                )
            )

            // Tick markers (speedometer style)
            if (showTicks) {
                val tickCount = 10
                for (i in 0..tickCount) {
                    val angle = Math.toRadians((startAngle + (sweepAngle / tickCount) * i).toDouble())
                    val innerRadius = radius - stroke / 2f
                    val outerRadius = innerRadius - 12f
                    
                    val startX = center.x + innerRadius * cos(angle).toFloat()
                    val startY = center.y + innerRadius * sin(angle).toFloat()
                    val endX = center.x + outerRadius * cos(angle).toFloat()
                    val endY = center.y + outerRadius * sin(angle).toFloat()
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.5f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Center content
        if (centerContent != null) {
            Box(
                modifier = Modifier.size(size * 0.6f),
                contentAlignment = Alignment.Center,
                content = centerContent
            )
        }
    }
}

/**
 * Circular data ring for HUD-style data visualization
 * Creates concentric rings with different data values - matches QuantraCore logo style
 * 
 * @param rings List of ring data (progress, color, label)
 * @param modifier Modifier for customization
 * @param size Component size
 * @param centerContent Optional content to display in center
 */
@Composable
fun CircularDataRing(
    rings: List<RingData>,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    centerContent: @Composable (BoxScope.() -> Unit)? = null
) {
    // Pre-compute all animated progress values outside Canvas
    val animatedProgressList = rings.mapIndexed { index, ringData ->
        animateFloatAsState(
            targetValue = ringData.progress.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = 1000 + index * 200,
                easing = FastOutSlowInEasing
            ),
            label = "ring_$index"
        ).value
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val strokeWidth = 8.dp.toPx()
            val ringSpacing = 16.dp.toPx()
            
            rings.forEachIndexed { index, ringData ->
                val animatedProgress = animatedProgressList[index]
                
                val ringRadius = canvasSize / 2f - (index * (strokeWidth + ringSpacing))
                val ringSize = ringRadius * 2f
                
                // Background ring
                drawArc(
                    color = Color(0xFF1A1A2E),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(ringSize, ringSize),
                    topLeft = Offset(
                        (this.size.width - ringSize) / 2f,
                        (this.size.height - ringSize) / 2f
                    )
                )
                
                // Progress arc with glow
                drawArc(
                    color = ringData.color.copy(alpha = 0.3f),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round),
                    size = Size(ringSize, ringSize),
                    topLeft = Offset(
                        (this.size.width - ringSize) / 2f,
                        (this.size.height - ringSize) / 2f
                    )
                )
                
                drawArc(
                    color = ringData.color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(ringSize, ringSize),
                    topLeft = Offset(
                        (this.size.width - ringSize) / 2f,
                        (this.size.height - ringSize) / 2f
                    )
                )
            }
        }

        // Center content
        if (centerContent != null) {
            Box(
                modifier = Modifier.size(size * 0.4f),
                contentAlignment = Alignment.Center,
                content = centerContent
            )
        }
    }
}

/**
 * Data class for CircularDataRing configuration
 * 
 * @param progress Progress value (0f to 1f)
 * @param color Ring color
 * @param label Optional label for this ring
 */
data class RingData(
    val progress: Float,
    val color: Color,
    val label: String? = null
)

// ============================================================================
// GLASS MORPHISM & ADVANCED BACKGROUNDS
// ============================================================================

/**
 * Glass morphic card with backdrop blur effect - matches QuantraCore futuristic aesthetic
 * 
 * Uses RenderEffect blur on Android 12+ with fallback to semi-transparent background
 * 
 * @param modifier Modifier for customization
 * @param onClick Optional click handler
 * @param blurRadius Backdrop blur amount (Android 12+ only)
 * @param backgroundColor Semi-transparent background color
 * @param borderColor Border color
 * @param content Card content
 */
@Composable
fun GlassMorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    blurRadius: Float = 0f,
    backgroundColor: Color = Color(0xFF0D1219).copy(alpha = 0.95f),
    borderColor: Color = NeonCyan,
    borderWidth: Dp = 1.dp,
    glowIntensity: Float = 0.6f,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
        // Clean professional background - NO visible borders
        .background(
            color = backgroundColor,
            shape = RoundedCornerShape(16.dp)
        )
        .padding(16.dp)
    
    Column(
        modifier = cardModifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/**
 * Particle starfield background - animated glowing dots for depth
 * 
 * Creates floating particle effect with random positions and subtle glow
 * Respects PerformanceProfile for low-end device optimization
 * 
 * @param modifier Modifier for customization
 * @param particleCount Number of particles (overridden by performance profile)
 * @param particleColor Base particle color
 */
@Composable
fun ParticleStarfield(
    modifier: Modifier = Modifier,
    particleCount: Int = AnimationSpecs.PARTICLE_COUNT_HIGH,
    particleColor: Color = NeonCyan
) {
    val performanceProfile = LocalPerformanceProfile.current
    
    // Don't render on low performance mode
    if (!performanceProfile.enableParticleStarfield) {
        return
    }
    
    // Use performance profile particle count
    val actualParticleCount = performanceProfile.particleCount
    
    // Remember random particle positions (stable across recompositions)
    val particles = remember(actualParticleCount) {
        List(actualParticleCount) {
            Triple(
                Math.random().toFloat(), // x (0-1)
                Math.random().toFloat(), // y (0-1)
                (Math.random() * 0.6f + 0.2f).toFloat() // alpha (0.2-0.8)
            )
        }
    }
    
    // Optional pulsing animation (disabled on low performance)
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                (AnimationSpecs.PULSE_DURATION * performanceProfile.animationFrameRateMultiplier).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particlePulse"
    )
    
    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { (x, y, alpha) ->
            val posX = size.width * x
            val posY = size.height * y
            val adjustedAlpha = (alpha * pulseAlpha).coerceIn(0f, 1f)
            
            // Outer glow
            drawCircle(
                color = particleColor.copy(alpha = adjustedAlpha * 0.3f),
                radius = 3.dp.toPx(),
                center = Offset(posX, posY)
            )
            
            // Core particle
            drawCircle(
                color = particleColor.copy(alpha = adjustedAlpha),
                radius = 1.5.dp.toPx(),
                center = Offset(posX, posY)
            )
        }
    }
}

/**
 * Radial glow background for depth and ambient lighting
 * 
 * Creates subtle radial gradient from center outward - adds depth to backgrounds
 * 
 * @param modifier Modifier for customization
 * @param glowColor Primary glow color
 * @param centerAlpha Alpha at center
 * @param edgeAlpha Alpha at edges
 */
@Composable
fun RadialGlowBackground(
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    centerAlpha: Float = 0.2f,
    edgeAlpha: Float = 0f
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = centerAlpha),
                        glowColor.copy(alpha = centerAlpha * 0.5f),
                        glowColor.copy(alpha = edgeAlpha)
                    )
                )
            )
    )
}

// ============================================================================
// GLASSMORPHIC CARD - FROSTED GLASS EFFECT
// ============================================================================

/**
 * Glassmorphic card with frosted glass effect
 * 
 * Creates translucent card with blur effect and subtle border glow
 * Perfect for non-critical settings and standard UI elements
 * 
 * @param modifier Modifier for customization
 * @param onClick Optional click handler
 * @param backgroundColor Semi-transparent background color
 * @param borderColor Glowing border color
 * @param isDark Whether to use dark variant (for developer sections)
 * @param elevation Card elevation
 * @param content Card content
 */
@Composable
fun GlassMorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
    borderColor: Color = NeonCyan,
    isDark: Boolean = false,
    elevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    val bgColor = if (isDark) {
        Color(0xFF0A0E1A).copy(alpha = 0.8f)
    } else {
        backgroundColor
    }
    
    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Subtle inner glow
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                borderColor.copy(alpha = 0.1f),
                                Color.Transparent,
                                borderColor.copy(alpha = 0.05f)
                            )
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
        ) {
            Column(content = content)
        }
    }
}

// ============================================================================
// NEON SWITCH - ENHANCED TOGGLE WITH GLOW
// ============================================================================

/**
 * Enhanced switch with neon glow effect
 * 
 * Material Design 3 switch with cyan/gold accent and subtle glow when enabled
 * 
 * @param checked Whether switch is checked
 * @param onCheckedChange Callback when toggled
 * @param modifier Modifier for customization
 * @param glowColor Glow color when enabled (default: cyan)
 * @param isPremium Whether this is a premium/PRO feature (uses gold accent)
 */
@Composable
fun NeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    isPremium: Boolean = false
) {
    val accentColor = if (isPremium) NeonGold else glowColor
    
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.drawBehind {
            if (checked) {
                // Subtle glow effect when enabled
                drawCircle(
                    color = accentColor.copy(alpha = 0.2f),
                    radius = size.maxDimension * 0.7f,
                    center = center
                )
            }
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = accentColor,
            checkedBorderColor = accentColor,
            uncheckedThumbColor = Color(0xFF666666),
            uncheckedTrackColor = Color(0xFF333333),
            uncheckedBorderColor = Color(0xFF555555)
        )
    )
}

// ============================================================================
// NEON BORDER BUTTON - BUTTON WITH GLOWING BORDER
// ============================================================================

/**
 * Button with neon border glow effect when selected
 * 
 * @param onClick Click handler
 * @param isSelected Whether button is selected
 * @param modifier Modifier for customization
 * @param glowColor Border glow color
 * @param contentPadding Button padding
 * @param content Button content
 */
@Composable
fun NeonBorderButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    glowColor: Color = NeonCyan,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .drawBehind {
                if (isSelected) {
                    // Outer glow
                    drawRoundRect(
                        color = glowColor.copy(alpha = 0.4f),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        size = Size(size.width + 4.dp.toPx(), size.height + 4.dp.toPx()),
                        topLeft = Offset(-2.dp.toPx(), -2.dp.toPx())
                    )
                    // Inner bright border
                    drawRoundRect(
                        color = glowColor.copy(alpha = 0.6f),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) glowColor.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White
                          else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}

// ============================================================================
// SHIMMER BUTTON - BUTTON WITH HOVER SHIMMER EFFECT
// ============================================================================

/**
 * Button with shimmer effect on hover/press
 * 
 * @param onClick Click handler
 * @param isSelected Whether button is selected
 * @param modifier Modifier for customization
 * @param enableShimmer Whether to enable shimmer animation
 * @param contentPadding Button padding
 * @param content Button content
 */
@Composable
fun ShimmerButton(
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    enableShimmer: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val shimmerState = rememberMetallicShimmer(enabled = enableShimmer && !isSelected)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    Button(
        onClick = onClick,
        modifier = modifier
            .then(
                if (enableShimmer && (isPressed || !isSelected)) {
                    Modifier.drawWithContent {
                        drawContent()
                        if (isPressed) {
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x44FFFFFF),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerState * size.width, 0f),
                                    end = Offset(shimmerState * size.width + size.width * 0.3f, size.height)
                                )
                            )
                        }
                    }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.onSurface
        ),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null,
        contentPadding = contentPadding,
        interactionSource = interactionSource
    ) {
        content()
    }
}
