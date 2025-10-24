# QuantraVision release shrink rules
# Keep TFLite & reflection
-keep class org.tensorflow.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn org.tensorflow.**
-dontwarn com.google.gson.**

# Keep Compose metadata
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Strip logs in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
