# ProGuard rules for QuantraVision Overlay
# Keep Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable <methods>;
}
# Keep OpenCV native bindings
-keep class org.opencv.** { *; }
# Keep TensorFlow Lite
-keep class org.tensorflow.lite.** { *; }
