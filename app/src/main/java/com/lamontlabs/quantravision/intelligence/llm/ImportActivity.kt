package com.lamontlabs.quantravision.intelligence.llm

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.lamontlabs.quantravision.overlay.OverlayServiceGuard
import com.lamontlabs.quantravision.ui.MetallicButton
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.theme.QuantraVisionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Direct file path import activity - BYPASSES Android's buggy file picker.
 * 
 * Android's Storage Access Framework has a known bug with files over 500MB that causes
 * crashes on Android 14. This activity lets users type the file path directly, completely
 * avoiding the file picker and all its tap-jacking/memory issues.
 * 
 * Common path: /storage/emulated/0/Download/gemma3-1b-it-int4.task
 */
class ImportActivity : ComponentActivity() {
    
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
                        importFromPath(filePath)
                    }
                )
            }
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
                                "File not found: $filePath",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        return@withContext
                    }
                    
                    // Convert to Uri and use existing import system
                    val uri = Uri.fromFile(sourceFile)
                    val controller = ModelImportController(this@ImportActivity)
                    controller.handleFileSelected(uri)
                }
                
                // Wait for background copy to start
                kotlinx.coroutines.delay(500)
                
                // Re-enable service and finish
                withContext(Dispatchers.Main) {
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
