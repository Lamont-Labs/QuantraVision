package com.lamontlabs.quantravision.validation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.analysis.HybridPatternDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Validation framework for testing pattern detection accuracy.
 * 
 * Use this to:
 * - Test against known patterns (ground truth)
 * - Measure accuracy percentage
 * - Compare ML vs Template detection
 * - A/B test improvements
 */
class ValidationFramework(private val context: Context) {
    
    private val hybridDetector = HybridPatternDetector(context)
    private val patternDetector = PatternDetector(context)
    
    data class TestCase(
        val imageFile: File,
        val expectedPattern: String,
        val patternType: PatternType = PatternType.REVERSAL,
        val confidence: ConfidenceLevel = ConfidenceLevel.HIGH
    ) {
        enum class PatternType {
            REVERSAL,       // Head & Shoulders, Double Top/Bottom
            CONTINUATION,   // Triangles, Flags, Pennants
            BILATERAL       // Symmetrical patterns
        }
        
        enum class ConfidenceLevel {
            VERY_HIGH,  // Ground truth is certain
            HIGH,       // Clear pattern visible
            MEDIUM,     // Subjective, debatable
            LOW         // Edge case, difficult
        }
    }
    
    data class ValidationResult(
        val testCase: TestCase,
        val detected: Boolean,
        val detectedPatterns: List<String>,
        val detectionMethod: String,
        val confidence: Float,
        val correct: Boolean,
        val processingTimeMs: Long,
        val boundingBox: android.graphics.RectF? = null,
        val originPath: String? = null
    )
    
    data class AccuracyReport(
        val totalTests: Int,
        val correctDetections: Int,
        val falsePositives: Int,
        val falseNegatives: Int,
        val accuracy: Float,
        val precision: Float,
        val recall: Float,
        val f1Score: Float,
        val avgProcessingTimeMs: Long,
        val perPatternAccuracy: Map<String, PatternAccuracy>,
        val results: List<ValidationResult>
    ) {
        data class PatternAccuracy(
            val patternName: String,
            val total: Int,
            val correct: Int,
            val accuracy: Float
        )
        
        fun prettyPrint(): String {
            return buildString {
                appendLine("=" .repeat(60))
                appendLine("VALIDATION REPORT")
                appendLine("=" .repeat(60))
                appendLine()
                appendLine("Overall Metrics:")
                appendLine("  Total Tests: $totalTests")
                appendLine("  Accuracy: ${(accuracy * 100).toInt()}%")
                appendLine("  Precision: ${(precision * 100).toInt()}%")
                appendLine("  Recall: ${(recall * 100).toInt()}%")
                appendLine("  F1 Score: ${(f1Score * 100).toInt()}%")
                appendLine()
                appendLine("Detection Performance:")
                appendLine("  ✓ Correct: $correctDetections")
                appendLine("  ✗ False Positives: $falsePositives")
                appendLine("  ✗ False Negatives: $falseNegatives")
                appendLine()
                appendLine("Timing:")
                appendLine("  Avg Processing: ${avgProcessingTimeMs}ms per image")
                appendLine()
                
                if (perPatternAccuracy.isNotEmpty()) {
                    appendLine("Per-Pattern Accuracy:")
                    perPatternAccuracy.values.sortedByDescending { it.accuracy }.forEach {
                        val emoji = when {
                            it.accuracy >= 0.9f -> "🟢"
                            it.accuracy >= 0.7f -> "🟡"
                            else -> "🔴"
                        }
                        appendLine("  $emoji ${it.patternName}: ${(it.accuracy * 100).toInt()}% (${it.correct}/${it.total})")
                    }
                }
                
                appendLine()
                
                // Show bounding box statistics
                val resultsWithBounds = results.count { it.boundingBox != null }
                val resultsWithoutBounds = results.size - resultsWithBounds
                
                appendLine("Detection Location Info:")
                appendLine("  Detections with bounding boxes: $resultsWithBounds/${results.size}")
                
                if (resultsWithoutBounds > 0) {
                    appendLine("  Detections without location data: $resultsWithoutBounds/${results.size}")
                }
                
                // Show sample detections with location data
                if (resultsWithBounds > 0) {
                    val samplesWithBounds = results.filter { it.boundingBox != null }.take(5)
                    if (samplesWithBounds.isNotEmpty()) {
                        appendLine()
                        appendLine("  Sample Detections (with location):")
                        samplesWithBounds.forEach { result ->
                            val bbox = result.boundingBox!!
                            val status = if (result.correct) "✓" else "✗"
                            appendLine("    $status ${result.testCase.imageFile.name}: " +
                                "${result.detectedPatterns.firstOrNull() ?: "None"} " +
                                "at [${bbox.left.toInt()},${bbox.top.toInt()}] " +
                                "${bbox.width().toInt()}x${bbox.height().toInt()}px")
                        }
                    }
                }
                
                // Show sample detections without location data
                if (resultsWithoutBounds > 0) {
                    val samplesWithoutBounds = results.filter { it.boundingBox == null }.take(5)
                    if (samplesWithoutBounds.isNotEmpty()) {
                        appendLine()
                        appendLine("  Sample Detections (no location data):")
                        samplesWithoutBounds.forEach { result ->
                            val status = if (result.correct) "✓" else "✗"
                            appendLine("    $status ${result.testCase.imageFile.name}: " +
                                "${result.detectedPatterns.firstOrNull() ?: "None"} " +
                                "(No location data)")
                        }
                    }
                }
                
                appendLine()
                appendLine("=" .repeat(60))
            }
        }
    }
    
