package com.lamontlabs.quantravision.overlay

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.view.Surface
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class SingleFrameCapture {
    
    private val captureMutex = Mutex()
    
    suspend fun captureFrame(
        projection: MediaProjection,
        width: Int,
        height: Int,
        densityDpi: Int
    ): Bitmap = captureMutex.withLock {
        Timber.d("Starting single frame capture: ${width}x${height} @ ${densityDpi}dpi")
        
        return suspendCancellableCoroutine { continuation ->
            var imageReader: ImageReader? = null
            var virtualDisplay: VirtualDisplay? = null
            
            try {
                imageReader = ImageReader.newInstance(
                    width,
                    height,
                    PixelFormat.RGBA_8888,
                    2
                )
                
                val reader = imageReader
                val surface: Surface = reader.surface
                
                imageReader.setOnImageAvailableListener({ imgReader ->
                    try {
                        val image = imgReader.acquireLatestImage()
                        if (image != null) {
                            val bitmap = image.toBitmap()
                            image.close()
                            
                            if (bitmap != null) {
                                Timber.d("Frame captured successfully: ${bitmap.width}x${bitmap.height}")
                                
                                try {
                                    virtualDisplay?.release()
                                } catch (e: Exception) {
                                    Timber.w(e, "Error releasing virtual display")
                                }
                                
                                try {
                                    imgReader.close()
                                } catch (e: Exception) {
                                    Timber.w(e, "Error closing image reader")
                                }
                                
                                continuation.resume(bitmap)
                            } else {
                                Timber.e("Failed to convert image to bitmap")
                                cleanup(virtualDisplay, imgReader)
                                continuation.resumeWithException(
                                    RuntimeException("Failed to convert image to bitmap")
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error in image available listener")
                        cleanup(virtualDisplay, imgReader)
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                }, android.os.Handler(android.os.Looper.getMainLooper()))
                
                virtualDisplay = projection.createVirtualDisplay(
                    "QuantraVision_SingleFrame",
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface,
                    null,
                    null
                )
                
                if (virtualDisplay == null) {
                    throw RuntimeException("Failed to create virtual display")
                }
                
                continuation.invokeOnCancellation {
                    Timber.d("Capture cancelled, cleaning up resources")
                    cleanup(virtualDisplay, imageReader)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error during frame capture setup")
                cleanup(virtualDisplay, imageReader)
                continuation.resumeWithException(e)
            }
        }
    }
    
    private fun cleanup(virtualDisplay: VirtualDisplay?, imageReader: ImageReader?) {
        try {
            virtualDisplay?.release()
        } catch (e: Exception) {
            Timber.w(e, "Error releasing virtual display during cleanup")
        }
        
        try {
            imageReader?.close()
        } catch (e: Exception) {
            Timber.w(e, "Error closing image reader during cleanup")
        }
    }
    
    private fun Image.toBitmap(): Bitmap? {
        if (format != PixelFormat.RGBA_8888) {
            Timber.e("Unexpected image format: $format")
            return null
        }
        
        val plane = planes.firstOrNull() ?: return null
        val buffer: ByteBuffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width
        
        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
        bitmap.recycle()
        
        return cropped
    }
}
