package com.lamontlabs.quantravision.replay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.lamontlabs.quantravision.PatternMatch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * SessionPlayback (production-ready)
 * - Loads and plays back recorded detection sessions
 * - Integrates with app storage for session management
 * - Implements frame caching and buffering
 * - Extracts metadata from session files
 * - Provides playback controls and state management
 */
data class ReplayFrame(
    val timestamp: Long,
    val imagePath: String,
    val detections: List<PatternMatch> = emptyList()
)

data class SessionMetadata(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val frameCount: Int,
    val detectionCount: Int,
    val duration: Long
)

class SessionPlayback(private val context: Context) {

    private val tag = "SessionPlayback"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val frames = mutableListOf<ReplayFrame>()
    private val frameCache = mutableMapOf<String, Bitmap>()
    private val maxCacheSize = 20
    
    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex
    
    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    
    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap
    
    private var playbackJob: Job? = null
    private var metadata: SessionMetadata? = null

    enum class PlaybackState {
        PLAYING, PAUSED, STOPPED
    }

    fun loadFromFolder(sessionDir: File): SessionMetadata {
        Log.d(tag, "Loading session from: ${sessionDir.absolutePath}")
        
        frames.clear()
        frameCache.clear()
        
        if (!sessionDir.exists() || !sessionDir.isDirectory) {
            throw IllegalArgumentException("Invalid session directory: ${sessionDir.absolutePath}")
        }

        val detectionLog = File(sessionDir, "detections.log")
        val detectionsMap = if (detectionLog.exists()) {
            parseDetectionLog(detectionLog)
        } else {
            emptyMap()
        }

        val imageFiles = sessionDir.listFiles { file ->
            file.name.startsWith("shot_") && file.name.endsWith(".png")
        }?.sortedBy { it.name } ?: emptyList()

        if (imageFiles.isEmpty()) {
            throw IllegalStateException("No frames found in session directory")
        }

        var totalDetections = 0
        imageFiles.forEach { imageFile ->
            val timestamp = extractTimestampFromFilename(imageFile.name)
            val detections = detectionsMap[timestamp] ?: emptyList()
            totalDetections += detections.size
            
            frames.add(
                ReplayFrame(
                    timestamp = timestamp,
                    imagePath = imageFile.absolutePath,
                    detections = detections
                )
            )
        }

        val startTime = frames.firstOrNull()?.timestamp ?: System.currentTimeMillis()
        val endTime = frames.lastOrNull()?.timestamp ?: startTime

        metadata = SessionMetadata(
            sessionId = sessionDir.name,
            startTime = startTime,
            endTime = endTime,
            frameCount = frames.size,
            detectionCount = totalDetections,
            duration = endTime - startTime
        )

        Log.d(tag, "Loaded ${frames.size} frames with $totalDetections detections")
        
        if (frames.isNotEmpty()) {
            loadFrameAtIndex(0)
        }

        return metadata!!
    }

