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

    /**
     * Load pattern templates from YAML files
     * Returns list of successfully loaded templates
     * Skips corrupted templates and logs warnings
     * 
     * @throws TemplateLoadException if NO templates could be loaded (critical failure)
     */
    fun loadTemplates(): List<Template> {
        val yaml = Yaml()
        val dir = File(context.filesDir, "pattern_templates")
        
        if (!dir.exists()) {
            throw TemplateLoadException("Template directory not found: ${dir.absolutePath}. Please reinstall the app.")
        }
        
        val templates = mutableListOf<Template>()
        var totalYamlFiles = 0
        var skippedCount = 0
        
        dir.listFiles()?.filter { it.extension == "yaml" }?.forEach { file ->
            totalYamlFiles++
            try {
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
                        skippedCount++
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
            } catch (e: Exception) {
                android.util.Log.e("TemplateLibrary", "Error loading template ${file.name}", e)
                skippedCount++
            }
        }
        
        android.util.Log.i("TemplateLibrary", "Loaded ${templates.size}/${totalYamlFiles} templates ($skippedCount skipped)")
        
        // CRITICAL: Throw exception if NO templates loaded successfully
        // This indicates app corruption and requires reinstall
        if (templates.isEmpty() && totalYamlFiles > 0) {
            throw TemplateLoadException("Failed to load any templates (0/$totalYamlFiles). Pattern detection will not work. Please reinstall the app.")
        }
        
        return templates
    }

    private fun sha256(bytes: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256").digest(bytes)
        return d.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Exception thrown when template loading fails critically
 */
class TemplateLoadException(message: String) : RuntimeException(message)
