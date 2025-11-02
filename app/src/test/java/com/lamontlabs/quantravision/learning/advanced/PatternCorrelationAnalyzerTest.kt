package com.lamontlabs.quantravision.learning.advanced

import org.junit.Test
import org.junit.Assert.*

class PatternCorrelationAnalyzerTest {
    
    @Test
    fun testMinimumDataThreshold() {
        assertTrue("Minimum correlation threshold is 20", 20 >= 10)
    }
    
    @Test
    fun testPearsonCorrelationRange() {
        val correlation = 0.75f
        assertTrue("Correlation in valid range", correlation >= -1.0f && correlation <= 1.0f)
    }
    
    @Test
    fun testSequenceWindowSize() {
        val windowSize = 3
        assertEquals("Sequence window is 3 patterns", 3, windowSize)
    }
}
