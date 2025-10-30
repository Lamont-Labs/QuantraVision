package com.lamontlabs.quantravision.analysis

import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import com.lamontlabs.quantravision.detection.Detection
import kotlinx.coroutines.runBlocking
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * IndicatorDetector
 * - Reads common indicators rendered by charting apps without APIs.
 * - Sources: legend OCR hits, color/style cues, subpanel geometry.
 * - Output integrates with overlay labels and tradeability logic.
 *
 * Detected types:
 *  - MA_SMA, MA_EMA, MA_WMA
 *  - BOLLINGER
 *  - VWAP
 *  - RSI, MACD
 *  - VOLUME
 *  - ICHIMOKU
 */
interface IndicatorDetector {
    fun load()
    fun analyze(frame: ImageProxy): List<IndicatorHit>
}

enum class IndicatorType {
    MA_SMA, MA_EMA, MA_WMA,
    BOLLINGER, VWAP,
    RSI, MACD, VOLUME, ICHIMOKU, UNKNOWN
}

data class IndicatorHit(
    val type: IndicatorType,
    val label: String,          // e.g., "EMA(50)", "BB(20,2)"
    val confidence: Float,      // 0..1
    val panel: Panel = Panel.MAIN
)

enum class Panel { MAIN, LOWER_1, LOWER_2 }

/**
 * SimpleIndicatorDetector
 * - Heuristic fusion: legend tokens + style cues + panel inference
 * - Pure on-device; wire your local OCR in recognizeLegend()
 */
class SimpleIndicatorDetector(private val context: android.content.Context) : IndicatorDetector {

    private val legendTokens = listOf(
        "SMA","EMA","WMA","MA","Moving Average","Bollinger","BB",
        "VWAP","RSI","MACD","Ichimoku","Volume","VOL"
    )
    
    private val legendOCR = LegendOCROffline()

    override fun load() {
        legendOCR.load()
    }

    override fun analyze(frame: ImageProxy): List<IndicatorHit> {
        val bitmap = imageProxyToBitmap(frame)
        
        if (bitmap == null) {
            frame.close()
            Log.w("IndicatorDetector", "Failed to convert ImageProxy to Bitmap")
            return emptyList()
        }
        
        try {
            val legend = recognizeLegend(bitmap)
            val hitsFromLegend = legend.flatMap { token -> mapLegendToken(token) }

            val cues = detectVisualCues(bitmap)

            val fused = mutableMapOf<String, IndicatorHit>()
            (hitsFromLegend + cues).forEach { hit ->
                val key = hit.type.name + ":" + hit.label + ":" + hit.panel.name
                val prev = fused[key]
                fused[key] = if (prev == null) hit else {
                    if (hit.confidence >= prev.confidence) hit else prev
                }
            }

            return fused.values.map { it.copy(confidence = it.confidence.coerceIn(0f,1f)) }
        } finally {
            bitmap.recycle()
            frame.close()
        }
    }

    private fun recognizeLegend(bitmap: Bitmap): List<String> {
        return try {
            runBlocking { legendOCR.analyze(bitmap) }
        } catch (e: Exception) {
            Log.w("IndicatorDetector", "OCR failed: ${e.message}")
            emptyList()
        }
    }

    private fun mapLegendToken(tok: String): List<IndicatorHit> {
        val t = tok.lowercase()
        return when {
            "ema" in t -> listOf(IndicatorHit(IndicatorType.MA_EMA, tok, 0.95f, Panel.MAIN))
            "sma" in t || "moving average" in t || ("ma(" in t && "ema" !in t && "wma" !in t) ->
                listOf(IndicatorHit(IndicatorType.MA_SMA, tok, 0.92f, Panel.MAIN))
            "wma" in t -> listOf(IndicatorHit(IndicatorType.MA_WMA, tok, 0.90f, Panel.MAIN))
            "bollinger" in t || "bb(" in t -> listOf(IndicatorHit(IndicatorType.BOLLINGER, tok, 0.92f, Panel.MAIN))
            "vwap" in t -> listOf(IndicatorHit(IndicatorType.VWAP, tok, 0.93f, Panel.MAIN))
            "rsi" in t -> listOf(IndicatorHit(IndicatorType.RSI, tok, 0.94f, Panel.LOWER_1))
            "macd" in t -> listOf(IndicatorHit(IndicatorType.MACD, tok, 0.94f, Panel.LOWER_1))
            "vol" in t || "volume" in t -> listOf(IndicatorHit(IndicatorType.VOLUME, tok, 0.90f, Panel.LOWER_2))
            "ichimoku" in t -> listOf(IndicatorHit(IndicatorType.ICHIMOKU, tok, 0.91f, Panel.MAIN))
            else -> emptyList()
        }
    }

