package com.lamontlabs.quantravision.intelligence.llm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.lamontlabs.quantravision.overlay.OverlayServiceGuard
import com.lamontlabs.quantravision.ui.MetallicButton
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.QuantraVisionTheme
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Multi-step model import activity for 3 separate TFLite models
 * 
 * Handles sequential import of:
 * 1. Intent Classifier (intent_classifier.tflite)
 * 2. Sentence Embeddings (sentence_embeddings.tflite)
 * 3. MobileBERT Q&A (mobilebert_qa_squad.tflite)
 * 
 * Requires MANAGE_EXTERNAL_STORAGE permission on Android 11+ to access Download folder.
 */
class ImportActivity : ComponentActivity() {
    
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkPermissionsAndProceed()
    }
    
    private var pendingFilePath: String? = null
    private var pendingModelType: ModelType? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("📥 ImportActivity: onCreate (3-step import mode)")
        
        setContent {
            QuantraVisionTheme {
                MultiStepImportScreen(
                    onImportComplete = {
                        Timber.i("📥 All imports complete, re-enabling OverlayService")
                        OverlayServiceGuard.enable(this)
                        setResult(Activity.RESULT_OK)
                        finish()
                    },
                    onCancel = {
                        Timber.i("📥 Import cancelled, re-enabling OverlayService")
                        OverlayServiceGuard.enable(this)
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    },
                    onImport = { filePath, modelType ->
                        pendingFilePath = filePath
                        pendingModelType = modelType
                        checkPermissionsAndProceed()
                    }
                )
            }
        }
    }
    
    private fun checkPermissionsAndProceed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ requires MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                Timber.w("📥 MANAGE_EXTERNAL_STORAGE permission not granted")
                Toast.makeText(
                    this,
                    "Please grant 'All files access' permission to import model files",
                    Toast.LENGTH_LONG
                ).show()
                
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:$packageName")
                    storagePermissionLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    storagePermissionLauncher.launch(intent)
                }
                return
            }
        } else {
            // Android 10 and below
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    100
                )
                return
            }
        }
        
        // Permission granted, proceed with import
        val filePath = pendingFilePath
        val modelType = pendingModelType
        if (filePath != null && modelType != null) {
            importFromPath(filePath, modelType)
        }
    }
    
    
    private fun importFromPath(filePath: String, modelType: ModelType) {
        lifecycleScope.launch {
            try {
                Timber.i("📥 Importing $modelType from path: $filePath")
                
                withContext(Dispatchers.IO) {
                    val sourceFile = File(filePath)
                    
                    // Validate source file exists
                    if (!sourceFile.exists()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ImportActivity,
                                "File not found at: $filePath",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@withContext
                    }
                    
                    if (!sourceFile.canRead()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ImportActivity,
                                "Cannot read file. Check permissions.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@withContext
                    }
                    
                    // Validate filename matches expected model type
                    val expectedFilename = when (modelType) {
                        ModelType.INTENT_CLASSIFIER -> ModelConfig.INTENT_CLASSIFIER_NAME
                        ModelType.SENTENCE_EMBEDDINGS -> ModelConfig.SENTENCE_EMBEDDINGS_NAME
                        ModelType.MOBILEBERT_QA -> ModelConfig.MOBILEBERT_QA_NAME
                    }
                    
                    if (!sourceFile.name.equals(expectedFilename, ignoreCase = true)) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@ImportActivity,
                                "❌ Incorrect filename! Expected: $expectedFilename\nGot: ${sourceFile.name}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@withContext
                    }
                    
                    // Copy file to ModelManager's expected location
                    val modelDir = File(this@ImportActivity.filesDir, "llm_models")
                    modelDir.mkdirs()
                    val destFile = File(modelDir, expectedFilename)
                    
                    val modelTypeName = when (modelType) {
                        ModelType.INTENT_CLASSIFIER -> "Intent Classifier"
                        ModelType.SENTENCE_EMBEDDINGS -> "Sentence Embeddings"
                        ModelType.MOBILEBERT_QA -> "MobileBERT Q&A"
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ImportActivity,
                            "Importing $modelTypeName (${sourceFile.length() / 1_000_000}MB)...",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    
                    Timber.i("📥 Copying ${sourceFile.length()} bytes to ${destFile.absolutePath}")
                    
                    FileInputStream(sourceFile).use { input ->
                        FileOutputStream(destFile).use { output ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    
                    Timber.i("📥 Copy complete, file size: ${destFile.length()} bytes")
                    
                    // Update ModelManager state for this specific model
                    val modelManager = ModelManager(this@ImportActivity)
                    modelManager.onModelImported(modelType)
                }
                
                // Success - don't finish, let user continue importing
                withContext(Dispatchers.Main) {
                    val modelTypeName = when (modelType) {
                        ModelType.INTENT_CLASSIFIER -> "Intent Classifier"
                        ModelType.SENTENCE_EMBEDDINGS -> "Sentence Embeddings"
                        ModelType.MOBILEBERT_QA -> "MobileBERT Q&A"
                    }
                    Toast.makeText(
                        this@ImportActivity,
                        "✓ $modelTypeName imported successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Clear pending state
                    pendingFilePath = null
                    pendingModelType = null
                }
                
            } catch (e: Exception) {
                Timber.e(e, "📥 Import failed for $modelType")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ImportActivity,
                        "Import failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

@Composable
private fun MultiStepImportScreen(
    onImportComplete: () -> Unit,
    onCancel: () -> Unit,
    onImport: (String, ModelType) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val modelManager = remember { ModelManager(context) }
    
    var modelState by remember { mutableStateOf(modelManager.getModelState()) }
    var filePath by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(1) }
    var isImporting by remember { mutableStateOf(false) }
    
    // Determine which models are imported
    val importedModels = when (val state = modelState) {
        is ModelState.PartiallyDownloaded -> state.importedModels
        is ModelState.Downloaded -> setOf(ModelType.INTENT_CLASSIFIER, ModelType.SENTENCE_EMBEDDINGS, ModelType.MOBILEBERT_QA)
        else -> emptySet()
    }
    
    val totalModels = 3
    val importedCount = importedModels.size
    
    // Determine current model to import based on what's missing
    val pendingModelType = when {
        ModelType.INTENT_CLASSIFIER !in importedModels -> ModelType.INTENT_CLASSIFIER
        ModelType.SENTENCE_EMBEDDINGS !in importedModels -> ModelType.SENTENCE_EMBEDDINGS
        ModelType.MOBILEBERT_QA !in importedModels -> ModelType.MOBILEBERT_QA
        else -> null
    }
    
    // Update default file path based on current model
    LaunchedEffect(pendingModelType) {
        filePath = when (pendingModelType) {
            ModelType.INTENT_CLASSIFIER -> "/storage/emulated/0/Download/${ModelConfig.INTENT_CLASSIFIER_NAME}"
            ModelType.SENTENCE_EMBEDDINGS -> "/storage/emulated/0/Download/${ModelConfig.SENTENCE_EMBEDDINGS_NAME}"
            ModelType.MOBILEBERT_QA -> "/storage/emulated/0/Download/${ModelConfig.MOBILEBERT_QA_NAME}"
            null -> ""
        }
    }
    
    // Refresh model state periodically
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(500)
            modelState = modelManager.getModelState()
            isImporting = false  // Reset importing flag when state changes
        }
    }
    
    StaticBrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📦",
                style = AppTypography.headlineLarge.copy(
                    fontSize = AppTypography.headlineLarge.fontSize * 2
                )
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            Text(
                text = "Import AI Models",
                style = AppTypography.headlineLarge,
                color = AppColors.NeonCyan,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            
            Text(
                text = "$importedCount / $totalModels models imported",
                style = AppTypography.bodyLarge,
                color = if (importedCount == totalModels) AppColors.MetallicGold else AppColors.MetallicSilver,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            // Status cards for each model
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    ModelStatusRow(
                        modelName = "Intent Classifier",
                        filename = ModelConfig.INTENT_CLASSIFIER_NAME,
                        isImported = ModelType.INTENT_CLASSIFIER in importedModels,
                        isCurrent = pendingModelType == ModelType.INTENT_CLASSIFIER
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    ModelStatusRow(
                        modelName = "Sentence Embeddings",
                        filename = ModelConfig.SENTENCE_EMBEDDINGS_NAME,
                        isImported = ModelType.SENTENCE_EMBEDDINGS in importedModels,
                        isCurrent = pendingModelType == ModelType.SENTENCE_EMBEDDINGS
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    
                    ModelStatusRow(
                        modelName = "MobileBERT Q&A",
                        filename = ModelConfig.MOBILEBERT_QA_NAME,
                        isImported = ModelType.MOBILEBERT_QA in importedModels,
                        isCurrent = pendingModelType == ModelType.MOBILEBERT_QA
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            // Show import UI only if not all models are imported
            if (pendingModelType != null) {
                MetallicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(AppSpacing.lg)) {
                        val modelTypeName = when (pendingModelType) {
                            ModelType.INTENT_CLASSIFIER -> "Intent Classifier"
                            ModelType.SENTENCE_EMBEDDINGS -> "Sentence Embeddings"
                            ModelType.MOBILEBERT_QA -> "MobileBERT Q&A"
                        }
                        
                        Text(
                            text = "Import: $modelTypeName",
                            style = AppTypography.bodyLarge,
                            color = AppColors.NeonCyan,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        
                        OutlinedTextField(
                            value = filePath,
                            onValueChange = { filePath = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("File Path") },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.NeonCyan,
                                unfocusedBorderColor = AppColors.MetallicSilver,
                                focusedLabelColor = AppColors.NeonCyan,
                                unfocusedLabelColor = AppColors.MetallicSilver,
                                cursorColor = AppColors.NeonCyan
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        
                        Text(
                            text = "💡 Tip: Place files in /storage/emulated/0/Download/",
                            style = AppTypography.bodySmall,
                            color = AppColors.MetallicSilver.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                
                MetallicButton(
                    onClick = {
                        isImporting = true
                        onImport(filePath, pendingModelType)
                    },
                    enabled = !isImporting && filePath.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AppColors.NeonCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(AppSpacing.sm))
                        Text("Importing...")
                    } else {
                        Text("Import ${when (pendingModelType) {
                            ModelType.INTENT_CLASSIFIER -> "Intent Classifier"
                            ModelType.SENTENCE_EMBEDDINGS -> "Embeddings"
                            ModelType.MOBILEBERT_QA -> "Q&A Model"
                        }}")
                    }
                }
            } else {
                // All models imported - show complete button
                MetallicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(AppSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✓ All Models Imported!",
                            style = AppTypography.headlineMedium,
                            color = AppColors.MetallicGold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.sm))
                        
                        Text(
                            text = "All 3 AI models are ready for use.",
                            style = AppTypography.bodyMedium,
                            color = AppColors.MetallicSilver,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                
                MetallicButton(
                    onClick = onImportComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Complete Setup")
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            TextButton(onClick = onCancel) {
                Text(
                    if (importedCount == 0) "Cancel" else "Close",
                    color = AppColors.MetallicSilver
                )
            }
        }
    }
}

@Composable
private fun ModelStatusRow(
    modelName: String,
    filename: String,
    isImported: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isImported) "✓" else if (isCurrent) "→" else "○",
            style = AppTypography.headlineSmall,
            color = if (isImported) AppColors.MetallicGold else if (isCurrent) AppColors.NeonCyan else AppColors.MetallicSilver.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.width(AppSpacing.md))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = modelName,
                style = AppTypography.bodyMedium,
                color = if (isCurrent) AppColors.NeonCyan else AppColors.MetallicSilver
            )
            Text(
                text = filename,
                style = AppTypography.bodySmall,
                color = AppColors.MetallicSilver.copy(alpha = 0.7f)
            )
        }
        
        if (isImported) {
            Text(
                text = "Imported",
                style = AppTypography.bodySmall,
                color = AppColors.MetallicGold
            )
        } else if (isCurrent) {
            Text(
                text = "Pending",
                style = AppTypography.bodySmall,
                color = AppColors.NeonCyan
            )
        }
    }
}
