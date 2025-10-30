# QuantraVision release shrink rules
# Keep TFLite & reflection
-keep class org.tensorflow.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn org.tensorflow.**
-dontwarn com.google.gson.**

# Keep Compose metadata
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# ML Kit Text Recognition
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.vision.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**

# OpenCV
-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** { *; }
-keep class org.opencv.core.** { *; }
-keep class org.opencv.imgproc.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-dontwarn org.opencv.**

# Strip logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