    private fun detectVisualCues(bitmap: Bitmap): List<IndicatorHit> {
        val tag = "IndicatorDetector"
        val results = mutableListOf<IndicatorHit>()
        
        try {
            val mat = bitmapToMat(bitmap) ?: return results
            val gray = Mat()
            Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
            
            val height = mat.rows()
            val width = mat.cols()
            
            val mainPanelEnd = (height * 0.70).toInt()
            val lower1End = (height * 0.85).toInt()
            
            val mainPanel = gray.submat(0, mainPanelEnd, 0, width)
            val lower1Panel = if (height > mainPanelEnd) {
                gray.submat(mainPanelEnd, min(lower1End, height), 0, width)
            } else null
            val lower2Panel = if (height > lower1End) {
                gray.submat(lower1End, height, 0, width)
            } else null
            
            detectLinesInPanel(mainPanel)?.let { lineCount ->
                when {
                    lineCount in 1..2 -> results.add(
                        IndicatorHit(IndicatorType.MA_EMA, "MA", 0.65f, Panel.MAIN)
                    )
                    lineCount in 3..4 -> results.add(
                        IndicatorHit(IndicatorType.MA_SMA, "MA", 0.60f, Panel.MAIN)
                    )
                }
            }
            
            if (detectBollingerBands(mainPanel)) {
                results.add(IndicatorHit(IndicatorType.BOLLINGER, "BB", 0.70f, Panel.MAIN))
            }
            
            if (detectThickLine(mainPanel)) {
                results.add(IndicatorHit(IndicatorType.VWAP, "VWAP", 0.60f, Panel.MAIN))
            }
            
            if (detectCloudPattern(mainPanel)) {
                results.add(IndicatorHit(IndicatorType.ICHIMOKU, "Ichimoku", 0.65f, Panel.MAIN))
            }
            
            lower1Panel?.let { panel ->
                if (detectOscillatorPattern(panel)) {
                    results.add(IndicatorHit(IndicatorType.RSI, "RSI", 0.55f, Panel.LOWER_1))
                }
            }
            
            lower2Panel?.let { panel ->
                if (detectVolumePattern(panel)) {
                    results.add(IndicatorHit(IndicatorType.VOLUME, "Volume", 0.75f, Panel.LOWER_2))
                }
            }
            
            mainPanel.release()
            lower1Panel?.release()
            lower2Panel?.release()
            gray.release()
            mat.release()
            
        } catch (e: Exception) {
            Log.e(tag, "Error detecting visual cues", e)
        }
        
        return results
    }
    
