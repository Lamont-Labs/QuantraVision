package com.lamontlabs.quantravision.intelligence.llm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.StatFs
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.io.File

/**
 * Import state tracking for model import workflow
 */
sealed class ImportState {
    data object Idle : ImportState()
    data object Selecting : ImportState()
    data class Copying(val progress: Int, val totalBytes: Long) : ImportState()
    data class Validating(val progress: Int) : ImportState()
    data object Success : ImportState()
    data class Error(val message: String, val recoverable: Boolean) : ImportState()
}

/**
 * Controller for importing Gemma model files from user's Downloads folder
 * 
 * Uses Android Storage Access Framework to enable mobile-only model import:
 * 1. User downloads model from HuggingFace to phone
 * 2. User taps "Import Model" button
 * 3. Android file picker opens
 * 4. User selects .task file from Downloads
 * 5. App copies file in background with progress
 * 6. Model becomes ready for AI inference
 * 
 * ## Key Features
 * - NO permissions required (SAF handles it)
 * - Streaming copy for large files (529MB)
 * - Background processing with WorkManager
 * - Progress tracking via StateFlow
 * - Cancellation support
 * - Storage space validation
 * 
 * ## Usage
 * ```
 * val controller = ModelImportController(context)
 * controller.setupFilePicker(activity) { launcher ->
 *     // Store launcher for later use
 * }
 * controller.startImport(launcher)
 * ```
 */
class ModelImportController(private val context: Context) {
    
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()
    
    private val workManager = WorkManager.getInstance(context)
    private val modelManager = ModelManager(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var currentWorkId: java.util.UUID? = null
    
    companion object {
        private const val WORK_NAME = "model_import_work"
        const val KEY_SOURCE_URI = "source_uri"
        const val KEY_FILE_SIZE = "file_size"
        const val KEY_PROGRESS = "progress"
        const val KEY_TOTAL_BYTES = "total_bytes"
        const val KEY_ERROR_MESSAGE = "error_message"
        
        private const val MIN_FREE_SPACE_BYTES = 1_073_741_824L // 1GB required
        private const val EXPECTED_MODEL_SIZE_BYTES = 529_000_000L // ~529MB
        private const val SIZE_TOLERANCE = 0.15f // Allow 15% variance
    }
    
    init {
        try {
            val existingWork = workManager.getWorkInfosForUniqueWork(WORK_NAME).get()
            
            if (existingWork.isNotEmpty()) {
                val workInfo = existingWork.first()
                
                when (workInfo.state) {
                    WorkInfo.State.RUNNING, WorkInfo.State.ENQUEUED -> {
                        currentWorkId = workInfo.id
                        observeWorkProgress(workInfo.id)
                        _importState.value = ImportState.Copying(
                            progress = workInfo.progress.getInt(KEY_PROGRESS, 0),
                            totalBytes = workInfo.progress.getLong(KEY_TOTAL_BYTES, 0L)
                        )
                        Timber.i("📥 Resuming existing import work: ${workInfo.id}")
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _importState.value = ImportState.Success
                        Timber.i("📥 Import already completed while away")
                    }
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.outputData.getString(KEY_ERROR_MESSAGE) ?: "Import failed"
                        _importState.value = ImportState.Error(error, true)
                        Timber.w("📥 Previous import failed: $error")
                    }
                    else -> {
                        _importState.value = ImportState.Idle
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error checking for existing import work")
        }
    }
    
    /**
     * Setup file picker launcher
     * 
     * Must be called during Activity initialization before onCreate() returns.
     * Provides ActivityResultLauncher that can be used to launch picker.
     */
    fun setupFilePicker(
        activity: AppCompatActivity,
        onLauncherReady: (ActivityResultLauncher<Array<String>>) -> Unit
    ) {
        val launcher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->
            if (uri != null) {
                handleFileSelected(uri)
            } else {
                _importState.value = ImportState.Error(
                    "No file selected",
                    recoverable = true
                )
            }
        }
        
        onLauncherReady(launcher)
    }
    
    /**
     * Start import workflow by launching file picker
     */
    fun startImport(launcher: ActivityResultLauncher<Array<String>>) {
        _importState.value = ImportState.Selecting
        
        // Launch picker with .task MIME type filter
        launcher.launch(arrayOf("*/*")) // Accept all, validate by extension
    }
    
    /**
     * Handle file selection from picker
     */
    fun handleFileSelected(uri: Uri) {
        try {
            Timber.i("📥 File selected: $uri")
            
            // Try to take persistable permission (optional - some providers don't support it)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Timber.i("📥 Persistable permission granted")
            } catch (e: SecurityException) {
                Timber.w("📥 Persistable permission not available - will copy immediately")
                // Not a fatal error - we'll copy the file immediately instead
            }
            
            // Validate file
            Timber.i("📥 Validating file...")
            val validation = validateSelectedFile(uri)
            if (!validation.isValid) {
                Timber.e("📥 Validation failed: ${validation.errorMessage}")
                _importState.value = ImportState.Error(
                    validation.errorMessage ?: "Invalid file",
                    recoverable = true
                )
                return
            }
            Timber.i("📥 Validation passed: ${validation.fileSize / 1_000_000}MB")
            
            // Check storage space
            if (!hasEnoughSpace()) {
                Timber.e("📥 Not enough storage space")
                _importState.value = ImportState.Error(
                    "Not enough storage space. Need at least 1GB free.",
                    recoverable = false
                )
                return
            }
            
            // Start background copy with WorkManager (pass validated file size)
            Timber.i("📥 Starting background copy...")
            startBackgroundCopy(uri, validation.fileSize)
            
        } catch (e: Exception) {
            Timber.e(e, "📥 ERROR in handleFileSelected")
            _importState.value = ImportState.Error(
                "Failed to process file: ${e.message}",
                recoverable = true
            )
        }
    }
    
    /**
     * Validate selected file meets requirements
     */
    private fun validateSelectedFile(uri: Uri): FileValidation {
        try {
            val fileName = getFileName(uri)
            val fileSize = getFileSize(uri)
            
            // Check file extension
            if (!fileName.endsWith(".task")) {
                return FileValidation(
                    isValid = false,
                    errorMessage = "Invalid file type. Must be a .task file (MediaPipe format)"
                )
            }
            
            // Check file size (allow 15% tolerance)
            val minSize = (EXPECTED_MODEL_SIZE_BYTES * (1 - SIZE_TOLERANCE)).toLong()
            val maxSize = (EXPECTED_MODEL_SIZE_BYTES * (1 + SIZE_TOLERANCE)).toLong()
            
            if (fileSize !in minSize..maxSize) {
                return FileValidation(
                    isValid = false,
                    errorMessage = "File size ${fileSize / 1_000_000}MB is outside expected range " +
                            "${minSize / 1_000_000}MB-${maxSize / 1_000_000}MB"
                )
            }
            
            // Validate file name contains expected model name
            if (!fileName.contains("gemma", ignoreCase = true)) {
                Timber.w("File name '$fileName' doesn't contain 'gemma' - might not be correct model")
            }
            
            return FileValidation(isValid = true, fileSize = fileSize)
            
        } catch (e: SecurityException) {
            Timber.e(e, "📥 SecurityException: Lost file access permission")
            return FileValidation(
                isValid = false,
                errorMessage = "File permission lost. Please select the file again."
            )
        } catch (e: Exception) {
            Timber.e(e, "Error validating file")
            return FileValidation(
                isValid = false,
                errorMessage = "Cannot read file: ${e.message}"
            )
        }
    }
    
    /**
     * Get file name from URI with defensive error handling
     */
    private fun getFileName(uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
            uri.lastPathSegment ?: "unknown"
        } catch (e: SecurityException) {
            Timber.e(e, "📥 SecurityException querying file name")
            throw e // Re-throw to be caught by validateSelectedFile
        } catch (e: Exception) {
            Timber.e(e, "📥 Error getting file name")
            uri.lastPathSegment ?: "unknown"
        }
    }
    
