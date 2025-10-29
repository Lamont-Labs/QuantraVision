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
    private val framePeriodMs = (1000.0 / targetFps).toLong().coerceAtLeast(30)

    fun start(projection: MediaProjection, width: Int, height: Int, densityDpi: Int) {
        if (running.getAndSet(true)) return
        mediaProjection = projection

        imageReader = ImageReader.newInstance(width, height, ImageFormat.RGBA_8888, 2)
        val reader = imageReader ?: return
        val surface: Surface = reader.surface

        virtualDisplay = projection.createVirtualDisplay(
            "QuantraVisionVD",
            width, height, densityDpi,
            android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface, null, null
        )

        reader.setOnImageAvailableListener({ reader ->
            val now = System.currentTimeMillis()
            if (now - lastEmitMs < framePeriodMs) {
                // Drop frame deterministically to meet targetFps
                reader.acquireLatestImage()?.close()
                return@setOnImageAvailableListener
            }
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val bmp = image.toBitmap()
            image.close()
            if (bmp != null) {
                lastEmitMs = now
                scope.launch(Dispatchers.Default) { onFrame(bmp) }
            }
        }, android.os.Handler(android.os.Looper.getMainLooper()))
    }

    fun stop() {
        if (!running.getAndSet(false)) return
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { imageReader?.close() } catch (_: Exception) {}
        try { mediaProjection?.stop() } catch (_: Exception) {}
        virtualDisplay = null
        imageReader = null
        mediaProjection = null
    }

    private fun Image.toBitmap(): Bitmap? {
        if (format != ImageFormat.RGBA_8888) return null
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
        return Bitmap.createBitmap(bmp, 0, 0, width, height)
    }
}
