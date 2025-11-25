/*
 * Copyright (c) 2025 Lamont Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lamontlabs.quantravision.vision

import android.content.Context
import timber.log.Timber

/**
 * ModelFallback
 * 
 * Master Spec v2.0 Section 8.10 - Model Fallback System
 * 
 * Provides no-op fallback behavior when models are unavailable.
 * This ensures the build never fails due to missing models.
 * 
 * Target models (v2.0):
 * - canny_edge.tflite: Trendlines, wicks, edges
 * - hed.tflite: Contour detection for candles
 * - mobilenet_v3.tflite: Chart segment classification
 * - unet_small.tflite: Support/resistance zone detection
 * - tiny_line.tflite: Trendline detection
 * 
 * Fallback Rule: If any model fails to download or load, 
 * use no-op stub returning empty primitives. Never fail build.
 */
object ModelFallback {
    
    private const val TAG = "ModelFallback"
    
    data class ModelStatus(
        val modelId: String,
        val available: Boolean,
        val fallbackActive: Boolean,
        val reason: String?
    )
    
    private val targetModels = listOf(
        "canny_edge",
        "hed",
        "mobilenet_v3",
        "unet_small",
        "tiny_line"
    )
    
    private val legacyModels = listOf(
        "detector_ssd_mobilenet_v1_quant",
        "deeplabv3_segmentation",
        "sentence_embeddings"
    )
    
    /**
     * Check if a model is available in assets.
     */
    fun isModelAvailable(context: Context, modelId: String): Boolean {
        val filename = "${modelId}.tflite"
        return try {
            context.assets.open("models/$filename").use { true }
        } catch (e: Exception) {
            Timber.d("$TAG: Model $modelId not available: ${e.message}")
            false
        }
    }
    
    /**
     * Get status of all target v2.0 models.
     */
    fun getTargetModelStatus(context: Context): List<ModelStatus> {
        return targetModels.map { modelId ->
            val available = isModelAvailable(context, modelId)
            ModelStatus(
                modelId = modelId,
                available = available,
                fallbackActive = !available,
                reason = if (!available) "Model not yet provisioned" else null
            )
        }
    }
    
    /**
     * Get status of all legacy models.
     */
    fun getLegacyModelStatus(context: Context): List<ModelStatus> {
        return legacyModels.map { modelId ->
            val available = isModelAvailable(context, modelId)
            ModelStatus(
                modelId = modelId,
                available = available,
                fallbackActive = !available,
                reason = if (!available) "Legacy model missing" else null
            )
        }
    }
    
    /**
     * Check if any model is using fallback mode.
     */
    fun isAnyFallbackActive(context: Context): Boolean {
        return getTargetModelStatus(context).any { it.fallbackActive } ||
               getLegacyModelStatus(context).any { it.fallbackActive }
    }
    
    /**
     * Get list of models in fallback mode.
     */
    fun getFallbackModels(context: Context): List<String> {
        val targetFallbacks = getTargetModelStatus(context)
            .filter { it.fallbackActive }
            .map { it.modelId }
        
        val legacyFallbacks = getLegacyModelStatus(context)
            .filter { it.fallbackActive }
            .map { it.modelId }
        
        return targetFallbacks + legacyFallbacks
    }
    
    /**
     * Log model availability status for diagnostics.
     */
    fun logModelStatus(context: Context) {
        Timber.i("$TAG: === Model Availability Report ===")
        
        Timber.i("$TAG: Target Models (v2.0):")
        getTargetModelStatus(context).forEach { status ->
            val indicator = if (status.available) "OK" else "FALLBACK"
            Timber.i("$TAG:   ${status.modelId}: $indicator")
        }
        
        Timber.i("$TAG: Legacy Models:")
        getLegacyModelStatus(context).forEach { status ->
            val indicator = if (status.available) "OK" else "FALLBACK"
            Timber.i("$TAG:   ${status.modelId}: $indicator")
        }
        
        if (isAnyFallbackActive(context)) {
            Timber.w("$TAG: Some models in fallback mode - using no-op stubs")
        } else {
            Timber.i("$TAG: All models available")
        }
    }
}

/**
 * NoOpModelStub
 * 
 * Provides empty/safe outputs when real models are unavailable.
 * This is the fallback behavior for fail-safe operation.
 */
object NoOpModelStub {
    
    /**
     * No-op edge detection result.
     */
    data class EdgeResult(
        val edges: List<FloatArray> = emptyList(),
        val confidence: Float = 0f
    )
    
    /**
     * No-op contour detection result.
     */
    data class ContourResult(
        val contours: List<FloatArray> = emptyList(),
        val confidence: Float = 0f
    )
    
    /**
     * No-op classification result.
     */
    data class ClassificationResult(
        val label: String = "unknown",
        val confidence: Float = 0f
    )
    
    /**
     * No-op segmentation result.
     */
    data class SegmentationResult(
        val mask: IntArray = IntArray(0),
        val classes: List<Int> = emptyList()
    )
    
    /**
     * No-op line detection result.
     */
    data class LineResult(
        val lines: List<FloatArray> = emptyList(),
        val confidence: Float = 0f
    )
    
    fun emptyEdgeResult() = EdgeResult()
    fun emptyContourResult() = ContourResult()
    fun emptyClassificationResult() = ClassificationResult()
    fun emptySegmentationResult() = SegmentationResult()
    fun emptyLineResult() = LineResult()
}
