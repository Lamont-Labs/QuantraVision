# QuantraVision 4

A rebuilt Android chart-image workspace. Import a screenshot or capture another app, inspect measured candle formations, and keep a private journal with notes and annotated exports.

Launch **QuantraVision 4**, the mint Q icon. Android 8.0 and newer; the APK is universal and contains no CPU-specific native libraries. The application ID is `com.lamontlabs.quantravision.next`.

## Working paths

- **Image import:** Android's file picker and image sharing → optional corner-drag crop → local analysis → measured outlines and explanations.
- **Live screen:** grant overlay access when requested → approve Android's screen-capture consent → open a chart app → tap the floating Scan control → analysis opens and saves. Stop through the floating control or Android notification.
- **Journal:** image, measured candles, outlines, date, source and editable note are stored locally. Reopen an entry, share its annotated chart, export journal JSON or delete data.
- **Settings:** export annotations, action haptics, supported formations, capture help and data management.

The app has no internet permission or runtime SDK dependencies. Analysis does not require a model download, account or subscription. The source has one Activity-based interface, one measurement engine, one persistence layer and one consent-bound capture service.

## What analysis means

The engine measures filled red/green or red/teal candle bodies and colored/gray wicks, then applies geometric formation rules. At least eight coherent candles are required. Light and dark charts are supported. Blank images, unsupported palettes, noise and volume-only histograms are rejected with a usable explanation.

Supported structures: double tops/bottoms, head-and-shoulders/inverse head-and-shoulders, ascending/descending/symmetrical triangles. Recent candle rules: doji, long upper/lower wicks, bullish/bearish engulfing bodies.

These are **visual observations**, not confirmed trading signals. Coordinates are image pixels. Market prices, volume, candle times and future returns are not invented. No success probability or dataset accuracy percentage is claimed. The educational descriptions identify context that a screenshot cannot establish.

The sample is explicitly labeled generated demonstration data. It passes through the same image measurement and pattern code as imported and captured images.

## Build and verify

Java 17, Android SDK 35 and Build Tools 35.0.0 are required for the Gradle build.

```sh
bash gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
```

The pure pixel/geometry regressions can also run locally without Gradle or Android:

```sh
python3 scripts/check-core.py
```

GitHub Actions builds the app once, then installs that exact APK on Android 8/API 26 and Android 15/API 35 emulators. The tests exercise sample and imported images, crop, notes, journal persistence/deletion, settings, exports, actual MediaProjection consent, screen acquisition, and capture-session stop. API 35 also uses the real Android image picker. Screenshots, UI hierarchies and logs are retained for failures and successful workflows.

Only a successful main workflow publishes a GitHub prerelease containing the APK, source/hash manifest and signing certificate. These are development-signed builds, not a Play Store release. Physical-phone acceptance, broad real-chart recognition accuracy, accessibility auditing and battery targets still need additional evidence.

## Source

- `engine/ChartEngine.java`: connected components, body/wick measurements, series validation and deterministic formation rules; no Android dependency.
- `MainActivity.java`, `Ui.java`, `ChartView.java`, `CropActivity.java`: interface, image/crop presentation and measured outlines.
- `AppSession.java`: serialized, cancelable work and Activity recreation state.
- `AppStore.java`: atomic journal/image persistence and export format.
- `CaptureService.java`: one consent token and one virtual display per session, single-frame capture on explicit taps, cleanup on Android stop.
- `ShareProvider.java`: narrowly scoped, read-only export URIs.

This rebuild replaces the previous app implementation; previous source and research documents remain in git history and the earlier repair branch. They are not validation evidence for version 4.

## References and license

Android's [MediaProjection contract](https://developer.android.com/media/grow/media-projection) governs capture consent, foreground service and display lifecycle. Geometric pattern terminology is discussed in [Lo, Mamaysky and Wang](https://www.nber.org/papers/w7613); this app uses its own screenshot heuristics and does not reproduce that paper's market-data study or results.

Apache License 2.0; see [LICENSE](LICENSE).
