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

package com.lamontlabs.quantravision.pipeline

import android.graphics.Bitmap
import com.lamontlabs.quantravision.apex.models.Candle
import com.lamontlabs.quantravision.apex.models.ChartPrimitives
import com.lamontlabs.quantravision.apex.models.TrendLine
import timber.log.Timber

/**
 * PrimitivesBuilder
 * 
 * Master Spec v2.0 Primitive Extraction Layer
 * 
 * Converts VisionManager output into ChartPrimitives for Apex Engine.
 * This layer normalizes vision data into the canonical format expected
 * by the Apex protocol stack.
 * 
 * Determinism: Same VisionOutput always produces same ChartPrimitives
 */
class PrimitivesBuilder {

    companion object {
        private const val TAG = "PrimitivesBuilder"
        private const val MIN_CANDLES_FOR_ANALYSIS = 10
    }

    /**
     * Build ChartPrimitives from VisionOutput.
     * 
     * Fail-closed behavior: Returns null if visionOutput is null.
     * Does NOT generate synthetic candles - only uses actually detected candles.
     * 
     * @param visionOutput Output from VisionManager (nullable for fail-closed)
     * @param bitmap Original bitmap for dimension reference
     * @return ChartPrimitives ready for Apex Engine, or null if vision failed
     */
    fun buildPrimitives(visionOutput: VisionManager.VisionOutput?, bitmap: Bitmap): ChartPrimitives? {
        if (visionOutput == null) {
            Timber.w("$TAG: Null vision output - fail-closed")
            return null
        }
        
        Timber.d("$TAG: Building primitives from vision output")

        val candles = convertDetectedCandles(
            visionOutput.detectedCandles, 
            bitmap,
            visionOutput.imageHash
        )
        
        if (candles.size < MIN_CANDLES_FOR_ANALYSIS) {
            Timber.w("$TAG: Insufficient candles (${candles.size} < $MIN_CANDLES_FOR_ANALYSIS) - fail-closed")
            return null
        }
        
        val trendlines = convertDetectedTrendlines(visionOutput.detectedTrendlines, bitmap)

        Timber.d("$TAG: Built ${candles.size} candles, ${trendlines.size} trendlines")

        return ChartPrimitives(
            rawImageHash = visionOutput.imageHash,
            candles = candles,
            detectedLines = trendlines,
            ocrText = visionOutput.ocrText,
            chartType = visionOutput.chartType
        )
    }

    private fun convertDetectedCandles(
        detected: List<VisionManager.DetectedCandle>,
        bitmap: Bitmap,
        imageHash: String
    ): List<Candle> {
        if (detected.isEmpty()) return emptyList()

        val hashLong = imageHash.hashCode().toLong() and 0xFFFFFFFFL
        val baseTimestamp = hashLong * 1000
        val candleIntervalMs = 60_000L

        return detected.sortedBy { it.index }.mapIndexed { index, detectedCandle ->
            val normalizedY = detectedCandle.bounds.centerY() / bitmap.height
            val candleHeight = detectedCandle.bounds.height() / bitmap.height

            val basePrice = 100.0
            val priceRange = 10.0

            val centerPrice = basePrice + (1.0 - normalizedY) * priceRange
            val halfRange = (candleHeight * priceRange) / 2.0

            val (open, close) = if (detectedCandle.isBullish) {
                (centerPrice - halfRange * 0.3) to (centerPrice + halfRange * 0.3)
            } else {
                (centerPrice + halfRange * 0.3) to (centerPrice - halfRange * 0.3)
            }

            Candle(
                timestamp = baseTimestamp + (index * candleIntervalMs),
                open = roundTo3Decimals(open),
                high = roundTo3Decimals(centerPrice + halfRange),
                low = roundTo3Decimals(centerPrice - halfRange),
                close = roundTo3Decimals(close),
                volume = 1000.0 * detectedCandle.confidence
            )
        }
    }

    private fun convertDetectedTrendlines(
        detected: List<VisionManager.DetectedTrendline>,
        bitmap: Bitmap
    ): List<TrendLine> {
        if (detected.isEmpty()) return emptyList()

        return detected.map { detectedLine ->
            val x1Normalized = detectedLine.startX / bitmap.width
            val y1Normalized = 1.0 - (detectedLine.startY / bitmap.height)
            val x2Normalized = detectedLine.endX / bitmap.width
            val y2Normalized = 1.0 - (detectedLine.endY / bitmap.height)

            TrendLine(
                x1 = roundTo3Decimals(x1Normalized.toDouble() * 100),
                y1 = roundTo3Decimals(y1Normalized.toDouble() * 100),
                x2 = roundTo3Decimals(x2Normalized.toDouble() * 100),
                y2 = roundTo3Decimals(y2Normalized.toDouble() * 100),
                confidence = detectedLine.confidence.toDouble()
            )
        }
    }

    private fun roundTo3Decimals(value: Double): Double {
        return kotlin.math.round(value * 1000.0) / 1000.0
    }
}
