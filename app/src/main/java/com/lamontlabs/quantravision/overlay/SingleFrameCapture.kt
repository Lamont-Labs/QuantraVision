package com.lamontlabs.quantravision.overlay

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.media.Image
import android.media.ImageReader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
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
     * 
     * @throws kotlinx.coroutines.TimeoutCancellationException if no frame arrives within 2.5 seconds
     */
    suspend fun captureFrame(imageReader: ImageReader): Bitmap = captureMutex.withLock {
        Timber.i("📸 Acquiring frame from persistent VirtualDisplay (2.5s timeout)...")
        
        // Samsung One UI fix: Try to prime the ImageReader by checking for existing frame
        try {
            val existingImage = imageReader.acquireLatestImage()
            if (existingImage != null) {
                Timber.d("✓ Found existing frame in ImageReader, using it immediately")
                val bitmap = existingImage.toBitmap()
                existingImage.close()
                if (bitmap != null && isValidBitmap(bitmap)) {
                    Timber.i("✅ Primed frame captured: ${bitmap.width}x${bitmap.height}")
                    return@withLock bitmap
                } else {
                    Timber.w("Existing frame was invalid, waiting for new frame...")
                    bitmap?.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.d("No existing frame available, waiting for new one: ${e.message}")
        }
        
        // Wait for new frame with timeout (Samsung One UI quirk protection)
        return withTimeout(2500) {
            suspendCancellableCoroutine { continuation ->
            var imageAvailableListener: ImageReader.OnImageAvailableListener? = null
            
            try {
                imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
                    try {
                        Timber.d("📥 ImageReader fired - frame available!")
                        val image = reader.acquireLatestImage()
                        if (image != null) {
                            val bitmap = image.toBitmap()
                            image.close()
                            
                            if (bitmap != null) {
                                if (!isValidBitmap(bitmap)) {
                                    Timber.e("❌ Captured bitmap is invalid (empty or all black)")
                                    bitmap.recycle()
                                    reader.setOnImageAvailableListener(null, null)
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            RuntimeException("Captured frame is empty or invalid")
                                        )
                                    }
                                    return@OnImageAvailableListener
                                }
                                
                                Timber.i("✅ Valid frame captured: ${bitmap.width}x${bitmap.height}")
                                
                                // Clean up listener
                                reader.setOnImageAvailableListener(null, null)
                                
                                if (continuation.isActive) {
                                    continuation.resume(bitmap)
                                }
                            } else {
                                Timber.e("❌ Failed to convert image to bitmap")
                                reader.setOnImageAvailableListener(null, null)
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        RuntimeException("Failed to convert image to bitmap")
                                    )
                                }
                            }
                        } else {
                            Timber.w("⏳ No image available yet, waiting...")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error in image available listener")
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
                    Timber.d("⏹️ Capture cancelled, removing listener")
                    imageReader.setOnImageAvailableListener(null, null)
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Error during frame capture setup")
                imageReader.setOnImageAvailableListener(null, null)
                continuation.resumeWithException(e)
            }
            }
        }
    }
    
    /**
     * Validates that a bitmap contains actual content (not empty/all black).
     * Samsung One UI quirk: Sometimes VirtualDisplay delivers empty frames.
     */
    private fun isValidBitmap(bitmap: Bitmap): Boolean {
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            Timber.w("Invalid bitmap dimensions: ${bitmap.width}x${bitmap.height}")
            return false
        }
        
        // Sample pixels to detect all-black frames
        // Check center pixel and 4 corners
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val pixels = intArrayOf(
            bitmap.getPixel(0, 0),
            bitmap.getPixel(bitmap.width - 1, 0),
            bitmap.getPixel(0, bitmap.height - 1),
            bitmap.getPixel(bitmap.width - 1, bitmap.height - 1),
            bitmap.getPixel(centerX, centerY)
        )
        
        // If all sampled pixels are black (or very dark), likely invalid
        val allBlack = pixels.all { pixel ->
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            r < 10 && g < 10 && b < 10
        }
        
        if (allBlack) {
            Timber.w("Bitmap appears to be all black/empty")
            return false
        }
        
        return true
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
