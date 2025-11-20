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
 * 1. Intent Classifier - for intent classification (optional)
 * 2. Sentence Embeddings - for semantic similarity (required, bundled)
 * 3. MobileBERT Q&A - for question answering (required, bundled)
 * 
 * Auto-provisioning: On first launch, ModelManager automatically detects bundled models
 * in app/src/main/assets/models/ and copies them to internal storage. No manual import needed!
 * 
 * Manual import still available as backup if user wants to use different/updated models.
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
     * Check if a model exists in the bundled assets
     */
    private fun modelExistsInAssets(modelType: ModelType): Boolean {
        val assetPath = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> "models/${ModelConfig.INTENT_CLASSIFIER_NAME}"
            ModelType.SENTENCE_EMBEDDINGS -> "models/${ModelConfig.SENTENCE_EMBEDDINGS_NAME}"
            ModelType.MOBILEBERT_QA -> "models/${ModelConfig.MOBILEBERT_QA_NAME}"
        }
        
        return try {
            context.assets.open(assetPath).use { true }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Copy model from assets to internal storage
     * 
     * @param modelType The type of model to copy
     * @return Result.success if copied successfully, Result.failure otherwise
     */
    private fun copyModelFromAssets(modelType: ModelType): Result<File> {
        val assetPath = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> "models/${ModelConfig.INTENT_CLASSIFIER_NAME}"
            ModelType.SENTENCE_EMBEDDINGS -> "models/${ModelConfig.SENTENCE_EMBEDDINGS_NAME}"
            ModelType.MOBILEBERT_QA -> "models/${ModelConfig.MOBILEBERT_QA_NAME}"
        }
        
        val targetFile = when (modelType) {
            ModelType.INTENT_CLASSIFIER -> intentClassifierFile
            ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile
            ModelType.MOBILEBERT_QA -> mobileBertFile
        }
        
        return try {
            Timber.i("🧠 Copying $modelType from assets/$assetPath to ${targetFile.absolutePath}...")
            
            // Ensure parent directory exists
            if (!modelDir.exists()) {
                Timber.d("🧠 Creating model directory: ${modelDir.absolutePath}")
                modelDir.mkdirs()
            }
            
            // Copy to temp file first to avoid losing valid model if copy fails
            val tempFile = File(modelDir, "${targetFile.name}.tmp")
            if (tempFile.exists()) {
                tempFile.delete()
            }
            
            context.assets.open(assetPath).use { input ->
                FileOutputStream(tempFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Timber.d("🧠 Copied $bytesCopied bytes to temp file")
                }
            }
            
            // Verify temp file was created successfully
            if (!tempFile.exists() || tempFile.length() == 0L) {
                tempFile.delete()
                throw Exception("Temp file validation failed after copy")
            }
            
            // Atomic swap: Preserve existing valid model until new one is confirmed
            val backupFile = if (targetFile.exists()) {
                val backup = File(modelDir, "${targetFile.name}.bak")
                if (backup.exists()) backup.delete()
                Timber.d("🧠 Backing up existing file before replacement")
                
                // Try rename first (atomic), fallback to copy if locked/cross-filesystem
                if (targetFile.renameTo(backup)) {
                    backup
                } else {
                    Timber.d("🧠 Rename to backup failed, using copy-based backup")
                    targetFile.copyTo(backup, overwrite = false)
                    
                    // CRITICAL: Ensure original is deleted before swap attempt
                    if (!targetFile.delete()) {
                        backup.delete()
                        throw Exception("Cannot delete original file after backup - file may be locked")
                    }
                    backup
                }
            } else {
                null
            }
            
            try {
                // Try atomic rename first (fastest)
                if (!tempFile.renameTo(targetFile)) {
                    // Fallback: copy if rename fails (different filesystems)
                    Timber.d("🧠 Rename failed, using copy fallback")
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
                
                // Success! Delete backup if present
                backupFile?.delete()
            } catch (e: Exception) {
                // Restore backup on failure with robust fallback
                if (backupFile != null && backupFile.exists()) {
                    Timber.w("🧠 Swap failed, restoring backup")
                    
                    // Try rename first, fallback to copy (mirror forward path)
                    if (!backupFile.renameTo(targetFile)) {
                        Timber.d("🧠 Backup restore rename failed, using copy")
                        try {
                            // Use overwrite=true to handle any partially written target
                            backupFile.copyTo(targetFile, overwrite = true)
                            backupFile.delete()
                        } catch (restoreError: Exception) {
                            Timber.e(restoreError, "🧠 CRITICAL: Failed to restore backup! Backup preserved at ${backupFile.absolutePath}")
                            // Don't delete backup - let user recover manually
                            tempFile.delete()
                            throw Exception("Model provisioning failed and backup restoration failed. Backup preserved at ${backupFile.absolutePath}", e)
                        }
                    } else {
                        // Rename succeeded, safe to delete backup
                        backupFile.delete()
                    }
                }
                tempFile.delete()
                throw e
            }
            
            val sizeMB = targetFile.length() / (1024 * 1024)
            Timber.i("🧠 ✅ Successfully copied $modelType (${sizeMB}MB) to ${targetFile.absolutePath}")
            
            Result.success(targetFile)
        } catch (e: Exception) {
            Timber.e(e, "🧠 ❌ Failed to copy $modelType from assets/$assetPath - existing valid model preserved if present")
            Result.failure(e)
        }
    }
    
    /**
     * Auto-provision models from bundled assets if not already in internal storage
     * This runs automatically on first launch to set up the models
     * 
     * @return Map of ModelType to Result indicating which models were provisioned
     */
    private fun autoProvisionFromAssets(): Map<ModelType, Result<File>> {
        Timber.i("🧠 ====== AUTO-PROVISION STARTING ======")
        val results = mutableMapOf<ModelType, Result<File>>()
        
        // Only provision required models (embeddings + mobilebert)
        // Intent classifier is optional and not bundled by default
        val requiredModels = listOf(
            ModelType.SENTENCE_EMBEDDINGS,
            ModelType.MOBILEBERT_QA
        )
        
        for (modelType in requiredModels) {
            Timber.i("🧠 Processing $modelType...")
            
            // Check if model already exists in internal storage
            val targetFile = when (modelType) {
                ModelType.SENTENCE_EMBEDDINGS -> sentenceEmbeddingsFile
                ModelType.MOBILEBERT_QA -> mobileBertFile
                else -> continue
            }
            
            Timber.d("🧠 Target file: ${targetFile.absolutePath}")
            Timber.d("🧠 File exists: ${targetFile.exists()}")
            
            if (targetFile.exists()) {
                val isValid = isModelValid(modelType)
                Timber.d("🧠 File is valid: $isValid")
                
                if (isValid) {
                    Timber.i("🧠 ✅ $modelType already exists and is valid, skipping provision")
                    results[modelType] = Result.success(targetFile)
                    continue
                } else {
                    Timber.w("🧠 ⚠️ $modelType exists but is invalid, will re-provision from assets")
                }
            }
            
            // Check if model exists in assets
            val existsInAssets = modelExistsInAssets(modelType)
            Timber.d("🧠 Model exists in assets: $existsInAssets")
            
            if (!existsInAssets) {
                Timber.e("🧠 ❌ $modelType not found in bundled assets!")
                results[modelType] = Result.failure(
                    Exception("Model not found in assets: $modelType")
                )
                continue
            }
            
            // Copy from assets to internal storage
            Timber.i("🧠 Attempting to copy $modelType from assets...")
            val result = copyModelFromAssets(modelType)
            results[modelType] = result
            
            if (result.isSuccess) {
                Timber.i("🧠 ✅ Auto-provisioned $modelType from bundled assets")
            } else {
                Timber.e("🧠 ❌ Failed to auto-provision $modelType: ${result.exceptionOrNull()?.message}")
            }
        }
        
        Timber.i("🧠 ====== AUTO-PROVISION COMPLETE ======")
        return results
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
     * 
     * Auto-provisions bundled models from assets on first launch if needed.
     * 
     * Required models: SENTENCE_EMBEDDINGS + MOBILEBERT_QA (2 models)
     * Optional model: INTENT_CLASSIFIER (bonus feature)
     */
    fun getModelState(): ModelState {
        val importedModels = mutableSetOf<ModelType>()
        
        // First pass: Check which models already exist in internal storage
        if (intentClassifierFile.exists() && isModelValid(ModelType.INTENT_CLASSIFIER)) {
            importedModels.add(ModelType.INTENT_CLASSIFIER)
        }
        if (sentenceEmbeddingsFile.exists() && isModelValid(ModelType.SENTENCE_EMBEDDINGS)) {
            importedModels.add(ModelType.SENTENCE_EMBEDDINGS)
        }
        if (mobileBertFile.exists() && isModelValid(ModelType.MOBILEBERT_QA)) {
            importedModels.add(ModelType.MOBILEBERT_QA)
        }
        
        // Check if we have the 2 required models (embeddings + mobilebert)
        val hasRequiredModels = importedModels.contains(ModelType.SENTENCE_EMBEDDINGS) && 
                                importedModels.contains(ModelType.MOBILEBERT_QA)
        
        // If required models are missing, try to auto-provision from bundled assets
        if (!hasRequiredModels) {
            Timber.i("🧠 Required models missing in internal storage, checking bundled assets...")
            
            val provisionResults = autoProvisionFromAssets()
            
            // Re-check which models are now available after auto-provisioning
            importedModels.clear()
            if (intentClassifierFile.exists() && isModelValid(ModelType.INTENT_CLASSIFIER)) {
                importedModels.add(ModelType.INTENT_CLASSIFIER)
            }
            if (sentenceEmbeddingsFile.exists() && isModelValid(ModelType.SENTENCE_EMBEDDINGS)) {
                importedModels.add(ModelType.SENTENCE_EMBEDDINGS)
            }
            if (mobileBertFile.exists() && isModelValid(ModelType.MOBILEBERT_QA)) {
                importedModels.add(ModelType.MOBILEBERT_QA)
            }
            
            // Log provisioning summary
            val successCount = provisionResults.values.count { it.isSuccess }
            val totalAttempted = provisionResults.size
            Timber.i("🧠 Auto-provisioning complete: $successCount/$totalAttempted models provisioned successfully")
        }
        
        // Final check: Do we have required models?
        val finalHasRequiredModels = importedModels.contains(ModelType.SENTENCE_EMBEDDINGS) && 
                                     importedModels.contains(ModelType.MOBILEBERT_QA)
        
        return when {
            importedModels.isEmpty() -> ModelState.NotDownloaded
            finalHasRequiredModels -> {
                Timber.i("🧠 All required models available: ${importedModels.size} total")
                ModelState.Downloaded  // 2 or 3 models present with required ones
            }
            else -> {
                Timber.w("🧠 Only partial models available: $importedModels")
                ModelState.PartiallyDownloaded(
                    importedCount = importedModels.size,
                    importedModels = importedModels
                )
            }
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
     * Remove all model files from device
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
            val hasModels = intentClassifierFile.exists() || 
                           sentenceEmbeddingsFile.exists() || 
                           mobileBertFile.exists()
            
            if (!hasModels) {
                Timber.i("🧠 No model files to remove")
                return Result.success(Unit)
            }
            
            val deleted = deleteModel()
            if (deleted) {
                Timber.i("🧠 All model files removed successfully")
                Result.success(Unit)
            } else {
                Timber.e("🧠 Failed to delete some model files")
                Result.failure(Exception("Failed to delete model files"))
            }
        } catch (e: Exception) {
            Timber.e(e, "🧠 Error removing model files")
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
