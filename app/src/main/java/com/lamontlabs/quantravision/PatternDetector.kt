package com.lamontlabs.quantravision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lamontlabs.quantravision.alerts.AlertManager
import com.lamontlabs.quantravision.calibration.ConfidenceCalibrator
import com.lamontlabs.quantravision.detection.ConsensusEngine
import com.lamontlabs.quantravision.detection.TemporalTracker
import com.lamontlabs.quantravision.time.TimeframeEstimator
import com.lamontlabs.quantravision.prediction.PatternPredictionEngine
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.File

class PatternDetector(private val context: Context) {

    private val templateLibrary = TemplateLibrary(context)
    private val db = PatternDatabase.getInstance(context)
    private val provenance = Provenance(context)
    
    // Cache loaded templates to prevent reloading on every detection call
    @Volatile
    private var cachedTemplates: List<Template>? = null
    private val templateLock = Any()

    /**
     * Load templates once and cache them for reuse.
     * Thread-safe lazy loading with double-checked locking.
     */
    private fun getTemplates(): List<Template> {
        cachedTemplates?.let { return it }
        
        synchronized(templateLock) {
            cachedTemplates?.let { return it }
            
            val loaded = try {
                templateLibrary.loadTemplates()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load templates")
                emptyList()
            }
            
            cachedTemplates = loaded
            Timber.i("Templates loaded and cached: ${loaded.size} patterns")
            return loaded
        }
    }
    
