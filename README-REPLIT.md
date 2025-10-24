# QuantraVision — Replit build

## One-click run
Press **Run**. It will:
1) Install Android SDK cmdline-tools into `$HOME/android-sdk`.
2) Accept licenses and install `platform-tools`, `platforms;android-34`, `build-tools;34.0.0`.
3) Generate Gradle wrapper if missing.
4) Build `assembleDebug`, run `lint` and `test`.

Artifacts:
- Debug APK: `app/build/outputs/apk/debug/*.apk`
- Release AAB (via `bash scripts/build-release.sh`): `app/build/outputs/bundle/release/*.aab`

## CLI
```bash
make sdk      # one-time
make debug    # debug APK
make release  # release AAB (requires signing config)
