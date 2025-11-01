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
    public static *** w(...);
}

# Strip Timber logs in release
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Remove test/debug utilities (belt-and-suspenders; already in test source set)
-assumenosideeffects class * {
    void println(...);
}

# Optimize: Remove unused code and inline aggressively
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep essential Android components
-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }
