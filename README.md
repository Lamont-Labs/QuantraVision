# QuantraVision

QuantraVision is an Android chart-image assistant from Lamont Labs. It measures visible candlestick geometry and compares chart images with bundled OpenCV pattern templates. The application includes Home, Markets, Scan, QuantraBot and Settings navigation.

**Development status:** 3.1.1 repair build. Build and test status comes from [Android verification](https://github.com/Lamont-Labs/QuantraVision/actions/workflows/ci.yml). Historical production-readiness and accuracy claims are not validation evidence.

## What the repaired live scanner does

1. Checks the current scan allowance and availability of quota storage.
2. Measures candle bodies and wicks from supported red/green or red/teal chart images. At least ten readable candles are required.
3. Runs local OCR for optional symbol/timeframe metadata. Missing OCR remains unknown.
4. Validates the measured sequence and source boundaries through the chart scan guards.
5. Runs single-frame OpenCV template matching, using a frame-local pyramid and coordinates mapped back to the original screenshot.
6. Records scan usage before exposing results, then displays visual matches and saves them to local history.

A rejected image, unavailable storage, invalid measurements or detector error ends the scan. There is no alternate detector after rejection. Empty successful scans consume one scan; rejected scans do not.

## Evidence boundaries

- Screen measurements are **pixels and sequence ordinals**, not exchange OHLCV data.
- Market prices, volume and candle timestamps are unavailable in the screenshot route. The scanner does not invent them.
- A visual match is not a confirmed trading signal. Similarity percentages describe template correlation, not a probability of profit or independently measured detection accuracy.
- Screenshot analysis remains `WAIT`; market confidence and market score are unavailable. The UI does not present an invented score.
- Entry prices, stop-loss levels and targets are not generated from an assumed price.
- Monochrome, unsupported palettes, line charts, obscured charts and insufficient candles are rejected. Broader chart-style coverage needs additional fixtures and validation.
- Optional assistant models are independent of scanner startup. The scanner requires OpenCV, overlay permission and Android screen-capture consent.
- Cloud narration is not enabled in the repaired screenshot route. The retained market-data and learning protocol code is research infrastructure, not a verified production feed or completed learning system.

## Build

Requirements: Java 17, Android SDK 35, Build Tools 35.0.0 and access to Gradle/Maven repositories. Minimum Android version is 8.0 (API 26). The APK targets 64-bit ARM phones (`arm64-v8a`).

```sh
python3 scripts/check-test-integrity.py
bash ./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

GitHub Actions runs these checks and Android 35 emulator startup/navigation and native OpenCV localization checks in parallel. Both jobs must pass before a main build publishes its development APK. Download the `QuantraVision-debug-<run number>` artifact from a successful workflow run. Debug builds use the application ID `com.lamontlabs.quantravision.debug` and install beside the production app.

## Tests and remaining validation

Automated unit/regression coverage includes pixel fixtures, blank/noise/unsupported-image rejection, measured body/wick conversion, guard enforcement, quota rejection, detector failures, result-coordinate validation, scaled template bounds, proof input binding, locale stability and subscription selection/downgrades.

Tests that merely asserted a literal `true` were removed rather than counted as coverage. An unwired accuracy gate that required nonexistent metric files is preserved under `docs/research/`; it does not establish a measured F1 score. CI rejects new unconditional boolean assertions.

JVM units use a plain Android Application to isolate their subjects. The Android smoke job launches the real application. The instrumentation suite includes a real OpenCV reference-template localization test. Run the supported smoke checks on a connected compatible device:

```sh
bash ./gradlew :app:connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true -Pandroid.testInstrumentationRunnerArguments.class=com.lamontlabs.quantravision.integration.DetectionFlowTest,com.lamontlabs.quantravision.integration.StartupSmokeTest
```

A JVM test run and APK compilation do not establish device compatibility, screen-capture reliability, purchase completion through Google Play, chart recognition accuracy across trading platforms, or battery/performance targets. Those require device and dataset evidence. No percentage accuracy or production-readiness claim is made here.

## Source layout

- `pipeline/`: authoritative live scan orchestration and quota accounting.
- `vision/ScreenChartExtractor.kt`: deterministic pixel measurements.
- `detection/VisualTemplateScanner.kt`: OpenCV template matching and frame-local native memory.
- `apex/`: chart guards, typed source metadata, result aggregation and input-bound proof hashes.
- `overlay/`: Android capture service, floating control and scan result presentation.
- `billing/`: Play Billing integration and independently tested ownership-selection policy.

Older design documents and legacy detector implementations remain for reference. This README describes the repaired screen-overlay route; older documents must not be treated as evidence that all planned capabilities are implemented.

## License

Apache License 2.0; see [LICENSE](LICENSE). Third-party dependencies have their own licenses.
