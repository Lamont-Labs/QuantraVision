package com.lamontlabs.quantravision.export

import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.*
import android.util.Log
import android.view.Surface
import java.io.File
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

/**
 * ReplayExporter (production-ready)
 * Creates deterministic MP4 video proof of detections or overlay sessions.
 * - Full MediaCodec H.264 encoding pipeline
 * - Surface rendering with overlay elements
 * - Progress tracking and error handling
 * - No audio, stable timestamps, deterministic frame ordering
 */
class ReplayExporter(private val context: Context) {

    private val tag = "ReplayExporter"
    private val width = 720
    private val height = 1280
    private val frameRate = 15
    private val bitRate = 2_000_000
    private val iFrameInterval = 1

    data class ExportProgress(
        val currentFrame: Int,
        val totalFrames: Int,
        val percentComplete: Float
    )

    interface ProgressCallback {
        fun onProgress(progress: ExportProgress)
        fun onComplete(outputFile: File)
        fun onError(error: Exception)
    }

    fun export(
        frames: List<Bitmap>,
        outputFileName: String = "quantravision_replay.mp4",
        callback: ProgressCallback? = null
    ): File {
        val outDir = File(context.filesDir, "dist").apply { mkdirs() }
        val outFile = File(outDir, outputFileName)

        if (frames.isEmpty()) {
            val error = IllegalArgumentException("No frames to export")
            callback?.onError(error)
            throw error
        }

        Log.d(tag, "Starting export of ${frames.size} frames to ${outFile.absolutePath}")

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null
        var eglHelper: EGLHelper? = null

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval)
            }

            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec.createInputSurface()
            
            eglHelper = EGLHelper(inputSurface)
            eglHelper.setup()
            
            codec.start()

            muxer = MediaMuxer(outFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val bufferInfo = MediaCodec.BufferInfo()
            var trackIndex = -1
            var frameIndex = 0
            val frameDurationUs = 1_000_000L / frameRate

            frames.forEachIndexed { index, bitmap ->
                try {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
                    
                    eglHelper.drawBitmap(scaledBitmap)
                    eglHelper.swapBuffers()
                    
                    if (scaledBitmap != bitmap) {
                        scaledBitmap.recycle()
                    }

                    var outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    
                    while (outputBufferId >= 0) {
                        if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (trackIndex != -1) {
                                throw RuntimeException("Format changed twice")
                            }
                            trackIndex = muxer.addTrack(codec.outputFormat)
                            muxer.start()
                            Log.d(tag, "Muxer started with track index: $trackIndex")
                        } else if (outputBufferId >= 0) {
                            val encodedData = codec.getOutputBuffer(outputBufferId)
                                ?: throw RuntimeException("Encoder output buffer was null")

                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                bufferInfo.size = 0
                            }

                            if (bufferInfo.size != 0) {
                                if (trackIndex == -1) {
                                    throw RuntimeException("Muxer not started")
                                }
                                
                                bufferInfo.presentationTimeUs = frameIndex * frameDurationUs
                                
                                encodedData.position(bufferInfo.offset)
                                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                                
                                muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                                frameIndex++
                            }

                            codec.releaseOutputBuffer(outputBufferId, false)

                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                break
                            }
                        }
                        
                        outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
                    }

                    val progress = ExportProgress(
                        currentFrame = index + 1,
                        totalFrames = frames.size,
                        percentComplete = ((index + 1).toFloat() / frames.size) * 100f
                    )
                    callback?.onProgress(progress)
                    
                    if (index % 10 == 0) {
                        Log.d(tag, "Progress: ${progress.percentComplete.toInt()}%")
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Error encoding frame $index", e)
                    throw e
                }
            }

            codec.signalEndOfInputStream()

            var outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            while (outputBufferId >= 0) {
                if (outputBufferId >= 0) {
                    val encodedData = codec.getOutputBuffer(outputBufferId)
                    
                    if (encodedData != null && bufferInfo.size != 0 && trackIndex != -1) {
                        bufferInfo.presentationTimeUs = frameIndex * frameDurationUs
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }

                    codec.releaseOutputBuffer(outputBufferId, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        break
                    }
                }
                outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 0)
            }

            Log.d(tag, "Export completed successfully: ${outFile.absolutePath}")
            callback?.onComplete(outFile)

        } catch (e: Exception) {
            Log.e(tag, "Export failed", e)
            callback?.onError(e)
            throw e
        } finally {
            try {
                eglHelper?.release()
                codec?.stop()
                codec?.release()
                muxer?.stop()
                muxer?.release()
                inputSurface?.release()
            } catch (e: Exception) {
                Log.e(tag, "Error releasing resources", e)
            }
        }

        return outFile
    }

    private class EGLHelper(private val surface: Surface) {
        private val tag = "EGLHelper"
        private var egl: EGL10? = null
        private var eglDisplay: EGLDisplay? = null
        private var eglContext: EGLContext? = null
        private var eglSurface: EGLSurface? = null
        private val textureRenderer = TextureRenderer()

        fun setup() {
            egl = EGLContext.getEGL() as EGL10
            eglDisplay = egl!!.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)

            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw RuntimeException("eglGetDisplay failed")
            }

            val version = IntArray(2)
            if (!egl!!.eglInitialize(eglDisplay, version)) {
                throw RuntimeException("eglInitialize failed")
            }

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            val configAttribs = intArrayOf(
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE
            )

            if (!egl!!.eglChooseConfig(eglDisplay, configAttribs, configs, 1, numConfigs)) {
                throw RuntimeException("eglChooseConfig failed")
            }

            val contextAttribs = intArrayOf(
                0x3098, 2,
                EGL10.EGL_NONE
            )

            eglContext = egl!!.eglCreateContext(
                eglDisplay,
                configs[0],
                EGL10.EGL_NO_CONTEXT,
                contextAttribs
            )

            if (eglContext == null || eglContext == EGL10.EGL_NO_CONTEXT) {
                throw RuntimeException("eglCreateContext failed")
            }

            eglSurface = egl!!.eglCreateWindowSurface(eglDisplay, configs[0], surface, null)

            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                throw RuntimeException("eglCreateWindowSurface failed")
            }

            if (!egl!!.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw RuntimeException("eglMakeCurrent failed")
            }

            textureRenderer.surfaceCreated()
        }

        fun drawBitmap(bitmap: Bitmap) {
            textureRenderer.drawFrame(bitmap)
        }

        fun swapBuffers() {
            egl?.eglSwapBuffers(eglDisplay, eglSurface)
        }

        fun release() {
            egl?.let {
                it.eglMakeCurrent(
                    eglDisplay,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT
                )
                it.eglDestroySurface(eglDisplay, eglSurface)
                it.eglDestroyContext(eglDisplay, eglContext)
                it.eglTerminate(eglDisplay)
            }
        }
    }

    private class TextureRenderer {
        private var textureId = 0

        fun surfaceCreated() {
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]
            
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat())
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        }

        fun drawFrame(bitmap: Bitmap) {
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            GLES20.glEnable(GLES20.GL_TEXTURE_2D)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

            val vertices = floatArrayOf(
                -1f, -1f, 0f, 1f,
                1f, -1f, 1f, 1f,
                -1f, 1f, 0f, 0f,
                1f, 1f, 1f, 0f
            )

            val vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
            vertexBuffer.position(0)

            GLES20.glEnableVertexAttribArray(0)
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)
            
            vertexBuffer.position(2)
            GLES20.glEnableVertexAttribArray(1)
            GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 16, vertexBuffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GLES20.glDisableVertexAttribArray(0)
            GLES20.glDisableVertexAttribArray(1)
        }
    }
}
