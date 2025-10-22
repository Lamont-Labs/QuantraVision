package com.lamontlabs.quantravision.util

data class Box(val x: Int, val y: Int, val w: Int, val h: Int, val score: Double)

fun iou(a: Box, b: Box): Double {
    val x1 = maxOf(a.x, b.x)
    val y1 = maxOf(a.y, b.y)
    val x2 = minOf(a.x + a.w, b.x + b.w)
    val y2 = minOf(a.y + a.h, b.y + b.h)
    val inter = maxOf(0, x2 - x1) * maxOf(0, y2 - y1)
    val union = a.w * a.h + b.w * b.h - inter
    return if (union <= 0) 0.0 else inter.toDouble() / union.toDouble()
}

/** Greedy Non-Maximum Suppression */
fun nms(boxes: List<Box>, iouThreshold: Double): List<Box> {
    val sorted = boxes.sortedByDescending { it.score }.toMutableList()
    val kept = mutableListOf<Box>()
    while (sorted.isNotEmpty()) {
        val current = sorted.removeAt(0)
        kept.add(current)
        val iter = sorted.iterator()
        while (iter.hasNext()) {
            val b = iter.next()
            if (iou(current, b) >= iouThreshold) iter.remove()
        }
    }
    return kept
}
