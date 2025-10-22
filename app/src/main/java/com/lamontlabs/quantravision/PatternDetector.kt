package com.lamontlabs.quantravision

import android.content.Context
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import com.lamontlabs.quantravision.util.Box
import com.lamontlabs.quantravision.util.nms
import kotlin.math.max
import kotlin.math.roundToInt

class PatternDetector(
    private val context: Context,
    private val config: DetectionConfig = DetectionConfig()
) {
    private val templateLibrary = TemplateLibrary(context)
    private val db = PatternDatabase.getInstance(context)
    private val provenance = Provenance(context)

    suspend fun scanStaticAssets() = withContext(Dispatchers.Default) {
        val dir = File(context.filesDir, "demo_charts")
        if (!dir.exists()) return@withContext
        dir.listFiles()?.forEach { imageFile ->
            try {
                val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
                val input = Mat()
                Utils.bitmapToMat(bmp, input)

                val gray = preprocess(input)

                val matches = detectPatterns(gray)
                val pruned = nms(matches.map {
                    Box(it.x, it.y, it.w, it.h, it.confidence)
                }, config.iouThreshold)

                // Map back pruned boxes to PatternMatch entries by best IoU
                val finalMatches = pruned.map { b ->
                    val best = matches.maxBy { candidate ->
                        val interX1 = maxOf(b.x, candidate.x)
                        val interY1 = maxOf(b.y, candidate.y)
                        val interX2 = minOf(b.x + b.w, candidate.x + candidate.w)
                        val interY2 = minOf(b.y + b.h, candidate.y + candidate.h)
                        val interArea = maxOf(0, interX2 - interX1) * maxOf(0, interY2 - interY1)
                        interArea.toDouble()
                    }
                    best
                }

                for (m in finalMatches) {
                    if (m.confidence >= max(config.minConfidenceGlobal, m.templateThreshold)) {
                        db.patternDao().insert(m)
                        provenance.logHash(imageFile, m.patternName, m.scaleUsed, m.aspectUsed, m.confidence)
                    }
                }
                Timber.i("Detected ${finalMatches.size} patterns in ${imageFile.name}")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun preprocess(src: Mat): Mat {
        var gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY)
        if (config.equalizeHist) {
            Imgproc.equalizeHist(gray, gray)
        }
        return gray
    }

    data class RawMatch(
        val patternName: String,
        val confidence: Double,
        val timestamp: Long,
        val x: Int, val y: Int, val w: Int, val h: Int,
        val templateThreshold: Double,
        val scaleUsed: Double,
        val aspectUsed: Double
    ) {
        fun toEntity(): PatternMatch =
            PatternMatch(patternName = patternName, confidence = confidence, timestamp = timestamp)
    }

    private fun detectPatterns(input: Mat): List<RawMatch> {
        val out = mutableListOf<RawMatch>()
        val templates = templateLibrary.loadTemplates()
        templates.forEach { tpl ->
            val aspectTol = tpl.aspectTolerance ?: config.aspectTolerance
            val (sMin, sMax) = tpl.scaleRange
            val levels = if (config.multiScaleEnabled) config.levels else 1
            val scales = generateSequence(1.0) { it * config.scaleFactor }
                .take(levels)
                .map { it.coerceIn(sMin, sMax) }
                .toSet()
                .sortedDescending()

            for (scale in scales) {
                val tplW = (tpl.image.cols() * scale).roundToInt().coerceAtLeast(8)
                val tplH = (tpl.image.rows() * scale).roundToInt().coerceAtLeast(8)
                if (tplW >= input.cols() || tplH >= input.rows()) continue

                val resizedTpl = Mat()
                Imgproc.resize(tpl.image, resizedTpl, Size(tplW.toDouble(), tplH.toDouble()), 0.0, 0.0, Imgproc.INTER_AREA)

                val resultCols = input.cols() - resizedTpl.cols() + 1
                val resultRows = input.rows() - resizedTpl.rows() + 1
                if (resultCols <= 0 || resultRows <= 0) continue

                val result = Mat(resultRows, resultCols, CvType.CV_32FC1)
                Imgproc.matchTemplate(input, resizedTpl, result, Imgproc.TM_CCOEFF_NORMED)

                val locations = MatOfPoint()
                Imgproc.threshold(result, result, tpl.threshold - 1e-6, 1.0, Imgproc.THRESH_TOZERO)
                Core.MinMaxLocResult() // touch class to ensure linkage

                // Sweep to collect candidates above min global confidence
                val minConf = max(config.minConfidenceGlobal, tpl.threshold) - 1e-6
                for (y in 0 until result.rows()) {
                    for (x in 0 until result.cols()) {
                        val conf = result.get(y, x)[0]
                        if (conf >= minConf) {
                            out.add(
                                RawMatch(
                                    patternName = tpl.name,
                                    confidence = conf,
                                    timestamp = System.currentTimeMillis(),
                                    x = x, y = y, w = tplW, h = tplH,
                                    templateThreshold = tpl.threshold,
                                    scaleUsed = scale,
                                    aspectUsed = 1.0 // aspect sweep hook
                                )
                            )
                        }
                    }
                }
                result.release()
                resizedTpl.release()
            }
        }
        return out
    }
}
