package com.lamontlabs.quantravision.analysis

import android.graphics.*
import android.os.Build
import androidx.camera.core.ImageProxy
import java.lang.reflect.Method

/**
 * LegendOCR (offline-first)
 * - Attempts on-device OCR for legend tokens like "EMA(50)", "RSI(14)", "VWAP", "BB(20,2)".
 * - Uses ML Kit Text Recognition if present (via reflection to avoid hard dependency).
 * - Falls back to a conservative whitelist matcher when OCR is unavailable.
 *
 * Completely local. No network calls.
 */
interface LegendOCR {
    fun load()
    suspend fun analyze(frame: ImageProxy): List<String>
}

class LegendOCROffline : LegendOCR {

    // Reflection handles to ML Kit if available at runtime
    private var mlkitAvailable = false
    private var textRecognitionGetClient: Method? = null
    private var inputImageFromBitmap: Method? = null
    private var recognizerProcess: Method? = null
    private var closeMethod: Method? = null
    private var recognizerInstance: Any? = null

    override fun load() {
        mlkitAvailable = tryInitMlKit()
    }

    override suspend fun analyze(frame: ImageProxy): List<String> {
        val bmp = imageProxyToBitmap(frame) ?: run {
            frame.close()
            return emptyList()
        }

        try {
            // Crop a top-left band where legends usually appear (y ∈ [0..15%], x ∈ [0..65%])
            val h = bmp.height
            val w = bmp.width
            val crop = Rect(0, 0, (w * 0.65f).toInt().coerceAtLeast(1), (h * 0.15f).toInt().coerceAtLeast(1))
            val legendBmp = try { Bitmap.createBitmap(bmp, crop.left, crop.top, crop.width(), crop.height()) } catch (_: Throwable) { null }
            frame.close()
            if (legendBmp == null) return emptyList()

            try {
                // Try ML Kit first
                val rawText = if (mlkitAvailable) runCatching { recognizeWithMlKit(legendBmp) }.getOrDefault("") else ""

                val tokens = tokenizeLegendText(rawText)
                return if (tokens.isNotEmpty()) tokens else
                    // Fallback: heuristic whitelist scan (very conservative)
                    heuristicLegendGuess(legendBmp)
            } finally {
                legendBmp.recycle()
            }
        } finally {
            bmp.recycle()
        }
    }

    // ---------------- internals ----------------

    /** Attempt to wire ML Kit Text Recognition via reflection. */
    private fun tryInitMlKit(): Boolean = try {
        // com.google.mlkit.vision.text.TextRecognition
        val trClass = Class.forName("com.google.mlkit.vision.text.TextRecognition")
        textRecognitionGetClient = trClass.getMethod("getClient", Class.forName("com.google.mlkit.vision.text.latin.TextRecognizerOptions"))
        val optClass = Class.forName("com.google.mlkit.vision.text.latin.TextRecognizerOptions\$Builder")
        val opt = optClass.getDeclaredConstructor().newInstance()
        val build = optClass.getMethod("build").invoke(opt)
        recognizerInstance = textRecognitionGetClient!!.invoke(null, build)

        // com.google.mlkit.vision.common.InputImage.fromBitmap
        val inputImageClass = Class.forName("com.google.mlkit.vision.common.InputImage")
        inputImageFromBitmap = inputImageClass.getMethod("fromBitmap", Bitmap::class.java, Int::class.javaPrimitiveType)

        // recognizer.process(InputImage)
        val recognizerInterface = Class.forName("com.google.mlkit.vision.text.TextRecognizer")
        recognizerProcess = recognizerInterface.getMethod("process", inputImageClass)

        // Close method (optional)
        closeMethod = try { recognizerInterface.getMethod("close") } catch (_: Throwable) { null }

        true
    } catch (_: Throwable) { false }

    /** Run ML Kit OCR, return plain text (single string). */
    @Suppress("UNCHECKED_CAST")
    private suspend fun recognizeWithMlKit(bmp: Bitmap): String {
        if (recognizerInstance == null || inputImageFromBitmap == null || recognizerProcess == null) return ""
        val input = inputImageFromBitmap!!.invoke(null, bmp, 0)
        val task = recognizerProcess!!.invoke(recognizerInstance, input) // returns com.google.android.gms.tasks.Task<Text>
        // Minimal Task await without importing Tasks.await:
        val textResult = suspendTaskAwait(task)
        // textResult.getText()
        val textClass = Class.forName("com.google.mlkit.vision.text.Text")
        val getText = textClass.getMethod("getText")
        val full = getText.invoke(textResult) as String
        return full ?: ""
    }

