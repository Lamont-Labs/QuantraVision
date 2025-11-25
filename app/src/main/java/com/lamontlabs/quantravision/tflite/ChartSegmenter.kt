package com.lamontlabs.quantravision.tflite

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import java.io.File

/**
 * ChartSegmenter - DeepLabv3 MobileNetV2 semantic segmentation for chart analysis.
 * 
 * Model: DeepLabv3 (Apache-2.0 licensed from TensorFlow/Kaggle)
 * Input: 257x257x3 RGB image
 * Output: 257x257x21 class probabilities (PASCAL VOC classes)
 * 
 * Usage in QuantraVision:
 * - Segment chart regions (background vs. foreground elements)
 * - Isolate candlestick/bar areas from chart chrome
 * - Support pattern detection by providing region masks
 * 
 * Fail-closed: Returns null on any error; pattern detection continues without segmentation.
 */
class ChartSegmenter(context: Context) {

    companion object {
        private const val TAG = "ChartSegmenter"
        private const val MODEL_FILENAME = "models/deeplabv3_segmentation.tflite"
        private const val INPUT_SIZE = 257
        private const val NUM_CLASSES = 21
        private const val BYTES_PER_CHANNEL = 4
    }

    private val interpreter: Interpreter?
    val isEnabled: Boolean
        get() = interpreter != null

    init {
        interpreter = try {
            val assetManager = context.assets
            val modelBuffer = loadModelFromAssets(assetManager, MODEL_FILENAME)
            if (modelBuffer != null) {
                Log.i(TAG, "Loading DeepLabv3 segmentation model from assets")
                Interpreter(modelBuffer)
            } else {
                Log.w(TAG, "DeepLabv3 model not found in assets - segmentation disabled")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load DeepLabv3 model: ${e.message}", e)
            Log.w(TAG, "ChartSegmenter will be disabled due to load failure")
            null
        }
    }

    /**
     * Segment a chart image and return class mask.
     * 
     * @param bitmap Input chart image (will be resized to 257x257)
     * @return 2D array of class indices [257][257], or null on error
     */
    fun segment(bitmap: Bitmap): Array<IntArray>? {
        val interp = interpreter
        if (interp == null) {
            Log.d(TAG, "segment() called but segmenter is disabled")
            return null
        }

        return try {
            val inputBuffer = preprocessBitmap(bitmap)
            val outputBuffer = Array(1) { Array(INPUT_SIZE) { Array(INPUT_SIZE) { FloatArray(NUM_CLASSES) } } }
            
            interp.run(inputBuffer, outputBuffer)
            
            postprocessOutput(outputBuffer[0])
        } catch (e: Exception) {
            Log.e(TAG, "Segmentation failed: ${e.message}", e)
            null
        }
    }

    /**
     * Get foreground mask for chart elements.
     * Returns a binary mask where true = foreground (non-background class).
     */
    fun getForegroundMask(bitmap: Bitmap): Array<BooleanArray>? {
        val classMask = segment(bitmap) ?: return null
        return Array(INPUT_SIZE) { y ->
            BooleanArray(INPUT_SIZE) { x ->
                classMask[y][x] != 0
            }
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * BYTES_PER_CHANNEL)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaledBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            inputBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    private fun postprocessOutput(output: Array<Array<FloatArray>>): Array<IntArray> {
        return Array(INPUT_SIZE) { y ->
            IntArray(INPUT_SIZE) { x ->
                val classProbs = output[y][x]
                var maxIdx = 0
                var maxVal = classProbs[0]
                for (c in 1 until NUM_CLASSES) {
                    if (classProbs[c] > maxVal) {
                        maxVal = classProbs[c]
                        maxIdx = c
                    }
                }
                maxIdx
            }
        }
    }

    private fun loadModelFromAssets(assetManager: android.content.res.AssetManager, filename: String): ByteBuffer? {
        return try {
            val inputStream = assetManager.open(filename)
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.order(ByteOrder.nativeOrder())
            buffer.put(bytes)
            buffer.rewind()
            buffer
        } catch (e: Exception) {
            Log.w(TAG, "Could not load model from assets: ${e.message}")
            null
        }
    }

    fun close() {
        try {
            interpreter?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error closing interpreter: ${e.message}")
        }
    }
}
