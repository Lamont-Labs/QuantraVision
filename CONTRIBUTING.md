# Contributing

Use Java 17 and Android SDK 35. Runtime code is Java and Android framework APIs; keep the pixel engine independent of Android so its tests run locally.

Before publishing changes, run the core tests, Gradle unit tests, Android lint and both APK assemblies. The Android 8 and Android 15 workflow tests must pass. Changes to image measurement need positive and rejection fixtures; changes to a user flow need an instrumented workflow regression. Never replace a failed observation with a different detector or a fabricated market value.

PR descriptions should state the concrete behavior, motivation, tests and evidence boundaries. Include UI screenshots for visual changes and update the changelog for user-facing behavior. Do not claim coverage percentages, accuracy or device acceptance without measured evidence.

Journal data is private. Exports must be read-only, explicitly selected by the user and limited to the export directory. Capture must respect Android consent and stop lifecycle requirements.
