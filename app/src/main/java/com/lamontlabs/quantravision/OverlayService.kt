package com.lamontlabs.quantravision

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private var scope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)
        startForegroundService()
        startDetectionLoop()
    }

    private fun startForegroundService() {
        val channelId = "QuantraVisionOverlay"
        val channel = NotificationChannel(channelId, "Overlay", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("QuantraVision Overlay")
            .setContentText("Running detection service")
            .setSmallIcon(R.drawable.ic_overlay_marker)
            .build()
        startForeground(1, notification)
    }

    private fun startDetectionLoop() {
        scope.launch {
            val detector = PatternDetector(applicationContext)
            while (true) {
                detector.scanStaticAssets()
                delay(3000)
            }
        }
    }

    override fun onDestroy() {
        windowManager.removeView(overlayView)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
