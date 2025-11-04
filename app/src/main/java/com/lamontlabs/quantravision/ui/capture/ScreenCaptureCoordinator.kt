package com.lamontlabs.quantravision.ui.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.lamontlabs.quantravision.overlay.OverlayService

@Composable
fun rememberScreenCaptureCoordinator(
    onSuccess: () -> Unit = {},
    onDenied: () -> Unit = {}
): ScreenCaptureCoordinator {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val serviceIntent = Intent(context, OverlayService::class.java).apply {
                putExtra(OverlayService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(OverlayService.EXTRA_RESULT_DATA, result.data)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            
            Toast.makeText(
                context,
                "Screen capture started! Open your trading app to see real-time pattern detection",
                Toast.LENGTH_LONG
            ).show()
            
            onSuccess()
        } else {
            Toast.makeText(
                context,
                "Screen capture permission denied. Pattern detection requires this permission.",
                Toast.LENGTH_LONG
            ).show()
            
            onDenied()
        }
    }
    
    return remember {
        ScreenCaptureCoordinator(context, launcher)
    }
}

class ScreenCaptureCoordinator(
    private val context: Context,
    private val launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    fun requestScreenCapture() {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        
        if (mediaProjectionManager == null) {
            Toast.makeText(
                context,
                "MediaProjection not supported on this device",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        launcher.launch(captureIntent)
    }
}
