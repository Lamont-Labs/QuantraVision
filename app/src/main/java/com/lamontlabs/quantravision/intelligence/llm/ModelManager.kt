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
 * Manages Gemma model lifecycle: verification, caching, and file management
 * 
 * NOTE: Model download is manual via Kaggle (requires authentication).
 * See app/src/main/assets/models/DOWNLOAD_INSTRUCTIONS.md for setup guide.
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
            !isModelValid() -> ModelState.Error("Model file corrupted or wrong format", recoverable = true)
            else -> ModelState.Downloaded
        }
    }
    
    /**
     * Check if model file is valid (size check + .task format validation)
     */
    private fun isModelValid(): Boolean {
        if (!modelFile.exists()) return false
        
        // Validate file extension is .task (MediaPipe format)
        if (!modelFile.name.endsWith(".task")) {
            Timber.w("🧠 Invalid model format: ${modelFile.name}. Must be .task file for MediaPipe")
            return false
        }
        
        val size = modelFile.length()
        
        // Model should be close to expected size (allow 10% variance)
        val minSize = (ModelConfig.MODEL_SIZE_BYTES * 0.9).toLong()
        val maxSize = (ModelConfig.MODEL_SIZE_BYTES * 1.1).toLong()
        
        if (size !in minSize..maxSize) {
            Timber.w("🧠 Model size ${size / 1_000_000}MB outside expected range ${minSize / 1_000_000}-${maxSize / 1_000_000}MB")
            return false
        }
        
        return true
    }
    
    /**
     * Download model (reserved for future automated download)
     * 
     * NOTE: Currently not functional - Kaggle requires authentication.
     * Users must manually download via instructions in DOWNLOAD_INSTRUCTIONS.md
     * 
     * This method is kept for potential future integration with:
     * - Play Asset Delivery (for bundling with app)
     * - Future authenticated Kaggle API support
     * - Alternative model hosting solutions
     */
    suspend fun downloadModel(
        onProgress: (ModelState.Downloading) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        // Kaggle downloads require authentication - not supported for automatic download
        Timber.w("🧠 Automatic download not supported. Please follow manual download instructions.")
        return@withContext Result.failure(
            Exception(
                "Automatic download not supported for Kaggle models. " +
                "Please see app/src/main/assets/models/DOWNLOAD_INSTRUCTIONS.md for setup guide."
            )
        )
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
