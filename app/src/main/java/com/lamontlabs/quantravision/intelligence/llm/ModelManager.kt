package com.lamontlabs.quantravision.intelligence.llm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Manages ensemble model lifecycle: verification, caching, and file management
 * 
 * Tracks 3 separate TFLite models:
 * 1. Intent Classifier - for intent classification
 * 2. Sentence Embeddings - for semantic similarity
 * 3. MobileBERT Q&A - for question answering
 * 
 * NOTE: Model download is manual.
 * See app/src/main/assets/models/ENSEMBLE_MODEL_DOWNLOADS.md for setup guide.
 */
class ModelManager(private val context: Context) {
    
    private val modelDir = File(context.filesDir, "llm_models")
    
    // 3 separate model files
    private val intentClassifierFile = File(modelDir, ModelConfig.INTENT_CLASSIFIER_NAME)
    private val sentenceEmbeddingsFile = File(modelDir, ModelConfig.SENTENCE_EMBEDDINGS_NAME)
    private val mobileBertFile = File(modelDir, ModelConfig.MOBILEBERT_QA_NAME)
    
    // Legacy model file (for backward compatibility during migration)
    @Deprecated("Use individual model files instead")
    private val legacyModelFile = File(modelDir, ModelConfig.MODEL_NAME)
    
    private val _modelStateFlow = MutableStateFlow<ModelState>(ModelState.NotDownloaded)
    val modelStateFlow: StateFlow<ModelState> = _modelStateFlow.asStateFlow()
    
    init {
        modelDir.mkdirs()
        _modelStateFlow.value = getModelState()
    }
    
    /**
     * Get the Intent Classifier model file if available
     */
    fun getIntentClassifierFile(): File? {
        return if (intentClassifierFile.exists() && isModelValid(ModelType.INTENT_CLASSIFIER)) {
            intentClassifierFile
        } else null
    }
    
    /**
     * Get the Sentence Embeddings model file if available
     */
    fun getEmbeddingsModelFile(): File? {
        return if (sentenceEmbeddingsFile.exists() && isModelValid(ModelType.SENTENCE_EMBEDDINGS)) {
            sentenceEmbeddingsFile
        } else null
    }
    
    /**
     * Get the MobileBERT Q&A model file if available
     */
    fun getMobileBertFile(): File? {
        return if (mobileBertFile.exists() && isModelValid(ModelType.MOBILEBERT_QA)) {
            mobileBertFile
        } else null
    }
    
    /**
     * Check if all 3 models are available
     */
    fun isModelAvailable(): Boolean {
        return getIntentClassifierFile() != null &&
               getEmbeddingsModelFile() != null &&
               getMobileBertFile() != null
    }
    
    /**
     * Get current model state based on which models are imported
     */
    fun getModelState(): ModelState {
        val importedModels = mutableSetOf<ModelType>()
        
        // Check each model file
        if (intentClassifierFile.exists() && isModelValid(ModelType.INTENT_CLASSIFIER)) {
            importedModels.add(ModelType.INTENT_CLASSIFIER)
        }
        if (sentenceEmbeddingsFile.exists() && isModelValid(ModelType.SENTENCE_EMBEDDINGS)) {
            importedModels.add(ModelType.SENTENCE_EMBEDDINGS)
        }
        if (mobileBertFile.exists() && isModelValid(ModelType.MOBILEBERT_QA)) {
            importedModels.add(ModelType.MOBILEBERT_QA)
        }
        
        return when (importedModels.size) {
            0 -> ModelState.NotDownloaded
            3 -> ModelState.Downloaded  // All 3 models present
            else -> ModelState.PartiallyDownloaded(
                importedCount = importedModels.size,
                importedModels = importedModels
            )
        }
    }
    
    /**
     * Check if a specific model file is valid (size check + .tflite format validation)
     */
    private fun isModelValid(modelType: ModelType): Boolean {
        val (file, expectedSize, expectedName) = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> Triple(
                intentClassifierFile,
                ModelConfig.INTENT_CLASSIFIER_SIZE_BYTES,
                ModelConfig.INTENT_CLASSIFIER_NAME
            )
            ModelType.SENTENCE_EMBEDDINGS -> Triple(
                sentenceEmbeddingsFile,
                ModelConfig.SENTENCE_EMBEDDINGS_SIZE_BYTES,
                ModelConfig.SENTENCE_EMBEDDINGS_NAME
            )
            ModelType.MOBILEBERT_QA -> Triple(
                mobileBertFile,
                ModelConfig.MOBILEBERT_QA_SIZE_BYTES,
                ModelConfig.MOBILEBERT_QA_NAME
            )
        }
        
