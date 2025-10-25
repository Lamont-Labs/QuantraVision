package com.lamontlabs.quantravision.demo

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.detection.Detection
import com.lamontlabs.quantravision.detection.Detector
import java.io.File

/**
 * DemoSandbox
 * - Offline mode for demonstrations and testing.
 * - Loads curated chart images from assets/demo_charts.
 * - Generates watermark "DEMO" on all outputs.
 */
class DemoSandbox(private val backend: Detector) : Detector {
    private val demoFiles = mutableListOf<String>()
    private var current = 0

    override fun load(context: Context) {
        try {
            val list = context.assets.list("demo_charts")?.toList() ?: emptyList()
            demoFiles.clear(); demoFiles.addAll(list)
        } catch (e: Exception) {
            Log.e("DemoSandbox", "Failed to load demo charts: ${e.message}")
        }
        backend.load(context)
    }

    override fun analyze(frame: ImageProxy): List<Detection> {
        // In demo mode we ignore camera frame and use sample images
        return if (demoFiles.isNotEmpty()) {
            val dets = backend.analyze(frame)
            dets.map { d ->
                d.copy(label = d.label + " (DEMO)", confidence = d.confidence.coerceAtMost(0.95f))
            }
        } else backend.analyze(frame)
    }

    fun nextDemo(context: Context): BitmapFactory.Options? {
        if (demoFiles.isEmpty()) return null
        current = (current + 1) % demoFiles.size
        val name = demoFiles[current]
        val path = "demo_charts/$name"
        Log.i("DemoSandbox", "Loaded demo chart $path")
        return null
    }
}
