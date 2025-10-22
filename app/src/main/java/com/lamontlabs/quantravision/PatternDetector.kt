package com.lamontlabs.quantravision

import android.content.Context
import android.graphics.BitmapFactory
import com.lamontlabs.quantravision.calibration.ConfidenceCalibrator
import com.lamontlabs.quantravision.detection.ConsensusEngine
import com.lamontlabs.quantravision.detection.TemporalTracker
import com.lamontlabs.quantravision.time.TimeframeEstimator
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
        val templates = templateLibrary.loadTemplates()

        dir.listFiles()?.forEach { imageFile ->
            try {
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                val input = Mat()
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
                            Imgproc.matchTemplate(scaled, tpl.image, res, Imgproc.TM_CCOEFF_NORMED)
                            val mmr = Core.minMaxLoc(res)
                            val conf = mmr.maxVal
                            if (conf >= tpl.threshold) {
                                scaleMatches.add(ScaleMatch(patternName, conf, s))
                            }
                        }
                    }

                    val consensus = ConsensusEngine.compute(patternName, scaleMatches) ?: return@forEach
                    val calibrated = ConfidenceCalibrator.calibrate(patternName, consensus.consensusScore)
                    val temporal = TemporalTracker.update("${patternName}:${imageFile.name}", calibrated, System.currentTimeMillis())

                    db.patternDao().insert(
                        PatternMatch(
                            patternName = patternName,
                            confidence = calibrated,
                            timestamp = System.currentTimeMillis(),
                            timeframe = tfLabel,
                            scale = consensus.bestScale,
                            consensusScore = consensus.consensusScore,
                            windowMs = 7000L
                        )
                    )

                    provenance.logHash(imageFile, "$patternName@${"%.2f".format(consensus.bestScale)}:${tfLabel}:c${"%.3f".format(calibrated)}:t${temporal.toBigDecimal().setScale(3, java.math.RoundingMode.HALF_UP)}")
                }

                Timber.i("Advanced detection complete for ${imageFile.name} [tf=$tfLabel]")

            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    data class ScaleMatch(val patternName: String, val confidence: Double, val scale: Double)
}