        if (!file.exists()) return false
        
        // Validate file extension is .tflite
        if (!file.name.endsWith(".tflite")) {
            Timber.w("🧠 Invalid model format: ${file.name}. Must be .tflite file")
            return false
        }
        
        // Validate filename matches expected
        if (file.name != expectedName) {
            Timber.w("🧠 Model filename mismatch: expected $expectedName, got ${file.name}")
            return false
        }
        
        val size = file.length()
        
        // Model should be close to expected size (allow 20% variance for TFLite models)
        val minSize = (expectedSize * 0.8).toLong()
        val maxSize = (expectedSize * 1.2).toLong()
        
        if (size !in minSize..maxSize) {
            Timber.w("🧠 Model size ${size / 1_000_000}MB outside expected range ${minSize / 1_000_000}-${maxSize / 1_000_000}MB for $modelType")
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
     * Get model file if available (legacy - returns Intent Classifier for backward compatibility)
     */
    @Deprecated("Use getIntentClassifierFile(), getEmbeddingsModelFile(), or getMobileBertFile() instead")
    fun getModelFile(): File? {
        return getIntentClassifierFile()
    }
    
    /**
     * Get model file for a specific model type
     */
    fun getModelFile(modelType: ModelType): File? {
        return when (modelType) {
            ModelType.INTENT_CLASSIFIER -> getIntentClassifierFile()
            ModelType.SENTENCE_EMBEDDINGS -> getEmbeddingsModelFile()
            ModelType.MOBILEBERT_QA -> getMobileBertFile()
        }
    }
    
    /**
     * Delete all model files (for clearing cache or re-downloading)
     */
    fun deleteModel(): Boolean {
        return try {
            var allDeleted = true
            if (intentClassifierFile.exists()) {
                allDeleted = allDeleted && intentClassifierFile.delete()
            }
            if (sentenceEmbeddingsFile.exists()) {
                allDeleted = allDeleted && sentenceEmbeddingsFile.delete()
            }
            if (mobileBertFile.exists()) {
                allDeleted = allDeleted && mobileBertFile.delete()
            }
            // Also delete legacy file if present
            if (legacyModelFile.exists()) {
                legacyModelFile.delete()
            }
            allDeleted
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete model files")
            false
        }
    }
    
    /**
     * Delete a specific model file
     */
    fun deleteModel(modelType: ModelType): Boolean {
        return try {
            val file = when (modelType) {
                ModelType.INTENT_CLASSIFIER -> intentClassifierFile
                ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile
                ModelType.MOBILEBERT_QA -> mobileBertFile
            }
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete model file for $modelType")
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
     * Get total model size for UI display (sum of all 3 models)
     */
    fun getModelSizeMB(): Long {
        var totalSize = 0L
        if (intentClassifierFile.exists()) totalSize += intentClassifierFile.length()
        if (sentenceEmbeddingsFile.exists()) totalSize += sentenceEmbeddingsFile.length()
        if (mobileBertFile.exists()) totalSize += mobileBertFile.length()
        
        return if (totalSize > 0) {
            totalSize / (1024 * 1024)
        } else {
            // Return expected total size
            (ModelConfig.INTENT_CLASSIFIER_SIZE_BYTES +
             ModelConfig.SENTENCE_EMBEDDINGS_SIZE_BYTES +
             ModelConfig.MOBILEBERT_QA_SIZE_BYTES) / (1024 * 1024)
        }
    }
    
    /**
     * Get size of a specific model for UI display
     */
    fun getModelSizeMB(modelType: ModelType): Long {
        val file = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> intentClassifierFile
            ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile
            ModelType.MOBILEBERT_QA -> mobileBertFile
        }
        
        val expectedSize = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> ModelConfig.INTENT_CLASSIFIER_SIZE_BYTES
            ModelType.SENTENCE_EMBEDDINGS -> ModelConfig.SENTENCE_EMBEDDINGS_SIZE_BYTES
            ModelType.MOBILEBERT_QA -> ModelConfig.MOBILEBERT_QA_SIZE_BYTES
        }
        
        return if (file.exists()) {
            file.length() / (1024 * 1024)
        } else {
            expectedSize / (1024 * 1024)
        }
    }
    
    /**
     * Called after a specific model import completes successfully
     * Refreshes model state and validates imported file
     * 
     * @param modelType The type of model that was imported
     */
    fun onModelImported(modelType: ModelType) {
        Timber.i("🧠 Model import completed for $modelType - refreshing state")
        
        // Validate the imported model
        val isValid = validateImportedModel(modelType)
        if (!isValid) {
            Timber.e("🧠 Imported model $modelType failed validation")
            _modelStateFlow.value = ModelState.Error("Model validation failed for $modelType", recoverable = true)
        } else {
            // Re-check model existence and update internal state
            val newState = getModelState()
            _modelStateFlow.value = newState
            Timber.i("🧠 New model state after $modelType import: $newState")
        }
    }
    
    /**
     * Called after model import completes successfully (legacy method)
     * Assumes all models were imported
     */
    @Deprecated("Use onModelImported(ModelType) instead to track individual model imports")
    fun onModelImported() {
        Timber.i("🧠 Model import completed - refreshing state")
        
        // Re-check model existence and update internal state
        val newState = getModelState()
        _modelStateFlow.value = newState
        Timber.i("🧠 New model state after import: $newState")
    }
    
    /**
     * Validate imported model file integrity for a specific model type
     * 
     * Checks:
     * - File exists
     * - Correct .tflite extension
     * - Correct filename
     * - Size within expected range
     * 
     * @param modelType The type of model to validate
     * @return true if model is valid, false otherwise
     */
    fun validateImportedModel(modelType: ModelType): Boolean {
        return isModelValid(modelType).also { isValid ->
            if (isValid) {
                val file = when (modelType) {
                    ModelType.INTENT_CLASSIFIER -> intentClassifierFile
                    ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile
                    ModelType.MOBILEBERT_QA -> mobileBertFile
                }
                Timber.i("🧠 Model validation passed for $modelType: ${file.length() / 1_000_000}MB")
            } else {
                Timber.w("🧠 Model validation failed for $modelType")
            }
        }
    }
    
    /**
     * Validate all imported model files
     * 
     * @return true if all present models are valid, false otherwise
     */
    @Deprecated("Use validateImportedModel(ModelType) instead")
    fun validateImportedModel(): Boolean {
        var allValid = true
        
        if (intentClassifierFile.exists()) {
            allValid = allValid && isModelValid(ModelType.INTENT_CLASSIFIER)
        }
        if (sentenceEmbeddingsFile.exists()) {
            allValid = allValid && isModelValid(ModelType.SENTENCE_EMBEDDINGS)
        }
        if (mobileBertFile.exists()) {
            allValid = allValid && isModelValid(ModelType.MOBILEBERT_QA)
        }
        
        return allValid
    }
    
    /**
     * Remove model file from device
     * 
     * Useful for:
     * - Freeing up storage space
     * - Re-importing corrupted model
     * - Troubleshooting
     * 
     * @return Result.success if deleted, Result.failure if error
     */
    fun removeModel(): Result<Unit> {
        return try {
            if (!modelFile.exists()) {
                Timber.i("🧠 No model file to remove")
                return Result.success(Unit)
            }
            
            val deleted = modelFile.delete()
            if (deleted) {
                Timber.i("🧠 Model file removed successfully")
                Result.success(Unit)
            } else {
                Timber.e("🧠 Failed to delete model file")
                Result.failure(Exception("Failed to delete model file"))
            }
        } catch (e: Exception) {
            Timber.e(e, "🧠 Error removing model file")
            Result.failure(e)
        }
    }
    
    /**
     * Get model file path for display in UI
     */
    fun getModelPath(modelType: ModelType): String {
        return when (modelType) {
            ModelType.INTENT_CLASSIFIER -> intentClassifierFile.absolutePath
            ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile.absolutePath
            ModelType.MOBILEBERT_QA -> mobileBertFile.absolutePath
        }
    }
    
    /**
     * Get model directory path (legacy method)
     */
    @Deprecated("Use getModelPath(ModelType) instead")
    fun getModelPath(): String {
        return modelDir.absolutePath
    }
}
