package com.lamontlabs.quantravision.detection

import android.content.Context
import android.graphics.Bitmap
import com.lamontlabs.quantravision.PatternDetector
import com.lamontlabs.quantravision.PatternMatch
import com.lamontlabs.quantravision.detection.model.Timeframe
import com.lamontlabs.quantravision.licensing.ProFeatureGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber

class TimeframeAggregator(private val context: Context) {
    
    private val detector = PatternDetector(context)
    
    private val resampledFrames = mutableMapOf<Timeframe, Bitmap>()
    
    suspend fun detectAllTimeframes(
        bitmap: Bitmap,
        originPath: String = "multi_tf_scan"
    ): Map<Timeframe, List<PatternMatch>> = withContext(Dispatchers.Default) {
        
        if (!ProFeatureGate.isActive(context)) {
            Timber.w("Multi-timeframe detection requires Pro tier")
            return@withContext mapOf(Timeframe.M5 to detector.detectFromBitmap(bitmap, originPath))
        }
        
        val results = mutableMapOf<Timeframe, List<PatternMatch>>()
        
        val timeframes = listOf(
            Timeframe.M1,
            Timeframe.M5,
            Timeframe.M15,
            Timeframe.H1,
            Timeframe.H4,
            Timeframe.DAILY
        )
        
        val detectionJobs = timeframes.map { timeframe ->
            async {
                val resampled = resampleForTimeframe(bitmap, timeframe)
                val matches = detector.detectFromBitmap(resampled, "$originPath:${timeframe.name}")
                
                resampledFrames[timeframe] = resampled
                
                timeframe to matches
            }
        }
        
        detectionJobs.forEach { job ->
            val (timeframe, matches) = job.await()
            results[timeframe] = matches
            Timber.d("Timeframe ${timeframe.displayName}: ${matches.size} patterns detected")
        }
        
        Timber.i("Multi-timeframe detection complete: ${results.values.sumOf { it.size }} total patterns across ${timeframes.size} timeframes")
        
        results
    }
    
    suspend fun getTimeframeConfluence(
        multiTimeframeResults: Map<Timeframe, List<PatternMatch>>,
        minTimeframes: Int = 2
    ): List<PatternMatch> = withContext(Dispatchers.Default) {
        
        val patternsByName = mutableMapOf<String, MutableList<Pair<Timeframe, PatternMatch>>>()
        
        multiTimeframeResults.forEach { (timeframe, matches) ->
            matches.forEach { match ->
                patternsByName
                    .getOrPut(match.patternName) { mutableListOf() }
                    .add(timeframe to match)
            }
        }
        
        val confluencePatterns = patternsByName
            .filter { (_, occurrences) -> occurrences.size >= minTimeframes }
            .map { (patternName, occurrences) ->
                val bestMatch = occurrences.maxByOrNull { it.second.confidence }?.second
                    ?: occurrences.first().second
                
                bestMatch
            }
        
        Timber.i("Timeframe confluence: ${confluencePatterns.size} patterns detected across $minTimeframes+ timeframes")
        
        confluencePatterns
    }
    
    private fun resampleForTimeframe(bitmap: Bitmap, timeframe: Timeframe): Bitmap {
        val scaleFactor = when (timeframe) {
            Timeframe.M1 -> 0.5f
            Timeframe.M5 -> 0.7f
            Timeframe.M15 -> 1.0f
            Timeframe.H1 -> 1.3f
            Timeframe.H4 -> 1.6f
            Timeframe.DAILY -> 2.0f
        }
        
        val newWidth = (bitmap.width * scaleFactor).toInt().coerceAtLeast(1)
        val newHeight = (bitmap.height * scaleFactor).toInt().coerceAtLeast(1)
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    fun clearCache() {
        resampledFrames.values.forEach { it.recycle() }
        resampledFrames.clear()
    }
}
