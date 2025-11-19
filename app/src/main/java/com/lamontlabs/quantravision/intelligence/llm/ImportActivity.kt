package com.lamontlabs.quantravision.intelligence.llm

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lamontlabs.quantravision.overlay.OverlayService
import com.lamontlabs.quantravision.overlay.OverlayServiceGuard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Dedicated Activity for model import via Storage Access Framework.
 * 
 * For initial setup: Called from ModelProvisionOrchestrator before OverlayService starts
 * For later imports: Checks if scanner is running and requires user to stop it first
 * 
 * This prevents Android 13+ tap-jacking protection from killing OverlayService.
 */
class ImportActivity : AppCompatActivity() {
    
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        try {
            Timber.i("📥 ImportActivity: File picker returned, uri=$uri")
            
            if (uri == null) {
                Timber.w("📥 ImportActivity: No file selected")
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
                finish()
                return@registerForActivityResult
            }
            
            // CRITICAL: Persist URI permission before activity finishes
            // This prevents SecurityException when background worker tries to read file
            try {
                Timber.i("📥 ImportActivity: Taking persistable URI permission for $uri")
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Timber.i("📥 ImportActivity: Successfully persisted URI permission")
            } catch (e: Exception) {
                Timber.w(e, "📥 ImportActivity: Could not persist URI permission (some providers don't support it)")
                // Continue anyway - some file providers don't support persistence
            }
            
            // Import the model
            Timber.i("📥 ImportActivity: Starting import for URI: $uri")
            val controller = ModelImportController(this)
            
            lifecycleScope.launch {
                try {
                    controller.handleFileSelected(uri)
                    Timber.i("📥 ImportActivity: handleFileSelected completed")
                    
                    // Wait a moment for WorkManager to start
                    kotlinx.coroutines.delay(500)
                    
                    // Re-enable OverlayService after successful import
                    Timber.i("📥 ImportActivity: Re-enabling OverlayService")
                    OverlayServiceGuard.enable(this@ImportActivity)
                    
                    // Success - return to app
                    setResult(Activity.RESULT_OK)
                    finish()
                    
                } catch (e: Exception) {
                    Timber.e(e, "📥 ImportActivity: Error in handleFileSelected")
                    
                    // Re-enable service even on failure
                    OverlayServiceGuard.enable(this@ImportActivity)
                    
                    Toast.makeText(
                        this@ImportActivity,
                        "Import failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "📥 ImportActivity: CRASH in file picker callback")
            Toast.makeText(this, "Picker crashed: ${e.message}", Toast.LENGTH_LONG).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("📥 ImportActivity: onCreate")
        
        if (savedInstanceState == null) {
            // CRITICAL: Disable OverlayService at OS level before importing
            // This is the NUCLEAR option - completely prevents Android from starting the service
            Timber.i("📥 ImportActivity: Disabling OverlayService at OS level")
            OverlayServiceGuard.disable(this)
            
            // Also explicitly stop any running instance
            val serviceIntent = Intent(this, OverlayService::class.java)
            try {
                stopService(serviceIntent)
                Timber.i("📥 ImportActivity: Stopped OverlayService")
            } catch (e: Exception) {
                Timber.w(e, "📥 ImportActivity: Could not stop OverlayService")
            }
            
            // Launch file picker immediately
            Timber.i("📥 ImportActivity: Launching file picker (service disabled)")
            filePicker.launch(arrayOf("*/*"))
        }
    }
    
    private fun showScannerRunningDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Stop Scanner First")
            .setMessage("The pattern scanner must be stopped before importing the AI model.\n\nPlease:\n1. Go to the Scan tab\n2. Tap 'Stop Scanner'\n3. Return here and try again")
            .setPositiveButton("OK") { _, _ ->
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun isOverlayServiceRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return activityManager.getRunningServices(Int.MAX_VALUE).any { service ->
            service.service.className == OverlayService::class.java.name
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.i("📥 ImportActivity: onDestroy")
    }
}
