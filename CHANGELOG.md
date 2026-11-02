# Changelog

## 4.1.0 preview.3 — 2026-11-01

- Give Studio a separate install identity after verification found that its signing certificate differed from the earlier preview. The earlier app and its journal remain accessible.
- Pin the Studio signing file and dedicated cache so subsequent Studio builds keep one certificate.
- Retain all tested Studio workflows and the unchanged geometry-3 engine/evaluation.

## 4.1.0 preview.2 — 2026-11-01

- Recovered the Studio rewrite: restrained dark/gold workspace, embedded typography, searchable visual atlas, persistent detail navigation, chart zoom/pan/fit and full-image controls.
- Keep reader selection and the Analyze action fixed above navigation, including when a tall image or larger text pushes chart content below the fold.
- Fixed rejected-image error propagation and kept detail/evaluation back controls outside their scroll containers.
- Added hollow-candle reading, neutral body outlines and visible older-candle inspection.
- Added an explicit experimental trace reader; saved trace pixels remain separate from candle observations and never trigger candle rules.
- Added multiple swing scales, sloping-neckline comparison and duplicate-outline suppression. Engine identity is `geometry-3`; existing `geometry-1` and `geometry-2` journal records retain their original provenance.
- Packaged the complete external development evaluation, including all misses. This iteration matches 7 of 16 labeled targets, up from 4 in the recovered working source. Nine targets remain missed; the set is small, selected, correlated and used during development.
- Bound reference inputs to decoded RGBA pixels and dimensions while preserving original acquisition byte hashes. Lossless recompression is accepted; pixel changes fail.
- Expanded required CI checks to 46 engine regressions, image-identity checks, Android lint and app workflow tests on API 26/35/36. CI results, rather than this change list, determine whether a build is publishable.

## 4.0.0 preview — 2026-10-31

- Rebuilt the app around image import, crop, analysis, result outlines, journal, notes, settings and image/JSON sharing.
- Added a standalone pixel measurement and formation engine with explicit light/dark and colored/gray wick handling, unsupported-image rejection and cancelable work.
- Rebuilt live capture around Android consent, a single virtual display, a movable tap-to-scan control and explicit stop/cleanup.
- Removed startup dependencies on optional models and permission gates from image analysis. The app has no internet permission.
- Added complete Android workflow checks on API 26 and API 35, including real screen capture, and source/hash/signature information for downloadable APKs.
- Separated screenshot geometry from unavailable market values and unmeasured trading or accuracy claims.

Earlier implementations are preserved in git history.
