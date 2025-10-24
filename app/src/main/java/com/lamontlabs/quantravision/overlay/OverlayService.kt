package com.lamontlabs.quantravision.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class OverlayService : Service() {
  private val channelId = "overlay_service"

  override fun onCreate() {
    super.onCreate()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(channelId, "QuantraVision Overlay", NotificationManager.IMPORTANCE_MIN)
      getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    val notif: Notification = Notification.Builder(this, channelId)
      .setContentTitle("QuantraVision Overlay")
      .setContentText("Running")
      .setSmallIcon(android.R.drawable.presence_online)
      .build()
    startForeground(7, notif)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
  override fun onBind(intent: Intent?): IBinder? = null
}