    /**
     * Initialize detectors
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            hybridDetector.initialize()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize detectors")
            false
        }
    }
    
    /**
     * Run validation tests on a set of test cases
     */
    suspend fun runValidation(testCases: List<TestCase>): AccuracyReport = withContext(Dispatchers.Default) {
        val results = mutableListOf<ValidationResult>()
        
        testCases.forEach { testCase ->
            try {
                val result = validateSingleCase(testCase)
                results.add(result)
                // Per-result logging is now done in validateSingleCase with location info
                
            } catch (e: Exception) {
                Timber.e(e, "Validation failed for ${testCase.imageFile.name}")
            }
        }
        
        calculateAccuracy(results)
    }
    
    /**
     * Validate a single test case
     */
    private suspend fun validateSingleCase(testCase: TestCase): ValidationResult {
        val bitmap = BitmapFactory.decodeFile(testCase.imageFile.absolutePath)
            ?: throw IllegalArgumentException("Failed to load image: ${testCase.imageFile.name}")
        
        val startTime = System.currentTimeMillis()
        
        // Run hybrid detection with actual file path for provenance tracking
        val detections = hybridDetector.detect(bitmap, testCase.imageFile.absolutePath)
        
        val processingTime = System.currentTimeMillis() - startTime
        
        // Extract detected pattern names
        val detectedPatterns = detections.map { it.patternName }
        
        // Check if expected pattern was detected
        val detected = detectedPatterns.any { 
            it.equals(testCase.expectedPattern, ignoreCase = true) ||
            normalizePatternName(it) == normalizePatternName(testCase.expectedPattern)
        }
        
        // Find best matching detection
        val bestMatch = detections.maxByOrNull { it.confidence }
        
        val correct = detected
        val detectionMethod = bestMatch?.method?.name ?: "NONE"
        val confidence = bestMatch?.confidence ?: 0f
        val boundingBox = bestMatch?.boundingBox
        val originPath = bestMatch?.metadata?.get("originPath") as? String
        
        // Log per-result information with detection location
        val locationInfo = if (boundingBox != null) {
            "at [${boundingBox.left.toInt()},${boundingBox.top.toInt()}, ${boundingBox.width().toInt()}x${boundingBox.height().toInt()}]"
        } else {
            "location unknown"
        }
        val status = if (correct) "✓" else "✗"
        Timber.i("$status ${testCase.imageFile.name}: Expected ${testCase.expectedPattern}, Got ${detectedPatterns.joinToString(", ")} $locationInfo")
        
        bitmap.recycle()
        
        return ValidationResult(
            testCase = testCase,
            detected = detected,
            detectedPatterns = detectedPatterns,
            detectionMethod = detectionMethod,
            confidence = confidence,
            correct = correct,
            processingTimeMs = processingTime,
            boundingBox = boundingBox,
            originPath = originPath
        )
    }
    
