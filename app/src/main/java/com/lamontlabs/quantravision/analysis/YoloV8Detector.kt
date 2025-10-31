package com.lamontlabs.quantravision.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * YOLOv8-based stock pattern detector using pre-trained model.
 * Detects 6 chart-agnostic patterns with high confidence.
 * 
 * Patterns: Head & Shoulders (top/bottom), Triangle, M_Head, W_Bottom, StockLine
 * 
 * Model: foduucom/stockmarket-pattern-detection-yolov8 from HuggingFace
 * Performance: 93.2% mAP@0.5, ~20ms inference time
 */
class YoloV8Detector(private val context: Context) {
    
    private var interpreter: Interpreter? = null
    private var isModelLoaded = false
    
    companion object {
        const val MODEL_FILE = "models/stockmarket-pattern-yolov8.tflite"
        const val INPUT_SIZE = 640
        const val CONFIDENCE_THRESHOLD = 0.25f
        const val IOU_THRESHOLD = 0.45f
        const val MAX_DETECTIONS = 100
        
        // Pattern class labels (order matches model training)
        val PATTERN_LABELS = listOf(
            "Head and Shoulders Bottom",
            "Head and Shoulders Top",
            "M_Head",
            "StockLine",
            "Triangle",
            "W_Bottom"
        )
    }
    
    data class MLDetection(
        val patternName: String,
        val confidence: Float,
        val boundingBox: RectF,
        val classIndex: Int
    )
    
    /**
     * Load TFLite model from assets.
     * Gracefully handles missing model file.
     */
    fun loadModel(): Boolean {
        return try {
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE)
            
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                // Use GPU delegate if available
                try {
                    addDelegate(org.tensorflow.lite.gpu.GpuDelegate())
                } catch (e: Exception) {
                    Timber.w("GPU delegate not available, using CPU")
                }
            }
            
            interpreter = Interpreter(modelBuffer, options)
            isModelLoaded = true
            Timber.i("YOLOv8 model loaded successfully: $MODEL_FILE")
            true
        } catch (e: Exception) {
            Timber.w("YOLOv8 model not found or failed to load: ${e.message}")
            Timber.w("Falling back to template-based detection only")
            isModelLoaded = false
            false
        }
    }
    
    /**
     * Run inference on a chart screenshot.
     * Returns list of detected patterns with confidence scores.
     */
    suspend fun detect(bitmap: Bitmap): List<MLDetection> = withContext(Dispatchers.Default) {
        if (!isModelLoaded || interpreter == null) {
            return@withContext emptyList()
        }
        
        try {
            // Preprocess image
            val inputBuffer = preprocessImage(bitmap)
            
            // Run inference
            val outputMap = runInference(inputBuffer)
            
            // Post-process results (NMS, threshold filtering)
            val detections = postprocess(outputMap, bitmap.width, bitmap.height)
            
            detections
        } catch (e: Exception) {
            Timber.e(e, "YOLOv8 inference failed")
            emptyList()
        }
    }
    
    /**
     * Preprocess bitmap to YOLOv8 input format: 640x640 RGB, normalized 0-1
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        
        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        
        for (pixel in pixels) {
            // Extract RGB channels, normalize to 0-1
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        
        return inputBuffer
    }
    
    /**
     * Run TFLite inference
     */
    private fun runInference(inputBuffer: ByteBuffer): Map<Int, Any> {
        val outputMap = mutableMapOf<Int, Any>()
        
        // YOLOv8 output: [batch, num_detections, 85]
        // Format: [x_center, y_center, width, height, confidence, class_probs...]
        val output = Array(1) { Array(25200) { FloatArray(85) } }
        
        outputMap[0] = output
        
        interpreter?.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)
        
        return outputMap
    }
    
    /**
     * Post-process YOLOv8 output: apply NMS, filter by confidence
     */
    private fun postprocess(
        outputMap: Map<Int, Any>,
        originalWidth: Int,
        originalHeight: Int
    ): List<MLDetection> {
        val output = outputMap[0] as Array<Array<FloatArray>>
        val detections = mutableListOf<MLDetection>()
        
        for (i in 0 until output[0].size) {
            val detection = output[0][i]
            
            // Extract confidence and class scores
            val objectConfidence = detection[4]
            if (objectConfidence < CONFIDENCE_THRESHOLD) continue
            
            // Find best class
            val classScores = detection.sliceArray(5 until detection.size)
            val bestClassIndex = classScores.indices.maxByOrNull { classScores[it] } ?: continue
            val classConfidence = classScores[bestClassIndex]
            val finalConfidence = objectConfidence * classConfidence
            
            if (finalConfidence < CONFIDENCE_THRESHOLD) continue
            
            // Extract bounding box (normalize to original image size)
            val xCenter = detection[0] / INPUT_SIZE * originalWidth
            val yCenter = detection[1] / INPUT_SIZE * originalHeight
            val width = detection[2] / INPUT_SIZE * originalWidth
            val height = detection[3] / INPUT_SIZE * originalHeight
            
            val left = xCenter - width / 2
            val top = yCenter - height / 2
            val right = xCenter + width / 2
            val bottom = yCenter + height / 2
            
            detections.add(
                MLDetection(
                    patternName = PATTERN_LABELS.getOrNull(bestClassIndex) ?: "Unknown",
                    confidence = finalConfidence,
                    boundingBox = RectF(left, top, right, bottom),
                    classIndex = bestClassIndex
                )
            )
        }
        
        // Apply Non-Maximum Suppression
        return applyNMS(detections)
    }
    
    /**
     * Non-Maximum Suppression to remove duplicate detections
     */
    private fun applyNMS(detections: List<MLDetection>): List<MLDetection> {
        if (detections.isEmpty()) return emptyList()
        
        val sortedDetections = detections.sortedByDescending { it.confidence }
        val selected = mutableListOf<MLDetection>()
        
        for (detection in sortedDetections) {
            var shouldAdd = true
            
            for (selectedDetection in selected) {
                val iou = calculateIoU(detection.boundingBox, selectedDetection.boundingBox)
                if (iou > IOU_THRESHOLD && detection.classIndex == selectedDetection.classIndex) {
                    shouldAdd = false
                    break
                }
            }
            
            if (shouldAdd) {
                selected.add(detection)
            }
            
            if (selected.size >= MAX_DETECTIONS) break
        }
        
        return selected
    }
    
    /**
     * Calculate Intersection over Union (IoU) for two bounding boxes
     */
    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)
        
        val intersectionWidth = maxOf(0f, intersectionRight - intersectionLeft)
        val intersectionHeight = maxOf(0f, intersectionBottom - intersectionTop)
        val intersectionArea = intersectionWidth * intersectionHeight
        
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)
        val unionArea = box1Area + box2Area - intersectionArea
        
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
    }
}
