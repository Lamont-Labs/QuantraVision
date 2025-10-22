package com.lamontlabs.quantravision

data class DetectionConfig(
    val multiScaleEnabled: Boolean = true,
    val levels: Int = 9,
    val scaleFactor: Double = 0.85,
    val aspectTolerance: Double = 0.12,
    val iouThreshold: Double = 0.25,
    val minConfidenceGlobal: Double = 0.70,
    val grayscale: Boolean = true,
    val equalizeHist: Boolean = true
)
