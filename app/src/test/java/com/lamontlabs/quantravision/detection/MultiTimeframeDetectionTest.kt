package com.lamontlabs.quantravision.detection

import com.lamontlabs.quantravision.detection.model.Timeframe
import org.junit.Test
import org.junit.Assert.*

class MultiTimeframeDetectionTest {
    
    @Test
    fun testTimeframeEnumValues() {
        val timeframes = Timeframe.values()
        
        assertEquals(6, timeframes.size)
        assertTrue(timeframes.contains(Timeframe.M1))
        assertTrue(timeframes.contains(Timeframe.M5))
        assertTrue(timeframes.contains(Timeframe.M15))
        assertTrue(timeframes.contains(Timeframe.H1))
        assertTrue(timeframes.contains(Timeframe.H4))
        assertTrue(timeframes.contains(Timeframe.DAILY))
    }
    
    @Test
    fun testTimeframeDurations() {
        assertEquals(1, Timeframe.M1.durationMinutes)
        assertEquals(5, Timeframe.M5.durationMinutes)
        assertEquals(15, Timeframe.M15.durationMinutes)
        assertEquals(60, Timeframe.H1.durationMinutes)
        assertEquals(240, Timeframe.H4.durationMinutes)
        assertEquals(1440, Timeframe.DAILY.durationMinutes)
    }
    
    @Test
    fun testTimeframeDurationInMs() {
        assertEquals(60000L, Timeframe.M1.durationMs)
        assertEquals(300000L, Timeframe.M5.durationMs)
        assertEquals(900000L, Timeframe.M15.durationMs)
        assertEquals(3600000L, Timeframe.H1.durationMs)
        assertEquals(14400000L, Timeframe.H4.durationMs)
        assertEquals(86400000L, Timeframe.DAILY.durationMs)
    }
    
    @Test
    fun testTimeframeDisplayNames() {
        assertEquals("1 Minute", Timeframe.M1.displayName)
        assertEquals("5 Minutes", Timeframe.M5.displayName)
        assertEquals("15 Minutes", Timeframe.M15.displayName)
        assertEquals("1 Hour", Timeframe.H1.displayName)
        assertEquals("4 Hours", Timeframe.H4.displayName)
        assertEquals("Daily", Timeframe.DAILY.displayName)
    }
    
    @Test
    fun testTimeframeFromString() {
        assertEquals(Timeframe.M1, Timeframe.fromString("M1"))
        assertEquals(Timeframe.M5, Timeframe.fromString("m5"))
        assertEquals(Timeframe.H1, Timeframe.fromString("H1"))
        assertNull(Timeframe.fromString("invalid"))
        assertNull(Timeframe.fromString(""))
    }
    
    @Test
    fun testTimeframeFromDisplayName() {
        assertEquals(Timeframe.M1, Timeframe.fromDisplayName("1 Minute"))
        assertEquals(Timeframe.H4, Timeframe.fromDisplayName("4 Hours"))
        assertEquals(Timeframe.DAILY, Timeframe.fromDisplayName("Daily"))
        assertNull(Timeframe.fromDisplayName("Invalid"))
    }
    
    @Test
    fun testTimeframeOrdering() {
        val timeframes = Timeframe.values()
        
        assertTrue(timeframes[0].durationMinutes < timeframes[1].durationMinutes)
        assertTrue(timeframes[1].durationMinutes < timeframes[2].durationMinutes)
        assertTrue(timeframes[2].durationMinutes < timeframes[3].durationMinutes)
        assertTrue(timeframes[3].durationMinutes < timeframes[4].durationMinutes)
        assertTrue(timeframes[4].durationMinutes < timeframes[5].durationMinutes)
    }
}
