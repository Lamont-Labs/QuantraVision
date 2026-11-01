# QuantraVision Studio

An Android workspace for inspecting chart screenshots, visible formations and your own notes. Android 8.0+, application ID `com.lamontlabs.quantravision.next`. The app has no Internet permission, account requirement or runtime SDK dependency. Launch **QuantraVision Studio**, the gold Q icon.

Studio adds a chart-first workspace, pinch/double-tap zoom and pan, a searchable 12-rule pattern atlas, detection criteria, an external-image evidence ledger and persistent study notes. Screenshot import, corner-drag crop, the Android file picker, exports and consent-based live capture remain real native app flows.

## Recognition and its limits

The candlestick reader measures coherent red/green or red/teal bodies, including supported outlined bodies, and visible wicks on light/dark backgrounds. It requires eight observations. Body colors identify direction; no market prices, candle timestamps, volume or future returns are inferred.

An **explicit experimental price-trace mode** reads a single cropped plot for structural rules. It records observed pixel samples separately, never fabricated candles or OHLC. Crop out menus, indicators and annotations; overlays and guides can still confuse it. There is no silent fallback between modes.

Twelve implemented rules cover double tops/bottoms, head-and-shoulders/inverse, three triangle types, doji, long upper/lower wicks and engulfing bodies. The atlas explains their geometry and missing context. A rule match is a visual observation, not a confirmed trading signal or success probability.

The [external-image development report](benchmarks/real_charts/REPORT.md) contains **all** labeled hits and misses, exact source links, input hashes, crops, target regions and baseline results. Nine source charts yield twenty correlated image cases and sixteen positive targets. This small selected development set is not a held-out accuracy study; negative controls are few and four rules have no external positive examples. Report totals describe this exact source revision only. The generated example and synthetic regression fixtures are not counted as real-chart evidence.

## Use

- **Workspace:** import a PNG/JPG/WebP screenshot, resume a study or start capture.
- **Analyze:** crop to a plot, choose Candles or experimental Price trace, inspect real input pixels and measured outlines. Double tap/pinch to zoom; drag while zoomed; Fit resets the view.
- **Atlas:** search/filter the implemented rules, inspect their schematics, criteria and measured external coverage.
- **Journal:** reopen an image and observations, add notes, export annotations or JSON, long-press to delete a study.
- **More:** preferences, evidence ledger, capture help and data management.

For live capture, grant the floating control when Android asks, approve Android's screen-sharing consent, open the chart app and tap Scan. Stop using the floating control or the notification. Secure-screen images cannot be captured.

## Build and verify

Requires JDK 17, Python 3, Android SDK 35 and Build Tools 35.0.0. The Gradle pre-build step fetches checksum-pinned OFL fonts and two licensed evaluation images. All network access happens at build time.

```sh
python3 -m pip install Pillow==12.0.0
python3 scripts/prepare-assets.py
python3 scripts/check-core.py
python3 scripts/evaluate-real-charts.py --check
bash gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest :fixture:assembleDebug
```

GitHub Actions builds one signed APK, checks the external report against the current engine, then installs that same APK on API 26, 35 and 36. Device flows cover import/crop, real market-image file picking, chart zoom, atlas and evidence navigation, notes, journal, legacy records, trace persistence, exports, actual MediaProjection consent/capture and stop. Screenshots and logs are retained for every run. Only a green main workflow publishes the development-signed prerelease APK, hash/commit manifest, evaluation and signing information.

The same application ID and cached development signing key are retained for updates from version 4. Physical phone acceptance and broader recognition validation remain necessary; an emulator pass alone does not establish them.

## Source and licenses

`ChartEngine` / `TraceReader` are pure Java pixel geometry. `MainActivity`, `Ui`, `ChartView` and the atlas activities implement the native interface. `AppSession` owns cancelable work; `AppStore` atomically stores images and observations, preserving the original engine version when editing legacy notes. `CaptureService` owns consent-bound capture and cleanup.

The app source is Apache 2.0. Bundled fonts retain OFL notices. The two original Commons evaluation images retain their separate [attribution and licenses](app/src/main/assets/evaluation/ATTRIBUTION.md). StockCharts publisher images are downloaded to an ignored reference cache for evaluation; they are not redistributed with the APK or repository. Old implementation/research claims in git history do not validate Studio.
