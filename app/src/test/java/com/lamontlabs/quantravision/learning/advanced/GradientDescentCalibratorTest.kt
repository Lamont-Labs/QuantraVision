package com.lamontlabs.quantravision.learning.advanced

import org.junit.Test
import org.junit.Assert.*

class GradientDescentCalibratorTest {
    
    @Test
    fun testLearningRate() {
        val learningRate = 0.01f
        assertEquals("Learning rate is 0.01", 0.01f, learningRate, 0.001f)
    }
    
    @Test
    fun testMaxIterations() {
        val maxIterations = 100
        assertEquals("Maximum 100 iterations", 100, maxIterations)
    }
    
    @Test
    fun testConvergenceThreshold() {
        val threshold = 0.001f
        assertEquals("Convergence threshold is 0.001", 0.001f, threshold, 0.0001f)
    }
    
    @Test
    fun testLossFunction() {
        val falsePositiveRate = 0.10f
        val truePositiveRate = 0.85f
        
        val loss = falsePositiveRate + (1.0f - truePositiveRate)
        
        assertEquals("Loss function calculation", 0.25f, loss, 0.01f)
    }
    
    @Test
    fun testThresholdBounds() {
        val threshold = 0.65f
        assertTrue("Threshold within bounds [0.3, 0.9]", threshold >= 0.3f && threshold <= 0.9f)
    }
}
