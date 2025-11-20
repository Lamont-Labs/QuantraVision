package com.lamontlabs.quantravision.devbot.diagnostics

import android.content.Context
import com.lamontlabs.quantravision.intelligence.llm.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

data class ModelFileInfo(
    val modelType: ModelType,
    val fileName: String,
    val exists: Boolean,
    val size: Long = 0,
    val sizeReadable: String = "0 B",
    val checksum: String = "",
    val location: String = "",
    val isValid: Boolean = false,
    val validationError: String? = null,
    val lastModified: Long = 0,
    val lastModifiedReadable: String = ""
)

data class ModelInitializationStatus(
    val timestamp: Long = System.currentTimeMillis(),
    val timestampReadable: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()),
    val modelType: ModelType,
    val initialized: Boolean,
    val initializationTime: Long? = null,
    val error: String? = null,
    val stackTrace: String? = null
)

data class ComprehensiveModelDiagnostics(
    val filesInAssets: List<ModelFileInfo>,
    val filesInInternalStorage: List<ModelFileInfo>,
    val initializationStatuses: List<ModelInitializationStatus>,
    val totalAssetsSize: Long,
    val totalInternalSize: Long,
    val recommendations: List<String>
)

object ModelDiagnostics {
    
    private val initStatuses = mutableMapOf<ModelType, ModelInitializationStatus>()
    
    suspend fun diagnose(context: Context): ComprehensiveModelDiagnostics = withContext(Dispatchers.IO) {
        Timber.i("🔬 MODEL DIAGNOSTICS: Starting comprehensive model analysis...")
        
        val assetsModels = diagnoseBundledAssets(context)
        val internalModels = diagnoseInternalStorage(context)
        val recommendations = generateRecommendations(assetsModels, internalModels)
        
        val totalAssetsSize = assetsModels.sumOf { it.size }
        val totalInternalSize = internalModels.sumOf { it.size }
        
        ComprehensiveModelDiagnostics(
            filesInAssets = assetsModels,
            filesInInternalStorage = internalModels,
            initializationStatuses = initStatuses.values.toList(),
            totalAssetsSize = totalAssetsSize,
            totalInternalSize = totalInternalSize,
            recommendations = recommendations
        )
    }
    
    private fun diagnoseBundledAssets(context: Context): List<ModelFileInfo> {
        val assetManager = context.assets
        val modelFiles = mutableListOf<ModelFileInfo>()
        
        // Check for bundled models in assets/models/
        ModelType.values().forEach { modelType ->
            val assetPath = "models/${getModelFileName(modelType)}"
            
            try {
                val inputStream = assetManager.open(assetPath)
                val size = inputStream.available().toLong()
                inputStream.close()
                
                modelFiles.add(
                    ModelFileInfo(
                        modelType = modelType,
                        fileName = getModelFileName(modelType),
                        exists = true,
                        size = size,
                        sizeReadable = formatFileSize(size),
                        checksum = "bundled-asset",
                        location = "assets/$assetPath",
                        isValid = size > 1000, // Basic validation: > 1KB
                        validationError = if (size < 1000) "File too small" else null,
                        lastModified = 0,
                        lastModifiedReadable = "Bundled in APK"
                    )
                )
                
                Timber.d("🔬 Found bundled model: $assetPath (${formatFileSize(size)})")
            } catch (e: Exception) {
                Timber.d("🔬 Bundled model not found: $assetPath")
            }
        }
        
        return modelFiles
    }
    
    private fun diagnoseInternalStorage(context: Context): List<ModelFileInfo> {
        val modelFiles = mutableListOf<ModelFileInfo>()
        val modelsDir = File(context.filesDir, "models")
        
        if (!modelsDir.exists()) {
            Timber.w("🔬 Models directory does not exist in internal storage")
            return modelFiles
        }
        
        ModelType.values().forEach { modelType ->
            val file = File(modelsDir, getModelFileName(modelType))
            
            if (file.exists()) {
                val size = file.length()
                val checksum = try {
                    calculateMD5(file)
                } catch (e: Exception) {
                    "error"
                }
                
                modelFiles.add(
                    ModelFileInfo(
                        modelType = modelType,
                        fileName = file.name,
                        exists = true,
                        size = size,
                        sizeReadable = formatFileSize(size),
                        checksum = checksum,
                        location = file.absolutePath,
                        isValid = size > 1000 && file.canRead(),
                        validationError = when {
                            size < 1000 -> "File too small"
                            !file.canRead() -> "File not readable"
                            else -> null
                        },
                        lastModified = file.lastModified(),
                        lastModifiedReadable = java.text.SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", 
                            java.util.Locale.US
                        ).format(java.util.Date(file.lastModified()))
                    )
                )
                
                Timber.d("🔬 Found internal model: ${file.name} (${formatFileSize(size)})")
            }
        }
        
        return modelFiles
    }
    
    fun recordInitialization(
        modelType: ModelType,
        success: Boolean,
        initTime: Long? = null,
        error: String? = null,
        stackTrace: String? = null
    ) {
        initStatuses[modelType] = ModelInitializationStatus(
            modelType = modelType,
            initialized = success,
            initializationTime = initTime,
            error = error,
            stackTrace = stackTrace
        )
        
        val emoji = if (success) "✅" else "❌"
        Timber.i("🔬 $emoji Model initialization: $modelType - ${if (success) "SUCCESS" else "FAILED: $error"}")
    }
    
    private fun generateRecommendations(
        assetsModels: List<ModelFileInfo>,
        internalModels: List<ModelFileInfo>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // Check if sentence embeddings model is present
        val hasEmbeddings = internalModels.any { 
            it.modelType == ModelType.SENTENCE_EMBEDDINGS && it.isValid 
        }
        
        if (!hasEmbeddings) {
            val embeddingsInAssets = assetsModels.any { 
                it.modelType == ModelType.SENTENCE_EMBEDDINGS && it.exists 
            }
            
            if (embeddingsInAssets) {
                recommendations.add("⚠️ Embeddings model found in assets but not in internal storage - auto-provision may have failed")
            } else {
                recommendations.add("❌ Embeddings model missing - AI features will not work")
            }
        }
        
        // Check MobileBERT (optional)
        val hasMobileBERT = internalModels.any { 
            it.modelType == ModelType.MOBILEBERT_QA && it.isValid 
        }
        
        if (!hasMobileBERT) {
            recommendations.add("ℹ️ MobileBERT Q&A model not available - using retrieval-only mode (expected)")
        }
        
        // Check for failed initializations
        val failedInits = initStatuses.values.filter { !it.initialized }
        failedInits.forEach { status ->
            recommendations.add("❌ ${status.modelType} initialization failed: ${status.error}")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("✅ All models are healthy")
        }
        
        return recommendations
    }
    
    private fun getModelFileName(modelType: ModelType): String {
        return when (modelType) {
            ModelType.SENTENCE_EMBEDDINGS -> "sentence_embeddings.tflite"
            ModelType.MOBILEBERT_QA -> "mobilebert_qa_squad.tflite"
            ModelType.INTENT_CLASSIFIER -> "intent_classifier.tflite"
        }
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
    
    private fun calculateMD5(file: File): String {
        val md = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var read: Int
            while (input.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
