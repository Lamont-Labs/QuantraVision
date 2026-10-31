# Android verification

`ci.yml` is the single Android workflow for main, repair branches, pull requests and manual dispatches.

1. `verify` rejects unconditional test assertions, runs the full JVM suite and Android lint, builds the ARM64 debug APK, verifies its signature and records its source commit, version, test totals and SHA-256.
2. `android-smoke` runs the installed app on an Android 35 emulator, exercises navigation and checks native OpenCV reference-template localization. Reports and screenshots are uploaded.
3. After both jobs pass on a push to main, `development-apk` publishes the verified development APK and identity files as a GitHub prerelease.

Branch artifacts are named `QuantraVision-debug-<run number>`. Main development releases are tagged `v3.1.1-repair.<run number>` and install as **QuantraVision Repair** beside the production app.

These checks do not establish broad chart-recognition accuracy, physical-phone acceptance, real Play purchase completion or production readiness. Unit and lint errors fail verification; no failures are ignored. Other Android build workflows were removed.
