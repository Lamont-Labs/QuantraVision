package com.lamontlabs.quantravision

import android.content.Context
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

data class Template(
    val name: String,
    val path: String,
    val threshold: Double,
    val image: Mat,
    val scaleMin: Double,
    val scaleMax: Double,
    val scaleStride: Double,
    val aspectTolerance: Double?,
    val timeframeHints: List<String>,
    val minBars: Int,
    val tplHash: String
)

class TemplateLibrary(private val context: Context) {

    fun loadTemplates(): List<Template> {
        val yaml = Yaml()
        val dir = File(context.filesDir, "pattern_templates")
        val templates = mutableListOf<Template>()
        dir.listFiles()?.filter { it.extension == "yaml" }?.forEach { file ->
            FileInputStream(file).use { fis ->
                val data = yaml.load<Map<String, Any>>(fis)
                val name = data["name"] as String
                val path = data["image"] as String
                val threshold = (data["threshold"] as Number).toDouble()
                val sr = (data["scale_range"] as? List<*>)?.map { (it as Number).toDouble() } ?: listOf(0.6, 1.6)
                val scaleMin = sr.getOrElse(0) { 0.6 }
                val scaleMax = sr.getOrElse(1) { 1.6 }
                val scaleStride = (data["scale_stride"] as? Number)?.toDouble() ?: 0.15
                val aspectTol = (data["aspect_tolerance"] as? Number)?.toDouble()
                val tfHints = (data["timeframe_hints"] as? List<*>)?.map { it.toString() } ?: emptyList()
                val minBars = (data["min_bars"] as? Number)?.toInt() ?: 0

                val imageFile = File(context.filesDir, path)
                val imageMat = Imgcodecs.imread(imageFile.absolutePath, Imgcodecs.IMREAD_GRAYSCALE)
                
                // CRITICAL: Check if image loaded successfully before creating template
                // If empty, skip this template instead of crashing the entire app
                if (imageMat.empty()) {
                    android.util.Log.w("TemplateLibrary", "SKIPPED: Template image not found or empty: $path (file: ${imageFile.absolutePath})")
                    // Skip this template but continue loading others
                    return@forEach
                }

                val tplHash = sha256(imageFile.readBytes())
                templates.add(
                    Template(
                        name = name,
                        path = path,
                        threshold = threshold,
                        image = imageMat,
                        scaleMin = scaleMin,
                        scaleMax = scaleMax,
                        scaleStride = scaleStride,
                        aspectTolerance = aspectTol,
                        timeframeHints = tfHints,
                        minBars = minBars,
                        tplHash = tplHash
                    )
                )
            }
        }
        return templates
    }

    private fun sha256(bytes: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(bytes)
        return d.joinToString("") { "%02x".format(it) }
    }
}
