package com.lamontlabs.quantravision.analysis

import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * LegendOCR (production-ready)
 * - On-device OCR for chart legend tokens like "EMA(50)", "RSI(14)", "VWAP", "BB(20,2)".
 * - Uses ML Kit Text Recognition with direct API integration.
 * - Falls back gracefully when OCR fails or is unavailable.
 * - Completely local. No network calls.
 */
interface LegendOCR {
    fun load()
    suspend fun analyze(frame: ImageProxy): List<String>
    suspend fun analyze(bitmap: Bitmap): List<String>
    fun close()
}

class LegendOCROffline : LegendOCR {

    private val tag = "LegendOCR"
    private var recognizer: TextRecognizer? = null
    private var isInitialized = false

    override fun load() {
        try {
            val options = TextRecognizerOptions.Builder().build()
            recognizer = TextRecognition.getClient(options)
            isInitialized = true
            Log.d(tag, "ML Kit Text Recognizer initialized successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize ML Kit Text Recognizer", e)
            isInitialized = false
        }
    }

    override suspend fun analyze(frame: ImageProxy): List<String> {
        val bmp = imageProxyToBitmap(frame) ?: run {
            Log.w(tag, "Failed to convert ImageProxy to Bitmap")
            return emptyList()
        }

        try {
            return analyze(bmp)
        } finally {
            bmp.recycle()
        }
    }

    override suspend fun analyze(bitmap: Bitmap): List<String> {
        try {
            val h = bitmap.height
            val w = bitmap.width
            val crop = Rect(
                0, 
                0, 
                (w * 0.65f).toInt().coerceAtLeast(1), 
                (h * 0.15f).toInt().coerceAtLeast(1)
            )
            
            val legendBmp = try {
                Bitmap.createBitmap(bitmap, crop.left, crop.top, crop.width(), crop.height())
            } catch (e: Exception) {
                Log.w(tag, "Failed to crop legend region", e)
                null
            }
            
            if (legendBmp == null) return emptyList()

            val rawText = if (isInitialized && recognizer != null) {
                runCatching { recognizeWithMlKit(legendBmp) }.getOrElse { error ->
                    Log.e(tag, "OCR failed", error)
                    ""
                }
            } else {
                Log.w(tag, "ML Kit not initialized, skipping OCR")
                ""
            }

            val tokens = tokenizeLegendText(rawText)
            Log.d(tag, "Extracted ${tokens.size} legend tokens: $tokens")
            return tokens
        } catch (e: Exception) {
            Log.e(tag, "Error during legend analysis", e)
            return emptyList()
        }
    }

    override fun close() {
        recognizer?.close()
        recognizer = null
        isInitialized = false
        Log.d(tag, "ML Kit Text Recognizer closed")
    }

    private suspend fun recognizeWithMlKit(bmp: Bitmap): String {
        val recognizerInstance = recognizer ?: return ""
        
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromBitmap(bmp, 0)
                val task: Task<Text> = recognizerInstance.process(inputImage)
                
                task.addOnSuccessListener { text ->
                    if (continuation.isActive) {
                        continuation.resume(text.text)
                    }
                }.addOnFailureListener { error ->
                    if (continuation.isActive) {
                        Log.e(tag, "ML Kit processing failed", error)
                        continuation.resumeWithException(error)
                    }
                }
                
                continuation.invokeOnCancellation {
                    Log.d(tag, "OCR task cancelled")
                }
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    private fun tokenizeLegendText(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        
        val normalized = text.replace("\n", " ").replace("\r", " ")
        val regexes = listOf(
            Regex("""\bEMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bSMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bWMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bBB\(\d{1,3}\s*,\s*\d(\.\d+)?\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bVWAP\b""", RegexOption.IGNORE_CASE),
            Regex("""\bRSI\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bMACD\(\d{1,3}\s*,\s*\d{1,3}\s*,\s*\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bIchimoku\b""", RegexOption.IGNORE_CASE),
            Regex("""\bVolume\b|\bVOL\b""", RegexOption.IGNORE_CASE),
            Regex("""\bStochastic\b|\bStoch\b""", RegexOption.IGNORE_CASE),
            Regex("""\bATR\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bADX\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE)
        )
        
        val tokens = mutableSetOf<String>()
        regexes.forEach { regex ->
            regex.findAll(normalized).forEach { match ->
                tokens.add(match.value.uppercase())
            }
        }
        
        return tokens.toList()
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? = try {
        val yuv = imageToNV21(image) ?: return null
        val yuvImage = YuvImage(yuv, ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val bytes = out.toByteArray()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        Log.e(tag, "Error converting ImageProxy to Bitmap", e)
        null
    }

    private fun imageToNV21(image: ImageProxy): ByteArray? {
        if (image.planes.size < 3) return null
        
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

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
}
