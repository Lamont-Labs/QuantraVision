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
 * Immediately launches file picker and finishes when done.
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
            // First creation - launch picker immediately
            Timber.i("📥 ImportActivity: Launching file picker")
            filePicker.launch(arrayOf("*/*"))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Timber.i("📥 ImportActivity: onDestroy")
    }
}