    /**
     * Calculate accuracy metrics from validation results
     */
    private fun calculateAccuracy(results: List<ValidationResult>): AccuracyReport {
        val totalTests = results.size
        val correctDetections = results.count { it.correct }
        
        // True Positives: Correctly detected the expected pattern
        val truePositives = results.count { it.correct && it.detected }
        
        // False Positives: Detected wrong pattern (detected something but not the expected pattern)
        val falsePositives = results.count { !it.correct && it.detectedPatterns.isNotEmpty() }
        
        // False Negatives: Failed to detect expected pattern
        val falseNegatives = results.count { !it.correct && !it.detected }
        
        // Metrics
        val accuracy = if (totalTests > 0) correctDetections.toFloat() / totalTests else 0f
        
        val precision = if (truePositives + falsePositives > 0) {
            truePositives.toFloat() / (truePositives + falsePositives)
        } else 0f
        
        val recall = if (truePositives + falseNegatives > 0) {
            truePositives.toFloat() / (truePositives + falseNegatives)
        } else 0f
        
        val f1Score = if (precision + recall > 0) {
            2 * (precision * recall) / (precision + recall)
        } else 0f
        
        val avgProcessingTime = if (results.isNotEmpty()) {
            results.map { it.processingTimeMs }.average().toLong()
        } else 0L
        
        // Per-pattern accuracy
        val perPatternAccuracy = results
            .groupBy { it.testCase.expectedPattern }
            .mapValues { (patternName, patternResults) ->
                val total = patternResults.size
                val correct = patternResults.count { it.correct }
                AccuracyReport.PatternAccuracy(
                    patternName = patternName,
                    total = total,
                    correct = correct,
                    accuracy = if (total > 0) correct.toFloat() / total else 0f
                )
            }
        
        return AccuracyReport(
            totalTests = totalTests,
            correctDetections = correctDetections,
            falsePositives = falsePositives,
            falseNegatives = falseNegatives,
            accuracy = accuracy,
            precision = precision,
            recall = recall,
            f1Score = f1Score,
            avgProcessingTimeMs = avgProcessingTime,
            perPatternAccuracy = perPatternAccuracy,
            results = results
        )
    }
    
    /**
     * Normalize pattern names for comparison
     * (handles variations like "Head and Shoulders" vs "Head & Shoulders")
     */
    private fun normalizePatternName(name: String): String {
        return name
            .lowercase()
            .replace("&", "and")
            .replace("-", " ")
            .replace("_", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Load test cases from a validation directory
     * 
     * Expected format: filename = "pattern-name_001.png"
     * Example: "head-and-shoulders-top_001.png"
     */
    fun loadTestCasesFromDirectory(directory: File): List<TestCase> {
        if (!directory.exists() || !directory.isDirectory) {
            Timber.w("Validation directory not found: ${directory.absolutePath}")
            return emptyList()
        }
        
        return directory.listFiles()
            ?.filter { it.extension in listOf("png", "jpg", "jpeg") }
            ?.mapNotNull { file ->
                try {
                    // Parse pattern name from filename
                    // Format: "pattern-name_###.png"
                    val nameWithoutExt = file.nameWithoutExtension
                    val patternName = nameWithoutExt
                        .substringBeforeLast("_")
                        .replace("-", " ")
                        .split(" ")
                        .joinToString(" ") { it.capitalize() }
                    
                    TestCase(
                        imageFile = file,
                        expectedPattern = patternName
                    )
                } catch (e: Exception) {
                    Timber.w("Failed to parse test case from: ${file.name}")
                    null
                }
            }
            ?: emptyList()
    }
    
    /**
     * Clean up resources
     */
    fun close() {
        hybridDetector.close()
    }
}
