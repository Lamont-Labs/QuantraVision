package com.lamontlabs.quantravision.fixtures

import android.graphics.Bitmap
import android.graphics.Color
import com.lamontlabs.quantravision.apex.models.Candle
import com.lamontlabs.quantravision.apex.models.ChartPrimitives
import com.lamontlabs.quantravision.apex.models.TrendLine

object TestFixtures {
    
    private const val TAG = "TestFixtures"
    
    fun createTestChartBitmap(width: Int = 1080, height: Int = 1920): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.parseColor("#1E1E1E"))
        }
    }
    
    fun createValidChartBitmap(width: Int = 1080, height: Int = 1920): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        bitmap.eraseColor(Color.parseColor("#1E1E1E"))
        
        val centerX = width / 2
        val centerY = height / 2
        val radius = 50
        
        for (x in (centerX - radius)..(centerX + radius)) {
            for (y in (centerY - radius)..(centerY + radius)) {
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    val dx = x - centerX
                    val dy = y - centerY
                    if (dx * dx + dy * dy <= radius * radius) {
                        bitmap.setPixel(x, y, Color.WHITE)
                    }
                }
            }
        }
        
        return bitmap
    }
    
    fun createMockChartPrimitives(): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "test-fixture-hash-12345",
            candles = listOf(
                Candle(timestamp = 1000L, open = 100.0, high = 110.0, low = 95.0, close = 105.0, volume = 1000.0),
                Candle(timestamp = 2000L, open = 105.0, high = 115.0, low = 100.0, close = 110.0, volume = 1200.0),
                Candle(timestamp = 3000L, open = 110.0, high = 120.0, low = 105.0, close = 115.0, volume = 1500.0),
                Candle(timestamp = 4000L, open = 115.0, high = 125.0, low = 110.0, close = 120.0, volume = 1800.0),
                Candle(timestamp = 5000L, open = 120.0, high = 130.0, low = 115.0, close = 125.0, volume = 2000.0)
            ),
            detectedLines = listOf(
                TrendLine(x1 = 0.0, y1 = 500.0, x2 = 1000.0, y2 = 400.0, confidence = 0.85),
                TrendLine(x1 = 0.0, y1 = 700.0, x2 = 1000.0, y2 = 600.0, confidence = 0.75)
            ),
            ocrText = "BTC/USD 1H RSI: 65.5 MACD: Bullish",
            chartType = "CANDLESTICK"
        )
    }
    
    fun createMockBullishChartPrimitives(): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "bullish-test-hash",
            candles = listOf(
                Candle(timestamp = 1000L, open = 100.0, high = 102.0, low = 99.0, close = 101.0, volume = 1000.0),
                Candle(timestamp = 2000L, open = 101.0, high = 104.0, low = 100.0, close = 103.0, volume = 1100.0),
                Candle(timestamp = 3000L, open = 103.0, high = 106.0, low = 102.0, close = 105.0, volume = 1200.0),
                Candle(timestamp = 4000L, open = 105.0, high = 108.0, low = 104.0, close = 107.0, volume = 1300.0),
                Candle(timestamp = 5000L, open = 107.0, high = 111.0, low = 106.0, close = 110.0, volume = 1400.0)
            ),
            detectedLines = listOf(
                TrendLine(x1 = 0.0, y1 = 600.0, x2 = 1000.0, y2 = 300.0, confidence = 0.9)
            ),
            ocrText = "SPY 5m RSI: 72.0 MACD: Strong Bullish",
            chartType = "CANDLESTICK"
        )
    }
    
    fun createMockBearishChartPrimitives(): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "bearish-test-hash",
            candles = listOf(
                Candle(timestamp = 1000L, open = 110.0, high = 111.0, low = 108.0, close = 109.0, volume = 1000.0),
                Candle(timestamp = 2000L, open = 109.0, high = 110.0, low = 106.0, close = 107.0, volume = 1100.0),
                Candle(timestamp = 3000L, open = 107.0, high = 108.0, low = 104.0, close = 105.0, volume = 1200.0),
                Candle(timestamp = 4000L, open = 105.0, high = 106.0, low = 102.0, close = 103.0, volume = 1300.0),
                Candle(timestamp = 5000L, open = 103.0, high = 104.0, low = 99.0, close = 100.0, volume = 1400.0)
            ),
            detectedLines = listOf(
                TrendLine(x1 = 0.0, y1 = 300.0, x2 = 1000.0, y2 = 600.0, confidence = 0.88)
            ),
            ocrText = "TSLA 15m RSI: 28.0 MACD: Strong Bearish",
            chartType = "CANDLESTICK"
        )
    }
    
    fun createEmptyChartPrimitives(): ChartPrimitives {
        return ChartPrimitives(
            rawImageHash = "empty-test-hash",
            candles = emptyList(),
            detectedLines = emptyList(),
            ocrText = "",
            chartType = "Unknown"
        )
    }
}
