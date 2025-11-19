# QuantraVision release shrink rules - Optimized for maximum code shrinking
# Aggressive optimization passes
-optimizationpasses 7
-allowaccessmodification
-mergeinterfacesaggressively
-dontpreverify
-dontskipnonpubliclibraryclassmembers

# Keep TFLite & reflection (only what's needed)
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-dontwarn org.tensorflow.**
-dontwarn com.google.gson.**

# Keep Compose runtime (not all Compose metadata)
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi

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

# Strip Timber logs in release - TEMPORARILY DISABLED FOR DEBUGGING
# TODO: Re-enable after model import is working
#-assumenosideeffects class timber.log.Timber {
#    public static *** d(...);
#    public static *** v(...);
#    public static *** i(...);
#    public static *** w(...);
#}

# Remove test/debug utilities (belt-and-suspenders; already in test source set)
-assumenosideeffects class * {
    void println(...);
}

# R8 Full Mode aggressive optimizations
-repackageclasses ''
-overloadaggressively

# Remove debug info from classes
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Aggressive method inlining
-optimizations code/removal/*,code/allocation/variable,method/inlining/*,method/removal/*,field/removal/*,class/merging/*

# Keep essential Android components
-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }

# Room Database - Keep entity classes and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keep @androidx.room.Dao class *

# Keep Billing classes for Google Play integration
-keep class com.android.billingclient.** { *; }
-keepclassmembers class com.android.billingclient.** { *; }

# Keep data classes and enums
-keepclassmembers class * {
    @kotlinx.serialization.* <fields>;
}
-keepclassmembers enum * { *; }