    fun play() {
        if (_playbackState.value == PlaybackState.PLAYING) return
        
        _playbackState.value = PlaybackState.PLAYING
        
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && _playbackState.value == PlaybackState.PLAYING) {
                val currentIndex = _currentFrameIndex.value
                
                if (currentIndex >= frames.size - 1) {
                    stop()
                    break
                }
                
                seekTo(currentIndex + 1)
                delay(66)
            }
        }
    }

    fun pause() {
        _playbackState.value = PlaybackState.PAUSED
        playbackJob?.cancel()
    }

    fun stop() {
        _playbackState.value = PlaybackState.STOPPED
        playbackJob?.cancel()
        seekTo(0)
    }

    fun seekTo(frameIndex: Int) {
        val validIndex = frameIndex.coerceIn(0, frames.size - 1)
        _currentFrameIndex.value = validIndex
        loadFrameAtIndex(validIndex)
    }

    fun getCurrentFrame(): ReplayFrame? {
        val index = _currentFrameIndex.value
        return frames.getOrNull(index)
    }

    fun getMetadata(): SessionMetadata? = metadata

    fun getTotalFrames(): Int = frames.size

    fun clear() {
        stop()
        frames.clear()
        clearCache()
        metadata = null
        _currentBitmap.value = null
        _currentFrameIndex.value = 0
    }

    fun release() {
        clear()
        scope.cancel()
    }

    private fun loadFrameAtIndex(index: Int) {
        if (index < 0 || index >= frames.size) return
        
        scope.launch {
            val frame = frames[index]
            val bitmap = loadBitmap(frame.imagePath)
            _currentBitmap.value = bitmap
            
            preloadNearbyFrames(index)
        }
    }

    private suspend fun loadBitmap(path: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            frameCache[path]?.let { return@withContext it }
            
            val bitmap = BitmapFactory.decodeFile(path)
            
            if (bitmap != null && frameCache.size < maxCacheSize) {
                frameCache[path] = bitmap
            }
            
            bitmap
        } catch (e: Exception) {
            Log.e(tag, "Error loading bitmap: $path", e)
            null
        }
    }

    private fun preloadNearbyFrames(currentIndex: Int) {
        scope.launch(Dispatchers.IO) {
            val startIndex = (currentIndex - 2).coerceAtLeast(0)
            val endIndex = (currentIndex + 5).coerceAtMost(frames.size - 1)
            
            for (i in startIndex..endIndex) {
                if (!frameCache.containsKey(frames[i].imagePath)) {
                    loadBitmap(frames[i].imagePath)
                }
            }
            
            evictOldFrames(currentIndex)
        }
    }

    private fun evictOldFrames(currentIndex: Int) {
        if (frameCache.size > maxCacheSize) {
            val keysToRemove = frameCache.keys.filter { path ->
                val frameIndex = frames.indexOfFirst { it.imagePath == path }
                frameIndex != -1 && Math.abs(frameIndex - currentIndex) > 10
            }.take(frameCache.size - maxCacheSize)
            
            keysToRemove.forEach { key ->
                frameCache.remove(key)?.recycle()
            }
        }
    }

    private fun clearCache() {
        frameCache.values.forEach { it.recycle() }
        frameCache.clear()
    }

    private fun parseDetectionLog(logFile: File): Map<Long, List<PatternMatch>> {
        val detectionsMap = mutableMapOf<Long, MutableList<PatternMatch>>()
        
        try {
            logFile.readLines().forEach { line ->
                if (line.isBlank()) return@forEach
                
                val parts = line.split(" | ")
                if (parts.size < 4) return@forEach
                
                try {
                    val fmt = SimpleDateFormat("yyyy-MM-dd'T'HHmmss'Z'", Locale.US)
                    val timestamp = fmt.parse(parts[0])?.time ?: return@forEach
                    val patternName = parts[1]
                    val confidence = parts[2].substringAfter("conf=").toFloatOrNull() ?: 0f
                    val timeframe = parts[3].substringAfter("tf=")
                    
                    val match = PatternMatch(
                        patternName = patternName,
                        confidence = confidence,
                        boundingBox = android.graphics.Rect(0, 0, 100, 100),
                        timestamp = timestamp,
                        timeframe = timeframe
                    )
                    
                    detectionsMap.getOrPut(timestamp) { mutableListOf() }.add(match)
                } catch (e: Exception) {
                    Log.w(tag, "Error parsing detection log line: $line", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error reading detection log", e)
        }
        
        return detectionsMap
    }

    private fun extractTimestampFromFilename(filename: String): Long {
        return try {
            val timestampStr = filename.removePrefix("shot_").removeSuffix(".png")
            timestampStr.toLong()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    companion object {
        fun getAvailableSessions(context: Context): List<File> {
            val sessionsDir = File(context.filesDir, "sessions")
            if (!sessionsDir.exists()) return emptyList()
            
            return sessionsDir.listFiles { file ->
                file.isDirectory && file.listFiles { f -> 
                    f.name.startsWith("shot_") && f.name.endsWith(".png")
                }?.isNotEmpty() == true
            }?.sortedByDescending { it.name } ?: emptyList()
        }
    }
}
