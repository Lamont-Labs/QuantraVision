# Changelog

## 4.0.0 preview — 2026-10-31

- Rebuilt the app around image import, crop, analysis, result outlines, journal, notes, settings and image/JSON sharing.
- Added a standalone pixel measurement and formation engine with explicit light/dark and colored/gray wick handling, unsupported-image rejection and cancelable work.
- Rebuilt live capture around Android consent, a single virtual display, a movable tap-to-scan control and explicit stop/cleanup.
- Removed startup dependencies on optional models and permission gates from image analysis. The app has no internet permission.
- Added complete Android workflow checks on API 26 and API 35, including real screen capture, and source/hash/signature information for downloadable APKs.
- Separated screenshot geometry from unavailable market values and unmeasured trading or accuracy claims.

Earlier implementations are preserved in git history.
