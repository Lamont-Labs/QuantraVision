# QuantraVision release shrink rules - Optimized for maximum code shrinking
# 
# CRITICAL WARNING: This file caused a production crash bug in Nov 2024
# Issue: -assumenosideeffects on Log.i/w/e caused empty catch blocks to be removed
# Result: Uncaught exceptions crashed the app instantly on launch
# Fix: Only strip debug/verbose logs, ALWAYS keep error/warning/info logs
# Rule: Never use -assumenosideeffects on code that appears in exception handlers
#
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

# OpenCV - Maven Central distribution (org.opencv:opencv:4.10.0)
# Keep all OpenCV classes for native library loading
-keep class org.opencv.** { *; }
-keepclassmembers class org.opencv.** { *; }
-keep class org.opencv.core.** { *; }
-keep class org.opencv.imgproc.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}
-dontwarn org.opencv.**

# Strip logs in release
# CRITICAL: Only strip debug/verbose. NEVER strip error/warning/info logs!
# Reason: Exception handlers use Log.e/w/i. If stripped, catch blocks become empty
# and ProGuard removes them entirely, causing crashes.
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
# DO NOT ADD Log.e/w/i here - they are needed for exception handling

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

# R8 Full Mode aggressive optimizations
-repackageclasses ''
-overloadaggressively

# Remove debug info from classes
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Aggressive method inlining
-optimizations code/removal/*,code/allocation/variable,method/inlining/*,method/removal/*,field/removal/*,class/merging/*

# Keep essential Android components - CRITICAL for app stability
-keep class * extends android.app.Application { 
    *; 
}
-keep class com.lamontlabs.quantravision.App { 
    *; 
}
-keepclassmembers class * extends android.app.Activity { *; }
-keepclassmembers class * extends android.app.Service { *; }
-keepclassmembers class * extends android.content.BroadcastReceiver { *; }

# CRITICAL: Prevent stripping of Log.e() calls - needed for exception handlers
-keep class android.util.Log {
    public static int e(...);
}
-keepclassmembers class android.util.Log {
    public static int e(...);
}

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
