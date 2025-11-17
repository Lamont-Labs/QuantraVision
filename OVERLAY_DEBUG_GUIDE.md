# Overlay Service Debugging Guide

## How to View Android Logs

After installing the APK, connect your Samsung S23 FE via USB and run:

```bash
adb logcat | grep -E "ScanViewModel|OverlayService|FloatingLogoButton"
```

This will show detailed logs from the overlay initialization process.

## Expected Log Sequence (Success)

When you tap "Start Scanner" and grant permission, you should see:

### 1. ScanViewModel Logs
```
I/ScanViewModel: === Starting OverlayService ===
I/ScanViewModel: MediaProjection resultCode: -1
I/ScanViewModel: MediaProjection data: Intent { ... }
I/ScanViewModel: ✓ MediaProjection result stored in companion object
I/ScanViewModel: Starting service...
I/ScanViewModel: ✓ startForegroundService() called
I/ScanViewModel: Minimizing app to background...
I/ScanViewModel: ✓ App moved to background
```

### 2. OverlayService.onCreate() Logs
```
I/OverlayService: === OverlayService.onCreate() START ===
I/OverlayService: ✓ Overlay permission granted
I/OverlayService: ✓ WindowManager initialized
I/OverlayService: Creating full-screen overlay view...
I/OverlayService: ✓ Full-screen overlay view added
I/OverlayService: ✓ EnhancedOverlayView initialized successfully
I/OverlayService: Creating FloatingMenu...
I/OverlayService: ✓ FloatingMenu created
I/OverlayService: Creating FloatingLogoButton...
I/OverlayService: Setting up FloatingLogoButton callbacks...
I/OverlayService: Calling FloatingLogoButton.show()...
```

### 3. FloatingLogoButton Logs
```
I/FloatingLogoButton: show() called, isAdded=false
I/FloatingLogoButton: Adding logo view to WindowManager at position (X, Y)...
I/FloatingLogoButton: ✓ Logo view successfully added to WindowManager
```

### 4. Service Completion
```
I/OverlayService: ✓ FloatingLogoButton.show() completed
I/OverlayService: ✓ FloatingLogoButton fully initialized
I/OverlayService: Starting foreground service...
I/OverlayService: === OverlayService.onCreate() COMPLETE ===
```

### 5. onStartCommand Logs
```
I/OverlayService: === OverlayService.onStartCommand() START ===
I/OverlayService: Intent action: ACTION_START_WITH_PROJECTION
I/OverlayService: Consuming MediaProjection result from companion object...
I/OverlayService: ✓ MediaProjection result found (resultCode=-1)
I/OverlayService: === OverlayService.onStartCommand() COMPLETE ===
```

## Common Failure Points

### If Q logo doesn't appear:

1. **Check for permission errors:**
   ```
   E/OverlayService: CRITICAL: SYSTEM_ALERT_WINDOW permission not granted
   ```
   → Go to Settings → Apps → QuantraVision → Enable "Display over other apps"

2. **Check for FloatingLogoButton errors:**
   ```
   E/FloatingLogoButton: CRITICAL: Failed to add logo view to WindowManager
   ```
   → Look at the error details immediately below this line

3. **Check for service early termination:**
   ```
   E/OverlayService: CRITICAL: Failed to add overlay view
   ```
   → Service stopped before FloatingLogoButton was created

### If "Failed" toast appears:

```
E/OverlayService: CRITICAL: MediaProjection data is NULL
```
→ The permission result wasn't stored correctly (race condition or Android security issue)

## What to Share for Debugging

If the overlay still doesn't work, run this command while reproducing the issue:

```bash
adb logcat -d > overlay_debug_log.txt
```

Then share the `overlay_debug_log.txt` file - it contains the complete log including any errors.

## Known Issues

- **Android 14 MediaProjection changes**: Some Android 14+ devices may have additional restrictions on screen capture that require special handling
- **OEM customizations**: Samsung's OneUI may have additional overlay restrictions in certain modes (e.g., Game Launcher, Edge Screen)
