package com.lamontlabs.quantravision.intelligence.llm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.lamontlabs.quantravision.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Background worker for copying large model files (529MB)
 * 
 * Uses streaming copy with progress notifications to handle large files
 * without loading entire file into memory.
 * 
 * ## Workflow
 * 1. Read source file via ContentResolver (user's Downloads)
 * 2. Stream to temp file in cacheDir with 1MB chunks
 * 3. Show foreground notification with progress
 * 4. Validate file size after copy
 * 5. Atomically move from cache → files/llm_models/
 * 6. Clean up temp file
 * 
 * ## Progress Tracking
 * - Updates every 1MB for smooth UI progress bar
 * - Shows speed (MB/s) and time remaining
 * - Supports cancellation mid-copy
 */
class ImportModelWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val CHANNEL_ID = "model_import_channel"
        private const val NOTIFICATION_ID = 1001
        private const val CHUNK_SIZE = 1024 * 1024 // 1MB chunks
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get source URI from input data
            val sourceUriString = inputData.getString(ModelImportController.KEY_SOURCE_URI)
                ?: return@withContext Result.failure(
                    Data.Builder().putString("error", "Source URI missing").build()
                )
            
            val sourceUri = Uri.parse(sourceUriString)
            
            // Create notification channel
            createNotificationChannel()
            
            // Set foreground notification
            setForeground(createForegroundInfo(0))
            
            // Perform copy
            val result = copyModelFile(sourceUri)
            
            return@withContext result
            
        } catch (e: Exception) {
            Timber.e(e, "Import worker failed")
            Result.failure(
                Data.Builder().putString("error", e.message ?: "Unknown error").build()
            )
        }
    }
    
    /**
     * Copy model file with progress tracking
     */
    private suspend fun copyModelFile(sourceUri: Uri): Result {
        var tempFile: File? = null
        
        try {
            val modelManager = ModelManager(context)
            val modelDir = File(context.filesDir, "llm_models")
            modelDir.mkdirs()
            
            // Create temp file in cache
            tempFile = File(context.cacheDir, "model_temp_${System.currentTimeMillis()}.task")
            
            // Get source input stream
            val inputStream = context.contentResolver.openInputStream(sourceUri)
                ?: return Result.failure(
                    Data.Builder().putString("error", "Cannot open source file").build()
                )
            
            // Get file size for progress
            val totalBytes = getFileSize(sourceUri)
            var bytesCopied = 0L
            val startTime = System.currentTimeMillis()
            
            // Stream copy with progress
            inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(CHUNK_SIZE)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        // Check if cancelled
                        if (isStopped) {
                            Timber.i("Import cancelled by user")
                            tempFile?.delete()
                            return Result.failure(
                                Data.Builder().putString("error", "Import cancelled").build()
                            )
                        }
                        
                        // Write chunk
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        
                        // Update progress
                        val progress = ((bytesCopied.toFloat() / totalBytes) * 100).toInt()
                        setProgressAsync(
                            Data.Builder()
                                .putInt(ModelImportController.KEY_PROGRESS, progress)
                                .putLong(ModelImportController.KEY_TOTAL_BYTES, totalBytes)
                                .build()
                        )
                        
                        // Update notification
                        val speed = calculateSpeed(bytesCopied, startTime)
                        setForegroundAsync(createForegroundInfo(progress, speed))
                    }
                }
            }
            
            Timber.i("File copied: $bytesCopied bytes")
            
            // Validate copied file size
            if (tempFile.length() != totalBytes) {
                tempFile.delete()
                return Result.failure(
                    Data.Builder()
                        .putString("error", "File copy incomplete. Expected $totalBytes bytes, got ${tempFile.length()}")
                        .build()
                )
            }
            
            // Move from cache to final location
            val finalFile = File(modelDir, ModelConfig.MODEL_NAME)
            if (finalFile.exists()) {
                finalFile.delete() // Replace existing
            }
            
            val moved = tempFile.renameTo(finalFile)
            if (!moved) {
                // Fallback: copy then delete
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }
            
            Timber.i("Model imported successfully: ${finalFile.absolutePath}")
            
            // Notify ModelManager to refresh state and emit to observers
            withContext(Dispatchers.Main) {
                modelManager.onModelImported()
                Timber.i("Model import complete, ModelManager state refreshed")
            }
            
            return Result.success()
            
        } catch (e: Exception) {
            Timber.e(e, "Error copying model file")
            tempFile?.delete()
            return Result.failure(
                Data.Builder().putString("error", e.message ?: "Copy failed").build()
            )
        }
    }
    
    /**
     * Get file size from URI
     */
    private fun getFileSize(uri: Uri): Long {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex >= 0) {
                    return cursor.getLong(sizeIndex)
                }
            }
        }
        return 0L
    }
    
    /**
     * Calculate copy speed in MB/s
     */
    private fun calculateSpeed(bytesCopied: Long, startTime: Long): Float {
        val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000f
        if (elapsedSeconds <= 0) return 0f
        
        val mbCopied = bytesCopied / (1024f * 1024f)
        return mbCopied / elapsedSeconds
    }
    
    /**
     * Create notification channel (Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Model Import",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress when importing AI model"
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Create foreground notification info
     */
    private fun createForegroundInfo(progress: Int, speed: Float = 0f): ForegroundInfo {
        val title = "Importing AI Model"
        val text = if (progress > 0) {
            "Progress: $progress% (${String.format("%.1f", speed)} MB/s)"
        } else {
            "Starting import..."
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .build()
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }
}
