package com.lamontlabs.quantravision.intelligence.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages Gemma model lifecycle: download, verification, caching
 */
class ModelManager(private val context: Context) {
    
    private val modelDir = File(context.filesDir, "llm_models")
    private val modelFile = File(modelDir, ModelConfig.MODEL_NAME)
    
    init {
        modelDir.mkdirs()
    }
    
    /**
     * Get current model state
     */
    fun getModelState(): ModelState {
        return when {
            !modelFile.exists() -> ModelState.NotDownloaded
            !isModelValid() -> ModelState.Error("Model file corrupted", recoverable = true)
            else -> ModelState.Downloaded
        }
    }
    
    /**
     * Check if model file is valid (basic size check)
     */
    private fun isModelValid(): Boolean {
        if (!modelFile.exists()) return false
        val size = modelFile.length()
        
        // Model should be close to expected size (allow 10% variance)
        val minSize = (ModelConfig.MODEL_SIZE_BYTES * 0.9).toLong()
        val maxSize = (ModelConfig.MODEL_SIZE_BYTES * 1.1).toLong()
        
        return size in minSize..maxSize
    }
    
    /**
     * Download model from HuggingFace
     * Returns ModelState as download progresses
     */
    suspend fun downloadModel(
        onProgress: (ModelState.Downloading) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Check WiFi availability if configured
            if (!isWiFiConnected() && ModelConfig.PRELOAD_ON_WIFI) {
                return@withContext Result.failure(
                    Exception("WiFi required for model download. Connect to WiFi and try again.")
                )
            }
            
            Timber.i("🧠 Starting Gemma model download: ${ModelConfig.MODEL_URL}")
            
            val url = URL(ModelConfig.MODEL_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            
            val totalSize = connection.contentLength.toLong()
            
            // Create temporary file
            val tempFile = File(modelDir, "${ModelConfig.MODEL_NAME}.tmp")
            
            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L
                    var lastProgressUpdate = 0L
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // Update progress every 5MB to avoid UI flooding
                        if (totalBytesRead - lastProgressUpdate > 5_000_000) {
                            val progress = totalBytesRead.toFloat() / totalSize
                            onProgress(ModelState.Downloading(progress, totalBytesRead))
                            lastProgressUpdate = totalBytesRead
                        }
                    }
                    
                    // Final progress update
                    onProgress(ModelState.Downloading(1.0f, totalBytesRead))
                }
            }
            
            // Move temp file to final location
            if (tempFile.renameTo(modelFile)) {
                Timber.i("🧠 Model downloaded successfully: ${modelFile.length()} bytes")
                Result.success(modelFile)
            } else {
                tempFile.delete()
                Result.failure(Exception("Failed to save model file"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "🧠 Model download failed")
            Result.failure(e)
        }
    }
    
    /**
     * Get model file if available
     */
    fun getModelFile(): File? {
        return if (modelFile.exists() && isModelValid()) modelFile else null
    }
    
    /**
     * Delete model file (for clearing cache or re-downloading)
     */
    fun deleteModel(): Boolean {
        return try {
            if (modelFile.exists()) {
                modelFile.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete model file")
            false
        }
    }
    
    /**
     * Check if device is connected to WiFi
     */
    private fun isWiFiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Get model size for UI display
     */
    fun getModelSizeMB(): Long {
        return if (modelFile.exists()) {
            modelFile.length() / (1024 * 1024)
        } else {
            ModelConfig.MODEL_SIZE_BYTES / (1024 * 1024)
        }
    }
}
