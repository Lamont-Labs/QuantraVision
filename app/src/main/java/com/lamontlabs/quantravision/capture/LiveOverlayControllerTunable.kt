package com.lamontlabs.quantravision.capture

/**
 * LiveOverlayControllerTunable
 * Allows runtime FPS target adjustment for MediaProjection path.
 */
object LiveOverlayControllerTunable {
    @Volatile private var fps: Int = 12
    fun setTargetFps(v: Int) { fps = v.coerceIn(4, 30) }
    fun getTargetFps(): Int = fps
}
