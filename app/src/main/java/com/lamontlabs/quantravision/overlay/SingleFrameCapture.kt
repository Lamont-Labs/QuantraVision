package com.lamontlabs.quantravision.overlay

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import timber.log.Timber
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android 14-compatible single frame capture.
 * Reuses persistent VirtualDisplay/ImageReader instead of creating new ones.
 */
class SingleFrameCapture {
    
    private val captureMutex = Mutex()
    
    /**
     * Captures a single frame from the persistent ImageReader.
     * Android 14 fix: No longer creates VirtualDisplay per frame.
     */
    suspend fun captureFrame(imageReader: ImageReader): Bitmap = captureMutex.withLock {
        Timber.d("Acquiring latest image from persistent VirtualDisplay...")
        
        return suspendCancellableCoroutine { continuation ->
            var imageAvailableListener: ImageReader.OnImageAvailableListener? = null
            
            try {
                imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
                    try {
                        val image = reader.acquireLatestImage()
                        if (image != null) {
                            val bitmap = image.toBitmap()
                            image.close()
                            
                            if (bitmap != null) {
                                Timber.d("Frame captured successfully: ${bitmap.width}x${bitmap.height}")
                                
                                // Clean up listener
                                reader.setOnImageAvailableListener(null, null)
                                
                                if (continuation.isActive) {
                                    continuation.resume(bitmap)
                                }
                            } else {
                                Timber.e("Failed to convert image to bitmap")
                                reader.setOnImageAvailableListener(null, null)
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        RuntimeException("Failed to convert image to bitmap")
                                    )
                                }
                            }
                        } else {
                            Timber.w("No image available yet, waiting...")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error in image available listener")
                        reader.setOnImageAvailableListener(null, null)
                        if (continuation.isActive) {
                            continuation.resumeWithException(e)
                        }
                    }
                }
                
                imageReader.setOnImageAvailableListener(
                    imageAvailableListener,
                    android.os.Handler(android.os.Looper.getMainLooper())
                )
                
                continuation.invokeOnCancellation {
                    Timber.d("Capture cancelled, removing listener")
                    imageReader.setOnImageAvailableListener(null, null)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "Error during frame capture setup")
                imageReader.setOnImageAvailableListener(null, null)
                continuation.resumeWithException(e)
            }
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
