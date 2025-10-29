package com.lamontlabs.quantravision.roi

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

/**
 * Deterministic Region-of-Interest proposal engine.
 * Filters large charts down to high-value areas before template matching.
 *  - Uses gradient magnitude and contour clustering.
 *  - No randomness; fixed thresholds and deterministic sorting.
 */
object ROIProposer {

    data class ROI(val rect: Rect, val score: Double)

    fun propose(inputGray: Mat, maxRois: Int = 8): List<ROI> {
        val gradX = Mat()
        val gradY = Mat()
        val mag = Mat()
        try {
            Imgproc.Sobel(inputGray, gradX, CvType.CV_32F, 1, 0)
            Imgproc.Sobel(inputGray, gradY, CvType.CV_32F, 0, 1)
            Core.magnitude(gradX, gradY, mag)
            Core.normalize(mag, mag, 0.0, 255.0, Core.NORM_MINMAX)
            mag.convertTo(mag, CvType.CV_8U)

            val contours = mutableListOf<MatOfPoint>()
            Imgproc.findContours(mag, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
            val rois = contours.mapNotNull {
                val r = Imgproc.boundingRect(it)
                if (r.width < 20 || r.height < 20) return@mapNotNull null
                val area = r.width * r.height.toDouble()
                ROI(r, area)
            }.sortedByDescending { it.score }
            return rois.take(maxRois)
        } finally {
            gradX.release()
            gradY.release()
            mag.release()
        }
    }
}