    private fun bitmapToMat(bitmap: Bitmap): Mat? {
        return try {
            val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC4)
            val buffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(buffer)
            buffer.rewind()
            mat.put(0, 0, buffer.array())
            mat
        } catch (e: Exception) {
            Log.e("IndicatorDetector", "Error converting Bitmap to Mat", e)
            null
        }
    }
    
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? = try {
        val yuv = imageToNV21(image) ?: return null
        val yuvImage = YuvImage(yuv, ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val bytes = out.toByteArray()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e("IndicatorDetector", "Error converting ImageProxy to Bitmap", e)
        null
    }

    private fun imageToNV21(image: ImageProxy): ByteArray? {
        if (image.planes.size < 3) return null
        
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        yBuffer.rewind()
        uBuffer.rewind()
        vBuffer.rewind()

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val totalSize = ySize.toLong() + uSize.toLong() + vSize.toLong()
        if (totalSize > Int.MAX_VALUE || totalSize <= 0) return null
        
        val nv21 = ByteArray(totalSize.toInt())
        yBuffer.get(nv21, 0, ySize)

        val pixelStride = image.planes[2].pixelStride
        val rowStride = image.planes[2].rowStride
        val uvWidth = image.width / 2
        val uvHeight = image.height / 2
        var offset = ySize
        val vBytes = ByteArray(vSize).also { vBuffer.get(it) }
        val uBytes = ByteArray(uSize).also { uBuffer.get(it) }

        for (i in 0 until uvHeight) {
            for (j in 0 until uvWidth) {
                val vuIndex = i * rowStride + j * pixelStride
                if (vuIndex + 1 < vBytes.size && vuIndex + 1 < uBytes.size && offset + 1 < nv21.size) {
                    nv21[offset++] = vBytes[vuIndex]
                    nv21[offset++] = uBytes[vuIndex]
                }
            }
        }
        return nv21
    }
    
    private fun detectLinesInPanel(panel: Mat): Int? {
        return try {
            val edges = Mat()
            Imgproc.Canny(panel, edges, 50.0, 150.0)
            
            val lines = Mat()
            Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 30.0, 10.0)
            
            val horizontalLines = mutableListOf<DoubleArray>()
            for (i in 0 until lines.rows()) {
                val line = lines.get(i, 0)
                val x1 = line[0]
                val y1 = line[1]
                val x2 = line[2]
                val y2 = line[3]
                
                val angle = Math.atan2(abs(y2 - y1), abs(x2 - x1)) * 180 / Math.PI
                if (angle < 20) {
                    horizontalLines.add(line)
                }
            }
            
            edges.release()
            lines.release()
            
            horizontalLines.size
        } catch (e: Exception) {
            null
        }
    }
    
    private fun detectBollingerBands(panel: Mat): Boolean {
        return try {
            val lines = detectLinesInPanel(panel) ?: return false
            lines >= 2
        } catch (e: Exception) {
            false
        }
    }
    
    private fun detectThickLine(panel: Mat): Boolean {
        return try {
            val blurred = Mat()
            Imgproc.GaussianBlur(panel, blurred, Size(5.0, 5.0), 0.0)
            
            val edges = Mat()
            Imgproc.Canny(blurred, edges, 30.0, 100.0)
            
            val nonZeroCount = Core.countNonZero(edges)
            val totalPixels = edges.rows() * edges.cols()
            val edgeRatio = nonZeroCount.toFloat() / totalPixels
            
            blurred.release()
            edges.release()
            
            edgeRatio in 0.02f..0.08f
        } catch (e: Exception) {
            false
        }
    }
    
    private fun detectCloudPattern(panel: Mat): Boolean {
        return try {
            val blurred = Mat()
            Imgproc.GaussianBlur(panel, blurred, Size(7.0, 7.0), 0.0)
            
            val mean = Core.mean(blurred).`val`[0]
            val stdDev = Mat()
            val meanMat = Mat()
            Core.meanStdDev(blurred, meanMat, stdDev)
            
            val variance = stdDev.get(0, 0)[0]
            
            blurred.release()
            stdDev.release()
            meanMat.release()
            
            variance > 20.0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun detectOscillatorPattern(panel: Mat): Boolean {
        return try {
            val lines = Mat()
            Imgproc.HoughLines(panel, lines, 1.0, Math.PI / 180, 30)
            
            val hasHorizontalMidline = lines.rows() >= 1
            lines.release()
            
            hasHorizontalMidline
        } catch (e: Exception) {
            false
        }
    }
    
    private fun detectVolumePattern(panel: Mat): Boolean {
        return try {
            val edges = Mat()
            Imgproc.Canny(panel, edges, 50.0, 150.0)
            
            val lines = Mat()
            Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 20, 5.0, 3.0)
            
            var verticalCount = 0
            for (i in 0 until lines.rows()) {
                val line = lines.get(i, 0)
                val x1 = line[0]
                val y1 = line[1]
                val x2 = line[2]
                val y2 = line[3]
                
                val angle = Math.atan2(abs(y2 - y1), abs(x2 - x1)) * 180 / Math.PI
                if (angle > 70) {
                    verticalCount++
                }
            }
            
            edges.release()
            lines.release()
            
            verticalCount >= 10
        } catch (e: Exception) {
            false
        }
    }
}
