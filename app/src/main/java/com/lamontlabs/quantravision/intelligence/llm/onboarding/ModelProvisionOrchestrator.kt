package com.lamontlabs.quantravision.intelligence.llm.onboarding

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lamontlabs.quantravision.intelligence.llm.ImportActivity
import com.lamontlabs.quantravision.intelligence.llm.ModelManager
import com.lamontlabs.quantravision.intelligence.llm.ModelState
import com.lamontlabs.quantravision.ui.theme.AppColors
import com.lamontlabs.quantravision.ui.theme.AppSpacing
import com.lamontlabs.quantravision.ui.theme.AppTypography
import com.lamontlabs.quantravision.ui.MetallicCard
import com.lamontlabs.quantravision.ui.MetallicButton
import com.lamontlabs.quantravision.ui.NeonText
import com.lamontlabs.quantravision.ui.StaticBrandBackground
import timber.log.Timber

/**
 * Model Provision Orchestrator - Gates app startup until AI model is imported
 * 
 * Similar to PermissionOrchestrator, this composable blocks the app from starting
 * until the Gemma AI model is successfully imported. This COMPLETELY AVOIDS the
 * overlay/tap-jacking issue because ImportActivity runs BEFORE OverlayService starts.
 * 
 * Flow:
 * 1. Check if model is downloaded
 * 2. If not, show import screen with instructions
 * 3. User taps "Import Model" → launches ImportActivity
 * 4. Monitor ModelManager.modelStateFlow for completion
 * 5. Once downloaded, call onModelReady()
 * 6. App continues to main screen
 */
@Composable
fun ModelProvisionOrchestrator(
    onModelReady: () -> Unit
) {
    val context = LocalContext.current
    val modelManager = remember { ModelManager(context) }
    
    // Observe model state
    val modelState by modelManager.modelStateFlow.collectAsStateWithLifecycle()
    
    // Track if we're currently importing
    var isImporting by remember { mutableStateOf(false) }
    
    // Import activity launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.i("🧠 Import activity result: ${result.resultCode}")
        isImporting = false
        
        // Check model state again after import
        // ModelManager.modelStateFlow will automatically update if import succeeded
    }
    
    // Check if model is ready
    LaunchedEffect(modelState) {
        Timber.i("🧠 ModelProvisionOrchestrator: modelState = $modelState")
        if (modelState == ModelState.Downloaded) {
            Timber.i("🧠 Model ready! Proceeding to main app")
            onModelReady()
        }
    }
    
    // Show import screen if model not ready
    when (modelState) {
        ModelState.NotDownloaded, is ModelState.Error -> {
            ModelImportScreen(
                isImporting = isImporting,
                errorMessage = (modelState as? ModelState.Error)?.error,
                onImportClick = {
                    Timber.i("🧠 Launching ImportActivity from ModelProvisionOrchestrator")
                    isImporting = true
                    val intent = Intent(context, ImportActivity::class.java)
                    importLauncher.launch(intent)
                }
            )
        }
        ModelState.Downloaded -> {
            // Model ready - LaunchedEffect will trigger onModelReady()
        }
        is ModelState.Downloading -> {
            // Shouldn't happen with manual import, but show loading if it does
            LoadingScreen()
        }
    }
}

@Composable
private fun ModelImportScreen(
    isImporting: Boolean,
    errorMessage: String?,
    onImportClick: () -> Unit
) {
    StaticBrandBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Text(
                text = "🧠",
                style = AppTypography.headlineLarge.copy(
                    fontSize = AppTypography.headlineLarge.fontSize * 2
                )
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            // Title
            NeonText(
                text = "AI Model Required",
                style = AppTypography.headlineLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            // Description
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(AppSpacing.lg)) {
                    Text(
                        text = "QuantraVision uses the Gemma 3 1B AI model (529MB) for intelligent pattern explanations.",
                        style = AppTypography.bodyLarge,
                        color = AppColors.MetallicSilver,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    
                    Text(
                        text = "To import your AI model:",
                        style = AppTypography.bodyMedium.copy(
                            color = AppColors.OnBackground
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    
                    Text(
                        text = "1. Download gemma3-1b-it-int4.task (555MB) from HuggingFace to your phone\n\n" +
                               "2. Tap 'Import Model' below\n\n" +
                               "3. Select the downloaded file\n\n" +
                               "4. Wait for import to complete (~30 seconds)",
                        style = AppTypography.bodyMedium,
                        color = AppColors.MetallicSilver
                    )
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(AppSpacing.lg))
                        Text(
                            text = "⚠️ Previous import failed: $errorMessage",
                            style = AppTypography.bodySmall,
                            color = AppColors.Error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            
            // Import button
            MetallicButton(
                onClick = onImportClick,
                enabled = !isImporting,
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
                    Text("Import Model from Phone")
                }
            }
            
            Spacer(modifier = Modifier.height(AppSpacing.md))
            
            Text(
                text = "This is a one-time setup. The model works 100% offline once imported.",
                style = AppTypography.bodySmall,
                color = AppColors.MetallicSilver,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    StaticBrandBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                CircularProgressIndicator(
                    color = AppColors.NeonCyan,
                    strokeWidth = 3.dp
                )
                Text(
                    text = "Preparing AI model...",
                    style = AppTypography.bodyLarge,
                    color = AppColors.MetallicSilver
                )
            }
        }
    }
}