    suspend fun scanStaticAssets() = withContext(Dispatchers.Default) {
        val dir = File(context.filesDir, "demo_charts")
        if (!dir.exists()) return@withContext
        
        // Use cached templates
        val templates = getTemplates()
        if (templates.isEmpty()) {
            Timber.w("No templates available for scanning")
            return@withContext
        }

        dir.listFiles()?.forEach { imageFile ->
            try {
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bmp == null) {
                    Timber.w("Failed to decode image: ${imageFile.name}")
                    return@forEach
                }
                try {
                    val input = Mat()
                    try {
                        Utils.bitmapToMat(bmp, input)
                        Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)

                        val est = TimeframeEstimator.estimateFromBitmap(bmp)
                        val tfLabel = est.timeframe.label
                        val grouped = templates.groupBy { it.name }

                    grouped.forEach { (patternName, family) ->
                        val scaleMatches = mutableListOf<ScaleMatch>()
                        family.forEach { tpl ->
                            val cfg = ScaleSpace.ScaleConfig(tpl.scaleMin, tpl.scaleMax, tpl.scaleStride)
                            for (s in ScaleSpace.scales(cfg)) {
                                val scaled = ScaleSpace.resizeForScale(input, s)
                                val res = Mat()
                                try {
                                    Imgproc.matchTemplate(scaled, tpl.image, res, Imgproc.TM_CCOEFF_NORMED)
                                    val mmr = Core.minMaxLoc(res)
                                    val conf = mmr.maxVal
                                    if (conf >= tpl.threshold) {
                                        scaleMatches.add(ScaleMatch(
                                            patternName = patternName,
                                            confidence = conf,
                                            scale = s,
                                            matchX = mmr.maxLoc.x,
                                            matchY = mmr.maxLoc.y,
                                            templateWidth = tpl.image.cols().toDouble(),
                                            templateHeight = tpl.image.rows().toDouble()
                                        ))
                                    }
                                } finally {
                                    res.release()
                                    scaled.release()
                                }
                            }
                        }

                    val consensus = ConsensusEngine.compute(patternName, scaleMatches) ?: return@forEach
                    val calibrated = ConfidenceCalibrator.calibrate(patternName, consensus.consensusScore)
                    val temporal = TemporalTracker.update("${patternName}:${imageFile.name}", calibrated, System.currentTimeMillis())

                    // Find best match for bounding box
                    val bestMatch = scaleMatches.maxByOrNull { it.confidence }
                    val detectionBounds = bestMatch?.let {
                        "${it.matchX.toInt()},${it.matchY.toInt()},${it.templateWidth.toInt()},${it.templateHeight.toInt()}"
                    }

                    val match = PatternMatch(
                        patternName = patternName,
                        confidence = calibrated,
                        timestamp = System.currentTimeMillis(),
                        timeframe = tfLabel,
                        scale = consensus.bestScale,
                        consensusScore = consensus.consensusScore,
                        windowMs = 7000L,
                        originPath = "demo/${imageFile.name}",
                        detectionBounds = detectionBounds
                    )
                    
                    db.patternDao().insert(match)

                    // Trigger alerts for detected pattern
                    AlertManager.getInstance(context).onPatternDetected(match)

                    // Integrate with new features
                    com.lamontlabs.quantravision.integration.FeatureIntegration.onPatternDetected(context, match)

                        provenance.logHash(imageFile, "$patternName@${"%.2f".format(consensus.bestScale)}:${tfLabel}:c${"%.3f".format(calibrated)}:t${temporal.toBigDecimal().setScale(3, java.math.RoundingMode.HALF_UP)}")
                    }

                        Timber.i("Advanced detection complete for ${imageFile.name} [tf=$tfLabel]")
                    } finally {
                        input.release()
                    }
                } finally {
                    bmp.recycle()
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        
        // Run pattern predictions after detection completes (Pro-only feature)
        runPredictions()
    }
    
    /**
     * Detect patterns from a single Bitmap image.
     * Uses multi-scale template matching with consensus and calibration.
     * Returns structured results instead of storing in database.
     * 
     * @param bitmap The chart image to analyze
     * @param originPath The source file path or identifier for provenance tracking
     * @return List of detected pattern matches
     */
    suspend fun detectFromBitmap(bitmap: Bitmap, originPath: String = "unknown"): List<PatternMatch> = withContext(Dispatchers.Default) {
        val results = mutableListOf<PatternMatch>()
        
        // Use cached templates to avoid reloading on every call
        val templates = getTemplates()
        if (templates.isEmpty()) {
            Timber.w("No templates available for detection")
            return@withContext emptyList()
        }
        
        val input = Mat()
        try {
            // Convert bitmap to grayscale Mat
            Utils.bitmapToMat(bitmap, input)
            Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)
            
            // Estimate timeframe from chart
            val est = TimeframeEstimator.estimateFromBitmap(bitmap)
            val tfLabel = est.timeframe.label
            
            // Group templates by pattern name
            val grouped = templates.groupBy { it.name }
            
            // Detect each pattern
            grouped.forEach { (patternName, family) ->
                val scaleMatches = mutableListOf<ScaleMatch>()
                
                // Multi-scale template matching
                family.forEach { tpl ->
                    val cfg = ScaleSpace.ScaleConfig(tpl.scaleMin, tpl.scaleMax, tpl.scaleStride)
                    for (s in ScaleSpace.scales(cfg)) {
                        val scaled = ScaleSpace.resizeForScale(input, s)
                        val res = Mat()
                        try {
                            Imgproc.matchTemplate(scaled, tpl.image, res, Imgproc.TM_CCOEFF_NORMED)
                            val mmr = Core.minMaxLoc(res)
                            val conf = mmr.maxVal
                            if (conf >= tpl.threshold) {
                                scaleMatches.add(ScaleMatch(
                                    patternName = patternName,
                                    confidence = conf,
                                    scale = s,
                                    matchX = mmr.maxLoc.x,
                                    matchY = mmr.maxLoc.y,
                                    templateWidth = tpl.image.cols().toDouble(),
                                    templateHeight = tpl.image.rows().toDouble()
                                ))
                            }
                        } finally {
                            res.release()
                            scaled.release()
                        }
                    }
                }
                
                // Guard against empty scaleMatches
                if (scaleMatches.isEmpty()) {
                    Timber.w("No scale matches found for pattern: $patternName (threshold not met)")
                    return@forEach
                }
                
                // Compute consensus across scales
                val consensus = ConsensusEngine.compute(patternName, scaleMatches) ?: return@forEach
                
                // Calibrate confidence
                val calibrated = ConfidenceCalibrator.calibrate(patternName, consensus.consensusScore)
                
                // Apply temporal tracking with stable content-based ID
                val timestamp = System.currentTimeMillis()
                val stableId = "${patternName}:validation_${timestamp}"
                val temporal = TemporalTracker.update(stableId, calibrated, timestamp)
                
                // Find best match for bounding box
                val bestMatch = scaleMatches.maxByOrNull { it.confidence }
                val detectionBounds = bestMatch?.let {
                    "${it.matchX.toInt()},${it.matchY.toInt()},${it.templateWidth.toInt()},${it.templateHeight.toInt()}"
                }
                
                // Log warning if bounds are missing
                if (detectionBounds == null) {
                    Timber.w("Pattern detected but bounding box unavailable: $patternName (no best match found)")
                }
                
                // Create pattern match result
                val match = PatternMatch(
                    patternName = patternName,
                    confidence = calibrated,
                    timestamp = timestamp,
                    timeframe = tfLabel,
                    scale = consensus.bestScale,
                    consensusScore = consensus.consensusScore,
                    windowMs = 7000L,
                    originPath = originPath,
                    detectionBounds = detectionBounds
                )
                
                results.add(match)
                
                // Trigger alerts for detected pattern
                AlertManager.getInstance(context).onPatternDetected(match)
                
                Timber.d("Detected: $patternName (conf=${String.format("%.3f", calibrated)}, scale=${String.format("%.2f", consensus.bestScale)})")
            }
            
            Timber.i("Detection complete: ${results.size} patterns found [tf=$tfLabel]")
            
        } catch (e: Exception) {
            Timber.e(e, "Detection from bitmap failed")
        } finally {
            input.release()
        }
        
        results
    }
    
