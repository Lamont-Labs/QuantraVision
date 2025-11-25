package com.lamontlabs.quantravision.tflite

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

/**
 * On-device TFLite classifier for candlestick segments (optional boost).
 * Model I/O: 64x64 grayscale -> logits for {bull, bear, doji, hammer, star}.
 * Used to veto or boost template matches near candles of interest.
 * 
 * NOTE: This classifier is optional. If the model file is not present,
 * the classifier will be disabled and infer() will return null.
 */
class CandleClassifier(context: Context) {

    companion object {
        private const val TAG = "CandleClassifier"
        private const val MODEL_FILENAME = "models/candles_64x64.tflite"
    }

    private val interpreter: Interpreter?
    val isEnabled: Boolean
        get() = interpreter != null

    init {
        interpreter = try {
            val model = File(context.filesDir, MODEL_FILENAME)
            if (model.exists()) {
                Log.i(TAG, "Loading candle classifier model from: ${model.absolutePath}")
                Interpreter(loadModelFile(model))
            } else {
                Log.w(TAG, "Candle classifier model not found at: ${model.absolutePath}")
                Log.w(TAG, "CandleClassifier will be disabled - this is expected if model was not downloaded")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load candle classifier model: ${e.message}", e)
            Log.w(TAG, "CandleClassifier will be disabled due to load failure")
            null
        }
    }

    fun infer(input: FloatArray): FloatArray? {
        val interp = interpreter
        if (interp == null) {
            Log.d(TAG, "infer() called but classifier is disabled (no model)")
            return null
        }
        
        return try {
            val output = FloatArray(5)
            interp.run(input, output)
            output
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed: ${e.message}", e)
            null
        }
    }

    private fun loadModelFile(file: File): MappedByteBuffer {
        FileInputStream(file).use { fis ->
            val channel: FileChannel = fis.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
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
