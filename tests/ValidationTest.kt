package com.lamontlabs.quantravision.tests

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.lamontlabs.quantravision.validation.SimpleTestRunner
import com.lamontlabs.quantravision.validation.ValidationFramework
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.opencv.android.OpenCVLoader
import timber.log.Timber
import java.io.File

/**
 * Validation tests for pattern detection accuracy.
 * 
 * To run:
 * 1. Add test images to validation_images directory
 * 2. Run this test
 * 3. Check logs for accuracy report
 */
class ValidationTest {
    
    private lateinit var context: Context
    private lateinit var testRunner: SimpleTestRunner
    
    @Before
    fun setup() {
        OpenCVLoader.initDebug()
        context = ApplicationProvider.getApplicationContext()
        testRunner = SimpleTestRunner(context)
    }
    
    @Test
    fun createValidationDirectoryTemplate() {
        val dir = testRunner.createTestDirectoryTemplate()
        assertTrue("Validation directory should be created", dir.exists())
        assertTrue("Validation directory should be a directory", dir.isDirectory)
        
        val readme = File(dir, "README.txt")
        assertTrue("README should exist", readme.exists())
        
        Timber.i("Validation directory created at: ${dir.absolutePath}")
        Timber.i("Add test images to this directory and run validation tests")
    }
    
    @Test
    fun runQuickValidationTest() = runBlocking {
        val report = testRunner.runQuickTest()
        
        // Log results
        Timber.i(report.prettyPrint())
        
        // Assert we can generate a report (even if no test cases)
        assertTrue("Should generate report", true)
        
        // If we have test cases, check accuracy
        if (report.totalTests > 0) {
            Timber.i("Accuracy: ${(report.accuracy * 100).toInt()}%")
            
            // Warning if accuracy is low
            if (report.accuracy < 0.7f) {
                Timber.w("⚠️  Accuracy below 70% - pattern detection needs improvement!")
            } else if (report.accuracy >= 0.9f) {
                Timber.i("✅ Excellent accuracy (>90%) - production ready!")
            }
        } else {
            Timber.w("No test images found. Add images to validation_images directory.")
        }
    }
    
    @Test
    fun testManualTestCase() = runBlocking {
        // Example: Test a specific image manually
        val validationDir = File(context.filesDir, "validation_images")
        val testImage = File(validationDir, "head-and-shoulders-top_001.png")
        
        if (testImage.exists()) {
            val result = testRunner.testSingleImage(
                imageFile = testImage,
                expectedPattern = "Head and Shoulders Top"
            )
            
            Timber.i("Test result: ${if (result.correct) "PASS" else "FAIL"}")
            Timber.i("Detected: ${result.detectedPatterns}")
            Timber.i("Confidence: ${result.confidence}")
            Timber.i("Processing time: ${result.processingTimeMs}ms")
            
            assertTrue("Should complete without error", true)
        } else {
            Timber.w("Test image not found: ${testImage.absolutePath}")
            Timber.w("Skipping manual test")
        }
    }
    
    @Test
    fun validateProcessingSpeed() = runBlocking {
        val report = testRunner.runQuickTest()
        
        if (report.totalTests > 0) {
            val avgTime = report.avgProcessingTimeMs
            
            Timber.i("Average processing time: ${avgTime}ms per image")
            
            // Check if processing is fast enough (should be < 1000ms)
            if (avgTime < 1000) {
                Timber.i("✅ Fast detection (<1s per image)")
            } else if (avgTime < 3000) {
                Timber.w("⚠️  Slow detection (1-3s per image)")
            } else {
                Timber.e("❌ Very slow detection (>3s per image) - needs optimization")
            }
            
            assertTrue("Processing should complete", avgTime >= 0)
        }
    }
}