    private suspend fun runPredictions() = withContext(Dispatchers.IO) {
        try {
            // Check Pro access - predictions are Pro-only
            if (!ProFeatureGate.isActive(context)) {
                Timber.d("Skipping predictions - Pro feature not active")
                return@withContext
            }
            
            // Get recent matches from last 30 seconds for prediction analysis
            val thirtySecondsAgo = System.currentTimeMillis() - 30000L
            val recentMatches = db.patternDao().getRecent(thirtySecondsAgo)
            
            if (recentMatches.isEmpty()) {
                Timber.d("No recent matches for prediction analysis")
                return@withContext
            }
            
            // Run prediction engine to find forming patterns (40-85% complete)
            val formingPatterns = PatternPredictionEngine.predictForming(
                context = context,
                recentMatches = recentMatches,
                partialConfidenceThreshold = 0.4
            )
            
            // Calculate formation velocity for each pattern
            val velocities = PatternPredictionEngine.analyzeFormationVelocity(
                matches = recentMatches,
                timeWindowMs = 30000L
            )
            
            // Store predictions in database
            val predictedDao = db.predictedPatternDao()
            
            // Clean up old predictions (older than 1 hour)
            val oneHourAgo = System.currentTimeMillis() - 3600000L
            predictedDao.deleteOld(oneHourAgo)
            
            formingPatterns.forEach { forming ->
                // Only store patterns that are 40-85% formed
                if (forming.completionPercent >= 40.0 && forming.completionPercent <= 85.0) {
                    val velocity = velocities[forming.patternName] ?: 0.0
                    
                    val prediction = PredictedPattern(
                        patternName = forming.patternName,
                        completionPercent = forming.completionPercent,
                        confidence = forming.confidence,
                        timestamp = System.currentTimeMillis(),
                        timeframe = "unknown", // Will be updated when matched to specific chart
                        estimatedCompletion = forming.estimatedCompletion,
                        stage = forming.stage,
                        formationVelocity = velocity
                    )
                    
                    predictedDao.insert(prediction)
                    
                    // Track prediction statistics in FeatureIntegration
                    com.lamontlabs.quantravision.integration.FeatureIntegration.onPredictionGenerated(
                        context, prediction
                    )
                    
                    Timber.i("Prediction: ${forming.patternName} at ${forming.completionPercent.toInt()}% (${forming.stage})")
                }
            }
            
            Timber.i("Predictions complete: ${formingPatterns.size} forming patterns detected")
            
        } catch (e: Exception) {
            // Error handling - predictions shouldn't break detection
            Timber.e(e, "Prediction error (non-fatal)")
        }
    }

    data class ScaleMatch(
        val patternName: String, 
        val confidence: Double, 
        val scale: Double,
        val matchX: Double = 0.0,
        val matchY: Double = 0.0,
        val templateWidth: Double = 0.0,
        val templateHeight: Double = 0.0
    )
    
    fun getAlertManager(): AlertManager = AlertManager.getInstance(context)
    
    fun getDatabase(): PatternDatabase = db
    
    fun setVoiceEnabled(enabled: Boolean) {
        AlertManager.getInstance(context).setVoiceEnabled(enabled)
    }
    
    fun setHapticEnabled(enabled: Boolean) {
        AlertManager.getInstance(context).setHapticEnabled(enabled)
    }
    
    fun isVoiceEnabled(): Boolean = AlertManager.getInstance(context).isVoiceEnabled()
    
    fun isHapticEnabled(): Boolean = AlertManager.getInstance(context).isHapticEnabled()
}
