package com.lamontlabs.quantravision.utils

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Minimum touch target size utilities per Material Design guidelines
 * 
 * Material Design 3 requires a minimum touch target size of 48dp x 48dp
 * for all interactive elements to ensure accessibility, especially for
 * users with motor impairments or using assistive technologies.
 * 
 * ## Usage
 * ```kotlin
 * IconButton(
 *     onClick = { },
 *     modifier = Modifier.minimumTouchTarget()
 * ) {
 *     Icon(Icons.Default.Menu, contentDescription = "Menu")
 * }
 * ```
 * 
 * ## References
 * - Material Design 3: Accessibility - Touch target size
 * - WCAG 2.1: Success Criterion 2.5.5 Target Size
 */
object TouchTargetUtils {
    /**
     * Minimum touch target size (48dp)
     * 
     * This is the recommended minimum size for all interactive elements
     * as per Material Design 3 and WCAG 2.1 Level AAA guidelines.
     */
    val MinTouchTargetSize = 48.dp
    
    /**
     * Ensure minimum touch target size for clickable elements
     * 
     * Applies minimum width and height constraints to ensure interactive
     * elements meet accessibility standards for touch target size.
     * 
     * @return Modifier with minimum touch target size constraints
     */
    fun Modifier.minimumTouchTarget() = this
        .widthIn(min = MinTouchTargetSize)
        .heightIn(min = MinTouchTargetSize)
}
