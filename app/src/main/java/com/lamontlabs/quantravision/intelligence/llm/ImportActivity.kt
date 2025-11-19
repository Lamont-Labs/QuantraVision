package com.lamontlabs.quantravision.intelligence.llm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Dedicated Activity for model import via Storage Access Framework.
 * 
 * Runs outside Compose to guarantee stable ActivityResult lifecycle.
 * Stops OverlayService before launching picker to prevent Android 13+ tap-jacking protection.
 */
class ImportActivity : AppCompatActivity() {
    
    companion object {
        const val ACTION_SUSPEND_OVERLAY = "com.lamontlabs.quantravision.SUSPEND_OVERLAY"
        const val ACTION_RESUME_OVERLAY = "com.lamontlabs.quantravision.RESUME_OVERLAY"
    }
    
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        try {
            Timber.i("📥 ImportActivity: File picker returned, uri=$uri")
            
            if (uri == null) {
                Timber.w("📥 ImportActivity: No file selected")
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
                resumeOverlay()
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
                    resumeOverlay()
                    setResult(Activity.RESULT_OK)
                    finish()
                    
                } catch (e: Exception) {
                    Timber.e(e, "📥 ImportActivity: Error in handleFileSelected")
                    Toast.makeText(
                        this@ImportActivity,
                        "Import failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    resumeOverlay()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "📥 ImportActivity: CRASH in file picker callback")
            Toast.makeText(this, "Picker crashed: ${e.message}", Toast.LENGTH_LONG).show()
            resumeOverlay()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("📥 ImportActivity: onCreate")
        
        if (savedInstanceState == null) {
            // Suspend overlay to prevent Android tap-jacking protection
            suspendOverlay()
            
            // Launch picker after brief delay
            lifecycleScope.launch {
                kotlinx.coroutines.delay(100)
                Timber.i("📥 ImportActivity: Launching file picker")
                filePicker.launch(arrayOf("*/*"))
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.i("📥 ImportActivity: onDestroy")
    }
    
    private fun suspendOverlay() {
        Timber.i("📥 ImportActivity: Suspending OverlayService")
        sendBroadcast(Intent(ACTION_SUSPEND_OVERLAY))
    }
    
    private fun resumeOverlay() {
        Timber.i("📥 ImportActivity: Resuming OverlayService")
        sendBroadcast(Intent(ACTION_RESUME_OVERLAY))
    }
}
