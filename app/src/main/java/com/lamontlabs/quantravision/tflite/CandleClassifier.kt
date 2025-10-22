package com.lamontlabs.quantravision.tflite

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream

/**
 * On-device TFLite classifier for candlestick segments (optional boost).
 * Model I/O: 64x64 grayscale -> logits for {bull, bear, doji, hammer, star}.
 * Used to veto or boost template matches near candles of interest.
 */
class CandleClassifier(context: Context) {

    private val interpreter: Interpreter

    init {
        val model = File(context.filesDir, "models/candles_64x64.tflite")
        require(model.exists()) { "Missing TFLite model: ${model.absolutePath}" }
        interpreter = Interpreter(loadModelFile(model))
    }

    fun infer(input: FloatArray): FloatArray {
        val output = FloatArray(5)
        interpreter.run(input, output)
        return output
    }

    private fun loadModelFile(file: File): MappedByteBuffer {
        FileInputStream(file).use { fis ->
            val channel: FileChannel = fis.channel
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
        }
    }
}
