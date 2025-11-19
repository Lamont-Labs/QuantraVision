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
 * Direct file path import activity - BYPASSES Android's buggy file picker.
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("📥 ImportActivity: onCreate (direct path mode)")
        
        setContent {
            QuantraVisionTheme {
                DirectFilePathImportScreen(
                    onImportComplete = {
                        Timber.i("📥 Import complete, re-enabling OverlayService")
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
                    onImport = { filePath ->
                        pendingFilePath = filePath
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
                    "Please grant 'All files access' permission to import the model file",
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
        pendingFilePath?.let { importFromPath(it) }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndProceed()
        } else {
            Toast.makeText(
                this,
                "Storage permission required to access the model file",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun importFromPath(filePath: String) {
        lifecycleScope.launch {
            try {
                Timber.i("📥 Importing from path: $filePath")
                
                withContext(Dispatchers.IO) {
                    val sourceFile = File(filePath)
                    
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
                    
                    // Copy file directly to internal storage
                    val destFile = File(this@ImportActivity.filesDir, "gemma-model.task")
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ImportActivity,
                            "Importing ${sourceFile.length() / 1_000_000}MB model file...",
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
                    
                    // Update ModelManager state
                    val modelManager = ModelManager(this@ImportActivity)
                    modelManager.onModelDownloaded()
                }
                
                // Success
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ImportActivity,
                        "✓ Model imported successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    OverlayServiceGuard.enable(this@ImportActivity)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                
            } catch (e: Exception) {
                Timber.e(e, "📥 Import failed")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ImportActivity,
                        "Import failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    OverlayServiceGuard.enable(this@ImportActivity)
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }
}

@Composable
private fun DirectFilePathImportScreen(
    onImportComplete: () -> Unit,
    onCancel: () -> Unit,
    onImport: (String) -> Unit
) {
    var filePath by remember { 
        mutableStateOf("/storage/emulated/0/Download/gemma3-1b-it-int4.task") 
    }
    var isImporting by remember { mutableStateOf(false) }
    
    StaticBrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📁",
                style = AppTypography.headlineLarge.copy(
                    fontSize = AppTypography.headlineLarge.fontSize * 2
                )
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            Text(
                text = "Enter File Path",
                style = AppTypography.headlineLarge,
                color = AppColors.NeonCyan,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    Text(
                        text = "Type the full path to your downloaded model file:",
                        style = AppTypography.bodyMedium,
                        color = AppColors.MetallicSilver,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
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
                        text = "Common location:\n/storage/emulated/0/Download/gemma3-1b-it-int4.task",
                        style = AppTypography.bodySmall,
                        color = AppColors.MetallicGold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    
                    Text(
                        text = "Note: You'll be asked to grant 'All files access' permission.",
                        style = AppTypography.bodySmall,
                        color = AppColors.MetallicSilver.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            MetallicButton(
                onClick = {
                    isImporting = true
                    onImport(filePath)
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
                    Text("Import Model")
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            TextButton(onClick = onCancel) {
                Text(
                    "Cancel",
                    color = AppColors.MetallicSilver
                )
            }
        }
    }
}
