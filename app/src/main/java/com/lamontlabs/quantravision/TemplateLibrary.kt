package com.lamontlabs.quantravision

import android.content.Context
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.yaml.snakeyaml.Yaml
import java.io.InputStream
import java.security.MessageDigest
import timber.log.Timber

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
     * Load pattern templates directly from APK assets.
     * Returns list of successfully loaded templates.
     * Skips corrupted templates and logs warnings.
     * 
     * Templates are loaded from assets/pattern_templates/ and cached in memory.
     * No filesystem copies needed - direct asset access with in-memory caching.
     * 
     * @throws TemplateLoadException if NO templates could be loaded (critical failure)
     */
    fun loadTemplates(): List<Template> {
        val yaml = Yaml()
        val assetManager = context.assets
        val assetPath = "pattern_templates"
        
        // List all YAML files in the asset directory
        val yamlFiles = try {
            assetManager.list(assetPath)?.filter { it.endsWith(".yaml") } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to list assets in $assetPath")
            throw TemplateLoadException("Cannot access pattern_templates in APK assets. App may be corrupted.")
        }
        
        if (yamlFiles.isEmpty()) {
            throw TemplateLoadException("No template YAML files found in assets/$assetPath. App packaging is broken.")
        }
        
        val templates = mutableListOf<Template>()
        var totalYamlFiles = yamlFiles.size
        var skippedCount = 0
        
        Timber.i("📚 Loading ${totalYamlFiles} pattern templates from assets...")
        
        yamlFiles.forEach { yamlFileName ->
            try {
                // Load and parse YAML file from assets
                assetManager.open("$assetPath/$yamlFileName").use { yamlStream ->
                    val data = yaml.load<Map<String, Any>>(yamlStream)
                    val name = data["name"] as String
                    val imagePath = data["image"] as String  // e.g., "pattern_templates/foo_ref.png"
                    val threshold = (data["threshold"] as Number).toDouble()
                    val sr = (data["scale_range"] as? List<*>)?.map { (it as Number).toDouble() } ?: listOf(0.6, 1.6)
                    val scaleMin = sr.getOrElse(0) { 0.6 }
                    val scaleMax = sr.getOrElse(1) { 1.6 }
                    val scaleStride = (data["scale_stride"] as? Number)?.toDouble() ?: 0.15
                    val aspectTol = (data["aspect_tolerance"] as? Number)?.toDouble()
                    val tfHints = (data["timeframe_hints"] as? List<*>)?.map { it.toString() } ?: emptyList()
                    val minBars = (data["min_bars"] as? Number)?.toInt() ?: 0

                    // Load image from assets and decode with OpenCV
                    val imageMat = try {
                        // Extract just the filename from the path (in case YAML has full path)
                        val imageFileName = imagePath.substringAfterLast("/")
                        val imageAssetPath = "$assetPath/$imageFileName"
                        
                        assetManager.open(imageAssetPath).use { imageStream ->
                            val imageBytes = imageStream.readBytes()
                            
                            // Decode image bytes to OpenCV Mat
                            val matOfByte = MatOfByte(*imageBytes)
                            val decodedMat = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_GRAYSCALE)
                            matOfByte.release()  // Release temporary buffer
                            
                            if (decodedMat.empty()) {
                                Timber.w("⚠️ SKIPPED: Failed to decode image $imageAssetPath")
                                null
                            } else {
                                decodedMat
                            }
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "⚠️ SKIPPED: Cannot load image asset for $name")
                        null
                    }
                    
                    if (imageMat == null) {
                        skippedCount++
                        return@forEach
                    }

                    // Compute hash from image bytes for integrity checking
                    val imageBytes = assetManager.open("$assetPath/${imagePath.substringAfterLast("/")}").use { it.readBytes() }
                    val tplHash = sha256(imageBytes)
                    
                    templates.add(
                        Template(
                            name = name,
                            path = imagePath,
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
                Timber.e(e, "❌ Error loading template $yamlFileName")
                skippedCount++
            }
        }
        
        Timber.i("✅ Loaded ${templates.size}/${totalYamlFiles} templates ($skippedCount skipped)")
        
        // CRITICAL: Throw exception if NO templates loaded successfully
        // This indicates app corruption or broken asset packaging
        if (templates.isEmpty()) {
            throw TemplateLoadException("Failed to load any templates (0/$totalYamlFiles). Pattern detection will not work. App packaging is broken.")
        }
        
        // VALIDATION: Expect ~109 templates (each with PNG + YAML = 218+ files)
        if (templates.size < 50) {
            Timber.w("⚠️ WARNING: Only ${templates.size} templates loaded - expected ~109. Some templates may be missing.")
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
