package com.lamontlabs.quantravision.capture

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.view.Surface
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * LiveOverlayController
 * Deterministic screen-capture pipeline for real-time detection + rendering.
 * - No network, no randomness.
 * - Throttled to targetFps to preserve battery.
 * - Emits ARGB_8888 Bitmaps via callback for downstream detection.
 *
 * Usage:
 *  val ctrl = LiveOverlayController(scope) { bmp -> onFrame(bmp) }
 *  ctrl.start(projection, width, height, densityDpi)
 *  ...
 *  ctrl.stop()
 */
class LiveOverlayController(
    private val scope: CoroutineScope,
    private val onFrame: (Bitmap) -> Unit,
    private val targetFps: Int = 12
) {

    private var imageReader: ImageReader? = null
    private var virtualDisplay: android.hardware.display.VirtualDisplay? = null
    private var mediaProjection: MediaProjection? = null
    private val running = AtomicBoolean(false)
    private var lastEmitMs = 0L
    private var lastPolicyCheckMs = 0L
    private var framePeriodMs = (1000.0 / targetFps).toLong().coerceAtLeast(30)
    
    private fun getEffectiveFps(): Int {
        return LiveOverlayControllerTunable.getTargetFps()
    }

    fun start(projection: MediaProjection, width: Int, height: Int, densityDpi: Int) {
        if (running.getAndSet(true)) return
        mediaProjection = projection

        try {
            imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2)
            val reader = imageReader ?: run {
                running.set(false)
                throw IllegalStateException("Failed to create ImageReader")
            }
            val surface: Surface = reader.surface

            virtualDisplay = projection.createVirtualDisplay(
                "QuantraVisionVD",
                width, height, densityDpi,
                android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null
            )
            
            if (virtualDisplay == null) {
                android.util.Log.e("LiveOverlayController", "CRITICAL: createVirtualDisplay returned null")
                running.set(false)
                try { imageReader?.close() } catch (_: Exception) {}
                imageReader = null
                throw RuntimeException("Failed to create VirtualDisplay. Please restart the overlay service.")
            }
            
            android.util.Log.i("LiveOverlayController", "VirtualDisplay created successfully: ${width}x${height}")
        } catch (e: SecurityException) {
            android.util.Log.e("LiveOverlayController", "CRITICAL: SecurityException - MediaProjection permission denied", e)
            running.set(false)
            try { imageReader?.close() } catch (_: Exception) {}
            imageReader = null
            throw RuntimeException("Screen capture permission denied. Please grant permission and restart.", e)
        } catch (e: IllegalStateException) {
            android.util.Log.e("LiveOverlayController", "CRITICAL: IllegalStateException - MediaProjection in invalid state", e)
            running.set(false)
            try { imageReader?.close() } catch (_: Exception) {}
            imageReader = null
            recoverFromFailedStart()
            throw RuntimeException("MediaProjection stopped or in invalid state. Please restart scanner.", e)
        } catch (e: Exception) {
            android.util.Log.e("LiveOverlayController", "CRITICAL: Failed to create virtual display (MediaProjection may have been stopped)", e)
            // Clean up partially initialized resources
            running.set(false)
            try { imageReader?.close() } catch (_: Exception) {}
            imageReader = null
            throw RuntimeException("Failed to start screen capture. Please restart the overlay service.", e)
        }

        imageReader!!.setOnImageAvailableListener({ imgReader ->
            try {
                val now = System.currentTimeMillis()
                
                if (now - lastPolicyCheckMs > 5000) {
                    framePeriodMs = (1000.0 / getEffectiveFps()).toLong().coerceAtLeast(30)
                    lastPolicyCheckMs = now
                }
                
                if (now - lastEmitMs < framePeriodMs) {
                    // Drop frame deterministically to meet targetFps
                    try {
                        imgReader.acquireLatestImage()?.close()
                    } catch (e: Exception) {
                        android.util.Log.w("LiveOverlayController", "Error dropping frame: ${e.message}")
                    }
                    return@setOnImageAvailableListener
                }
                
                val image = imgReader.acquireLatestImage()
                if (image == null) {
                    android.util.Log.v("LiveOverlayController", "No image available from ImageReader")
                    return@setOnImageAvailableListener
                }
                
                val bmp = try {
                    image.toBitmap()
                } catch (e: Exception) {
                    android.util.Log.e("LiveOverlayController", "Error converting image to bitmap", e)
                    null
                } finally {
                    try { image.close() } catch (_: Exception) {}
                }
                
                if (bmp != null) {
                    lastEmitMs = now
                    scope.launch(Dispatchers.Default) { 
                        try {
                            onFrame(bmp)
                        } catch (e: Exception) {
                            android.util.Log.e("LiveOverlayController", "Error in onFrame callback", e)
                        }
                    }
                } else {
                    android.util.Log.w("LiveOverlayController", "Failed to convert image to bitmap")
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveOverlayController", "Critical error in image available listener", e)
            }
        }, android.os.Handler(android.os.Looper.getMainLooper()))
    }

    fun stop() {
        if (!running.getAndSet(false)) return
        
        android.util.Log.i("LiveOverlayController", "Stopping capture pipeline...")
        
        try {
            virtualDisplay?.release()
            android.util.Log.d("LiveOverlayController", "VirtualDisplay released")
        } catch (e: android.os.DeadObjectException) {
            android.util.Log.w("LiveOverlayController", "VirtualDisplay already dead")
        } catch (e: IllegalStateException) {
            android.util.Log.w("LiveOverlayController", "VirtualDisplay in illegal state during release")
        } catch (e: Exception) {
            android.util.Log.e("LiveOverlayController", "Error releasing VirtualDisplay", e)
        }
        virtualDisplay = null
        
        try {
            imageReader?.close()
            android.util.Log.d("LiveOverlayController", "ImageReader closed")
        } catch (e: Exception) {
            android.util.Log.e("LiveOverlayController", "Error closing ImageReader", e)
        }
        imageReader = null
        
        try {
            mediaProjection?.stop()
            android.util.Log.d("LiveOverlayController", "MediaProjection stopped")
        } catch (e: android.os.DeadObjectException) {
            android.util.Log.w("LiveOverlayController", "MediaProjection already dead")
        } catch (e: IllegalStateException) {
            android.util.Log.w("LiveOverlayController", "MediaProjection in illegal state during stop")
        } catch (e: Exception) {
            android.util.Log.e("LiveOverlayController", "Error stopping MediaProjection", e)
        }
        mediaProjection = null
        
        android.util.Log.i("LiveOverlayController", "Capture pipeline stopped successfully")
    }
    
    private fun recoverFromFailedStart() {
        android.util.Log.w("LiveOverlayController", "Attempting recovery from failed start...")
        try {
            virtualDisplay?.release()
        } catch (e: Exception) {
            android.util.Log.d("LiveOverlayController", "Error during recovery VirtualDisplay release: ${e.message}")
        }
        virtualDisplay = null
        
        try {
            imageReader?.close()
        } catch (e: Exception) {
            android.util.Log.d("LiveOverlayController", "Error during recovery ImageReader close: ${e.message}")
        }
        imageReader = null
        
        android.util.Log.i("LiveOverlayController", "Recovery complete - resources cleaned up")
    }

    private fun Image.toBitmap(): Bitmap? {
        if (format != android.graphics.PixelFormat.RGBA_8888) return null
        val plane = planes.firstOrNull() ?: return null
        val buffer: ByteBuffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        val bmp = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bmp.copyPixelsFromBuffer(buffer)
        // Crop away row padding deterministically
        val cropped = Bitmap.createBitmap(bmp, 0, 0, width, height)
        bmp.recycle() // Release the padded bitmap
        return cropped
    }
}
