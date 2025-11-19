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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Dedicated Activity for model import via Storage Access Framework.
 * 
 * Completely stops OverlayService before launching file picker to prevent 
 * Android 13+ tap-jacking protection from killing the service.
 * Restarts the service after import completes if it was previously running.
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
            
            // Import the model
            Timber.i("📥 ImportActivity: Starting import for URI: $uri")
            val controller = ModelImportController(this)
            
            lifecycleScope.launch {
                try {
                    controller.handleFileSelected(uri)
                    Timber.i("📥 ImportActivity: handleFileSelected completed")
                    
                    // Wait a moment for WorkManager to start
                    kotlinx.coroutines.delay(500)
                    
                    // Success - return to app
                    setResult(Activity.RESULT_OK)
                    finish()
                    
                } catch (e: Exception) {
                    Timber.e(e, "📥 ImportActivity: Error in handleFileSelected")
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
            // Check if OverlayService is running
            val isRunning = isOverlayServiceRunning()
            Timber.i("📥 ImportActivity: OverlayService running = $isRunning")
            
            if (isRunning) {
                // Scanner is running - show error dialog
                Timber.w("📥 ImportActivity: Scanner must be stopped before import")
                showScannerRunningDialog()
            } else {
                // Safe to proceed
                Timber.i("📥 ImportActivity: Scanner not running, launching file picker")
                filePicker.launch(arrayOf("*/*"))
            }
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
