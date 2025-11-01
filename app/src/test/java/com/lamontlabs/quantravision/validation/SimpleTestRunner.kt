package com.lamontlabs.quantravision.validation

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Simple test runner for quick validation testing.
 * 
 * Usage:
 * ```kotlin
 * val runner = SimpleTestRunner(context)
 * val report = runner.runQuickTest()
 * println(report.prettyPrint())
 * ```
 */
class SimpleTestRunner(private val context: Context) {
    
    private val framework = ValidationFramework(context)
    
    /**
     * Run quick validation test with images in validation directory
     */
    suspend fun runQuickTest(
        validationDir: File = File(context.filesDir, "validation_images")
    ): ValidationFramework.AccuracyReport = withContext(Dispatchers.IO) {
        
        Timber.i("Initializing detectors...")
        framework.initialize()
        
        Timber.i("Loading test cases from: ${validationDir.absolutePath}")
        val testCases = framework.loadTestCasesFromDirectory(validationDir)
        
        if (testCases.isEmpty()) {
            Timber.w("No test cases found. Add images to: ${validationDir.absolutePath}")
            Timber.w("Format: pattern-name_001.png (e.g., head-and-shoulders-top_001.png)")
            
            return@withContext ValidationFramework.AccuracyReport(
                totalTests = 0,
                correctDetections = 0,
                falsePositives = 0,
                falseNegatives = 0,
                accuracy = 0f,
                precision = 0f,
                recall = 0f,
                f1Score = 0f,
                avgProcessingTimeMs = 0,
                perPatternAccuracy = emptyMap(),
                results = emptyList()
            )
        }
        
        Timber.i("Found ${testCases.size} test cases")
        Timber.i("Running validation tests...")
        
        val report = framework.runValidation(testCases)
        
        Timber.i("Validation complete!")
        Timber.i(report.prettyPrint())
        
        framework.close()
        
        report
    }
    
    /**
     * Run test on a single image file
     */
    suspend fun testSingleImage(
        imageFile: File,
        expectedPattern: String
    ): ValidationFramework.ValidationResult = withContext(Dispatchers.IO) {
        
        framework.initialize()
        
        val testCase = ValidationFramework.TestCase(
            imageFile = imageFile,
            expectedPattern = expectedPattern
        )
        
        Timber.i("Testing: ${imageFile.name} (expecting: $expectedPattern)")
        
        val report = framework.runValidation(listOf(testCase))
        val result = report.results.first()
        
        val status = if (result.correct) "✓ PASS" else "✗ FAIL"
        Timber.i("$status - Detected: ${result.detectedPatterns}, Confidence: ${result.confidence}")
        
        framework.close()
        
        result
    }
    
    /**
     * Create example test directory structure with instructions
     */
    fun createTestDirectoryTemplate(): File {
        val validationDir = File(context.filesDir, "validation_images")
        validationDir.mkdirs()
        
        // Create README file with instructions
        val readmeFile = File(validationDir, "README.txt")
        readmeFile.writeText("""
            QuantraVision Validation Image Directory
            =========================================
            
            How to Add Test Images:
            
            1. Take screenshots of charts with known patterns
               (TradingView, MetaTrader, etc.)
            
            2. Name files: pattern-name_###.png
               
               Examples:
               - head-and-shoulders-top_001.png
               - double-top_001.png
               - triangle_001.png
               - cup-and-handle_001.png
            
            3. Place files in this directory
            
            4. Run validation test:
               val runner = SimpleTestRunner(context)
               val report = runner.runQuickTest()
               println(report.prettyPrint())
            
            Supported Patterns:
            -------------------
            - Head and Shoulders (Top/Bottom)
            - Double Top/Bottom
            - Triple Top/Bottom
            - Triangle (Ascending/Descending/Symmetrical)
            - Wedge (Rising/Falling)
            - Flag/Pennant
            - Cup and Handle
            - And 100+ more!
            
            Tips for Good Test Images:
            --------------------------
            ✓ Clear, well-formed patterns
            ✓ Good contrast (dark or light theme OK)
            ✓ Minimal indicators/overlays
            ✓ Candlestick or line charts work best
            ✓ Various timeframes (1m, 5m, 1h, 1d)
            ✓ Different assets (stocks, crypto, forex)
            
            What Happens:
            -------------
            The framework will:
            1. Load each image
            2. Run both ML and template detection
            3. Check if expected pattern was found
            4. Calculate accuracy percentage
            5. Generate detailed report
            
            Interpreting Results:
            --------------------
            - Accuracy >90%: Excellent, production-ready
            - Accuracy 70-90%: Good, may need tuning
            - Accuracy 50-70%: Needs improvement
            - Accuracy <50%: Major issues, requires fixing
            
        """.trimIndent())
        
        Timber.i("Created validation directory: ${validationDir.absolutePath}")
        Timber.i("Read instructions in: ${readmeFile.absolutePath}")
        
        return validationDir
    }
}
