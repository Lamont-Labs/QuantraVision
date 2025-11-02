package com.lamontlabs.quantravision.learning.advanced

import android.content.Context
import com.lamontlabs.quantravision.PatternDatabase
import com.lamontlabs.quantravision.learning.advanced.data.PatternCorrelationEntity
import com.lamontlabs.quantravision.learning.advanced.data.PatternSequenceEntity
import com.lamontlabs.quantravision.learning.advanced.model.PatternSequence
import com.lamontlabs.quantravision.learning.advanced.model.PredictionWithConfidence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.sqrt

class PatternCorrelationAnalyzer(private val context: Context) {
    
    private val db = PatternDatabase.getInstance(context)
    private val dao = db.advancedLearningDao()
    private val patternDao = db.patternDao()
    
    private val minPatternsForCorrelation = 20
    
    suspend fun analyzeCorrelations(): Map<Pair<String, String>, Float> = withContext(Dispatchers.IO) {
        try {
            val recentPatterns = patternDao.getRecent(System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L)
            
            if (recentPatterns.size < minPatternsForCorrelation) {
                Timber.w("Insufficient data for correlation analysis: ${recentPatterns.size} < $minPatternsForCorrelation")
                return@withContext emptyMap()
            }
            
            val patternNames = recentPatterns.map { it.patternName }.distinct()
            val correlations = mutableMapOf<Pair<String, String>, Float>()
            
            for (pattern1 in patternNames) {
                for (pattern2 in patternNames) {
                    if (pattern1 >= pattern2) continue
                    
                    val correlation = calculatePearsonCorrelation(
                        recentPatterns,
                        pattern1,
                        pattern2
                    )
                    
                    if (correlation != null) {
                        correlations[Pair(pattern1, pattern2)] = correlation
                        
                        dao.insertCorrelation(
                            PatternCorrelationEntity(
                                pattern1 = pattern1,
                                pattern2 = pattern2,
                                correlation = correlation,
                                cooccurrenceCount = countCooccurrences(recentPatterns, pattern1, pattern2),
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }
            
            correlations
        } catch (e: Exception) {
            Timber.e(e, "Failed to analyze correlations")
            emptyMap()
        }
    }
    
    suspend fun getPredictedNextPatterns(currentPattern: String): List<PredictionWithConfidence> = withContext(Dispatchers.IO) {
        try {
            val recentPatterns = patternDao.getRecent(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)
            val sequences = extractSequences(recentPatterns)
            
            val predictions = mutableMapOf<String, MutableList<Boolean>>()
            
            sequences.forEach { sequence ->
                val index = sequence.indexOf(currentPattern)
                if (index >= 0 && index < sequence.size - 1) {
                    val nextPattern = sequence[index + 1]
                    predictions.getOrPut(nextPattern) { mutableListOf() }.add(true)
                }
            }
            
            predictions.map { (pattern, occurrences) ->
                PredictionWithConfidence(
                    patternType = pattern,
                    probability = occurrences.size.toFloat() / sequences.size,
                    sampleSize = occurrences.size
                )
            }.filter { it.sampleSize >= 3 }
                .sortedByDescending { it.probability }
        } catch (e: Exception) {
            Timber.e(e, "Failed to predict next patterns for $currentPattern")
            emptyList()
        }
    }
    
    suspend fun getCommonSequences(): List<PatternSequence> = withContext(Dispatchers.IO) {
        try {
            val entities = dao.getCommonSequences(10)
            entities.map { entity ->
                PatternSequence(
                    patterns = entity.sequencePatterns.split(","),
                    frequency = entity.frequency,
                    avgSuccessRate = entity.avgSuccessRate,
                    timeSpan = entity.avgTimeSpan
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get common sequences")
            emptyList()
        }
    }
    
    suspend fun updateSequences() = withContext(Dispatchers.IO) {
        try {
            val recentPatterns = patternDao.getRecent(System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L)
            val sequences = extractSequences(recentPatterns)
            
            val sequenceStats = sequences.groupBy { it }
                .mapValues { (_, occurrences) -> occurrences.size }
            
            sequenceStats.forEach { (sequence, frequency) ->
                if (frequency >= 2) {
                    val timeSpans = mutableListOf<Long>()
                    
                    for (i in 0 until recentPatterns.size - sequence.size + 1) {
                        val window = recentPatterns.subList(i, i + sequence.size)
                        if (window.map { it.patternName } == sequence) {
                            timeSpans.add(window.last().timestamp - window.first().timestamp)
                        }
                    }
                    
                    dao.insertSequence(
                        PatternSequenceEntity(
                            sequencePatterns = sequence.joinToString(","),
                            frequency = frequency,
                            avgSuccessRate = 0.0f,
                            avgTimeSpan = timeSpans.average().toLong(),
                            lastSeen = System.currentTimeMillis()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update sequences")
        }
    }
    
    private fun calculatePearsonCorrelation(
        patterns: List<com.lamontlabs.quantravision.PatternMatch>,
        pattern1: String,
        pattern2: String
    ): Float? {
        val hourlyBuckets = 24
        val buckets1 = IntArray(hourlyBuckets)
        val buckets2 = IntArray(hourlyBuckets)
        
        patterns.forEach { pattern ->
            val hourBucket = ((pattern.timestamp / (60 * 60 * 1000)) % hourlyBuckets).toInt()
            if (pattern.patternName == pattern1) buckets1[hourBucket]++
            if (pattern.patternName == pattern2) buckets2[hourBucket]++
        }
        
        val mean1 = buckets1.average()
        val mean2 = buckets2.average()
        
        var numerator = 0.0
        var sum1Sq = 0.0
        var sum2Sq = 0.0
        
        for (i in buckets1.indices) {
            val diff1 = buckets1[i] - mean1
            val diff2 = buckets2[i] - mean2
            numerator += diff1 * diff2
            sum1Sq += diff1 * diff1
            sum2Sq += diff2 * diff2
        }
        
        val denominator = sqrt(sum1Sq * sum2Sq)
        return if (denominator > 0) (numerator / denominator).toFloat() else null
    }
    
    private fun countCooccurrences(
        patterns: List<com.lamontlabs.quantravision.PatternMatch>,
        pattern1: String,
        pattern2: String
    ): Int {
        val windowMs = 60 * 60 * 1000L
        var count = 0
        
        patterns.filter { it.patternName == pattern1 }.forEach { p1 ->
            if (patterns.any {
                    it.patternName == pattern2 &&
                            kotlin.math.abs(it.timestamp - p1.timestamp) < windowMs &&
                            it.id != p1.id
                }) {
                count++
            }
        }
        
        return count
    }
    
    private fun extractSequences(
        patterns: List<com.lamontlabs.quantravision.PatternMatch>
    ): List<List<String>> {
        val windowSize = 3
        val sequences = mutableListOf<List<String>>()
        
        for (i in 0..patterns.size - windowSize) {
            sequences.add(patterns.subList(i, i + windowSize).map { it.patternName })
        }
        
        return sequences
    }
}
