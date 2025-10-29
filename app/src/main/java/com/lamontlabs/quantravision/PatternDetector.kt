package com.lamontlabs.quantravision

import android.content.Context
import android.graphics.BitmapFactory
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

    suspend fun scanStaticAssets() = withContext(Dispatchers.Default) {
        val dir = File(context.filesDir, "demo_charts")
        if (!dir.exists()) return@withContext
        
        // Load templates with error handling
        val templates = try {
            templateLibrary.loadTemplates()
        } catch (e: Exception) {
            Timber.e(e, "Failed to load templates")
            return@withContext
        }

        dir.listFiles()?.forEach { imageFile ->
            try {
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                if (bmp == null) {
                    Timber.w("Failed to decode image: ${imageFile.name}")
                    return@forEach
                }
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
                                        scaleMatches.add(ScaleMatch(patternName, conf, s))
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

                    val match = PatternMatch(
                        patternName = patternName,
                        confidence = calibrated,
                        timestamp = System.currentTimeMillis(),
                        timeframe = tfLabel,
                        scale = consensus.bestScale,
                        consensusScore = consensus.consensusScore,
                        windowMs = 7000L
                    )
                    
                    db.patternDao().insert(match)

                    // Integrate with new features
                    com.lamontlabs.quantravision.integration.FeatureIntegration.onPatternDetected(context, match)

                    provenance.logHash(imageFile, "$patternName@${"%.2f".format(consensus.bestScale)}:${tfLabel}:c${"%.3f".format(calibrated)}:t${temporal.toBigDecimal().setScale(3, java.math.RoundingMode.HALF_UP)}")
                }

                    Timber.i("Advanced detection complete for ${imageFile.name} [tf=$tfLabel]")
                } finally {
                    input.release()
                }

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        
        // Run pattern predictions after detection completes (Pro-only feature)
        runPredictions()
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

    data class ScaleMatch(val patternName: String, val confidence: Double, val scale: Double)
}
