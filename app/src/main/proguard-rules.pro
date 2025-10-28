# QuantraVision — ProGuard
-dontoptimize
-dontpreverify
-keep class org.tensorflow.** { *; }
-keep class org.opencv.** { *; }
-keep class com.android.billingclient.** { *; }
-keep class androidx.camera.** { *; }
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-assumenosideeffects class android.util.Log { *; }