    /**
     * Get file size from URI with defensive error handling  
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        return cursor.getLong(sizeIndex)
                    }
                }
            }
            0L
        } catch (e: SecurityException) {
            Timber.e(e, "📥 SecurityException querying file size")
            throw e // Re-throw to be caught by validateSelectedFile
        } catch (e: Exception) {
            Timber.e(e, "📥 Error getting file size")
            0L
        }
    }
    
    /**
     * Check if device has enough free space
     */
    private fun hasEnoughSpace(): Boolean {
        val filesDir = context.filesDir
        val stat = StatFs(filesDir.absolutePath)
        val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
        
        Timber.i("Available space: ${availableBytes / 1_000_000}MB, Required: ${MIN_FREE_SPACE_BYTES / 1_000_000}MB")
        return availableBytes >= MIN_FREE_SPACE_BYTES
    }
    
    /**
     * Start background copy using WorkManager
     */
    private fun startBackgroundCopy(uri: Uri, fileSize: Long) {
        val inputData = Data.Builder()
            .putString(KEY_SOURCE_URI, uri.toString())
            .putLong(KEY_FILE_SIZE, fileSize)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ImportModelWorker>()
            .setInputData(inputData)
            .setConstraints(Constraints.Builder().build())
            .build()
        
        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        currentWorkId = workRequest.id
        observeWorkProgress(workRequest.id)
        
        Timber.i("📥 Starting import: ${fileSize / 1_000_000}MB file")
    }
    
    /**
     * Observe WorkManager progress using Flow (no memory leaks)
     */
    private fun observeWorkProgress(workId: java.util.UUID) {
        workManager.getWorkInfoByIdFlow(workId)
            .onEach { workInfo ->
                when (workInfo?.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(KEY_PROGRESS, 0)
                        val totalBytes = workInfo.progress.getLong(KEY_TOTAL_BYTES, 0L)
                        _importState.value = ImportState.Copying(progress, totalBytes)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _importState.value = ImportState.Success
                        modelManager.onModelImported()
                    }
                    WorkInfo.State.FAILED -> {
                        val errorMsg = workInfo.outputData.getString("error") ?: "Import failed"
                        _importState.value = ImportState.Error(errorMsg, recoverable = true)
                    }
                    WorkInfo.State.CANCELLED -> {
                        _importState.value = ImportState.Error("Import cancelled", recoverable = true)
                    }
                    else -> {
                        // ENQUEUED, BLOCKED - do nothing
                    }
                }
            }
            .launchIn(scope)
    }
    
    /**
     * Cancel ongoing import
     */
    fun cancelImport() {
        workManager.cancelUniqueWork(WORK_NAME)
        _importState.value = ImportState.Idle
    }
    
    /**
     * Reset import state
     */
    fun resetState() {
        _importState.value = ImportState.Idle
    }
    
    /**
     * Dispose controller and cancel coroutine scope to prevent memory leaks
     * Should be called from ViewModel.onCleared()
     */
    fun dispose() {
        scope.cancel()
        Timber.i("📥 ModelImportController disposed")
    }
}

/**
 * File validation result
 */
private data class FileValidation(
    val isValid: Boolean,
    val fileSize: Long = 0L,
    val errorMessage: String? = null
)
