package com.lamontlabs.quantravision.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Deterministic overlay renderer for bounding boxes, trendlines, labels, and heatmaps.
 * Input coordinates must already be in screen space (pixels). No network, no randomness.
 */
@Composable
fun OverlayRenderer(
    regions: List<RenderRegion>,
    heatmap: Heatmap? = null,
    opacity: Float = 0.35f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Heatmap first
        heatmap?.let { drawHeatmap(it, opacity = opacity * 0.9f) }
        // Regions and labels
        regions.sortedByDescending { it.confidence }.forEach { r ->
            drawRegion(r, opacity = opacity)
        }
    }
}

data class RenderRegion(
    val patternName: String,
    val confidence: Double,           // 0.0..1.0
    val rect: RectPx,                 // bounding rect in px
    val lines: List<LinePx> = emptyList(), // optional trendlines
    val color: Color = Color(0xFF00BFFF)
)

data class RectPx(val left: Int, val top: Int, val right: Int, val bottom: Int)
data class LinePx(val x1: Int, val y1: Int, val x2: Int, val y2: Int)

data class Heatmap(
    val widthPx: Int,
    val heightPx: Int,
    /** intensity in [0,1] row-major of size widthPx*heightPx, coarse grid allowed */
    val intensity: FloatArray
)

private fun DrawScope.drawRegion(r: RenderRegion, opacity: Float) {
    val stroke = Stroke(width = 2.dp.toPx())
    val rectColor = r.color.copy(alpha = opacity.coerceIn(0f, 1f))
    val x0 = r.rect.left.toFloat()
    val y0 = r.rect.top.toFloat()
    val x1 = r.rect.right.toFloat()
    val y1 = r.rect.bottom.toFloat()

    // Box
    drawRect(
        color = rectColor,
        topLeft = Offset(x0, y0),
        size = androidx.compose.ui.geometry.Size(x1 - x0, y1 - y0),
        style = stroke
    )
    // Fill with very low alpha
    drawRect(
        color = rectColor.copy(alpha = (opacity * 0.25f).coerceAtMost(0.25f)),
        topLeft = Offset(x0, y0),
        size = androidx.compose.ui.geometry.Size(x1 - x0, y1 - y0)
    )

    // Trendlines
    val lineStroke = Stroke(width = 1.5.dp.toPx())
    r.lines.forEach { l ->
        drawLine(
            color = rectColor,
            start = Offset(l.x1.toFloat(), l.y1.toFloat()),
            end = Offset(l.x2.toFloat(), l.y2.toFloat()),
            strokeWidth = lineStroke.width
        )
    }

    // Label ribbon
    val label = "${r.patternName} ${(r.confidence * 100).roundToInt()}%"
    drawLabelRibbon(label, x0, y0, rectColor)
}

private fun DrawScope.drawHeatmap(hm: Heatmap, opacity: Float) {
    val cellW = size.width / hm.widthPx
    val cellH = size.height / hm.heightPx
    val maxAlpha = (opacity * 0.55f).coerceIn(0f, 0.7f)
    var idx = 0
    for (y in 0 until hm.heightPx) {
        for (x in 0 until hm.widthPx) {
            val v = hm.intensity[idx++].coerceIn(0f, 1f)
            if (v <= 0f) continue
            val a = (v * maxAlpha)
            drawRect(
                color = Color(0xFF00BFFF).copy(alpha = a),
                topLeft = Offset(x * cellW, y * cellH),
                size = androidx.compose.ui.geometry.Size(cellW, cellH)
            )
        }
    }
}

private fun DrawScope.drawLabelRibbon(text: String, x: Float, y: Float, color: Color) {
    val padX = 6.dp.toPx()
    val padY = 3.dp.toPx()
    val paint = android.graphics.Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12.dp.toPx()
        color = android.graphics.Color.WHITE
    }
    val textWidth = paint.measureText(text)
    val textHeight = paint.fontMetrics.let { it.bottom - it.top }
    val w = textWidth + 2 * padX
    val h = textHeight + 2 * padY

    // Ribbon shape
    val path = Path().apply {
        moveTo(x, y - h)
        lineTo(x + w, y - h)
        lineTo(x + w + 10f, y - h / 2f)
        lineTo(x + w, y)
        lineTo(x, y)
        close()
    }
    drawPath(path, color = color.copy(alpha = 0.9f))

    // Draw text via native canvas for crispness
    drawContext.canvas.nativeCanvas.drawText(
        text,
        x + padX,
        y - (h / 2f) + (textHeight / 3f),
        paint
    )
}