    /** Suspend until a GMS Task completes using reflection. */
    private suspend fun suspendTaskAwait(task: Any): Any? = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
        try {
            val taskClass = Class.forName("com.google.android.gms.tasks.Task")
            val addSuccessListener = taskClass.getMethod("addOnSuccessListener", Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            val addFailureListener = taskClass.getMethod("addOnFailureListener", Class.forName("com.google.android.gms.tasks.OnFailureListener"))

            val successListenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                taskClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnSuccessListener"))
            ) { _, _, args ->
                if (!cont.isCompleted) cont.resume(args?.get(0), null)
                null
            }
            val failureListenerProxy = java.lang.reflect.Proxy.newProxyInstance(
                taskClass.classLoader,
                arrayOf(Class.forName("com.google.android.gms.tasks.OnFailureListener"))
            ) { _, _, args ->
                if (!cont.isCompleted) cont.resumeWith(Result.failure((args?.get(0) as? Throwable) ?: RuntimeException("OCR failure")))
                null
            }

            addSuccessListener.invoke(task, successListenerProxy)
            addFailureListener.invoke(task, failureListenerProxy)
        } catch (e: Throwable) {
            if (!cont.isCompleted) cont.resumeWith(Result.failure(e))
        }
    }

    /** Extract tokens like EMA(50), SMA(200), BB(20,2), VWAP, RSI(14), MACD(12,26,9). */
    private fun tokenizeLegendText(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val t = text.replace("\n", " ").replace("\r", " ")
        val regexes = listOf(
            Regex("""\bEMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bSMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bWMA\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bBB\(\d{1,3}\s*,\s*\d(\.\d+)?\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bVWAP\b""", RegexOption.IGNORE_CASE),
            Regex("""\bRSI\(\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bMACD\(\d{1,3}\s*,\s*\d{1,3}\s*,\s*\d{1,3}\)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bIchimoku\b""", RegexOption.IGNORE_CASE),
            Regex("""\bVolume\b|\bVOL\b""", RegexOption.IGNORE_CASE)
        )
        val out = mutableSetOf<String>()
        regexes.forEach { r -> r.findAll(t).forEach { m -> out.add(m.value.uppercase()) } }
        return out.toList()
    }

    /** Heuristic backup when OCR not available: returns common defaults conservatively empty. */
    private fun heuristicLegendGuess(@Suppress("UNUSED_PARAMETER") bmp: Bitmap): List<String> {
        // Safe fallback: no guesses to avoid false claims.
        return emptyList()
    }

    // --- utilities ---

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? = try {
        val yuv = imageToNV21(image) ?: return null
        val yuvImage = YuvImage(yuv, ImageFormat.NV21, image.width, image.height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
        val bytes = out.toByteArray()
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Throwable) { null }

    private fun imageToNV21(image: ImageProxy): ByteArray? {
        if (image.planes.size < 3) return null
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // Guard against integer overflow in buffer size calculation
        val totalSize = ySize.toLong() + uSize.toLong() + vSize.toLong()
        if (totalSize > Int.MAX_VALUE || totalSize <= 0) return null
        
        val nv21 = ByteArray(totalSize.toInt())
        yBuffer.get(nv21, 0, ySize)

        // NV21 = Y + VU
        val pixelStride = image.planes[2].pixelStride
        val rowStride = image.planes[2].rowStride
        val uvWidth = image.width / 2
        val uvHeight = image.height / 2
        var offset = ySize
        val vBytes = ByteArray(vSize).also { vBuffer.get(it) }
        val uBytes = ByteArray(uSize).also { uBuffer.get(it) }

        var i = 0
        while (i < uvHeight) {
            var j = 0
            while (j < uvWidth) {
                val vuIndex = i * rowStride + j * pixelStride
                if (vuIndex + 1 < vBytes.size && vuIndex + 1 < uBytes.size && offset + 1 < nv21.size) {
                    nv21[offset++] = vBytes[vuIndex]
                    nv21[offset++] = uBytes[vuIndex]
                }
                j++
            }
            i++
        }
        return nv21
    }
}
```0
