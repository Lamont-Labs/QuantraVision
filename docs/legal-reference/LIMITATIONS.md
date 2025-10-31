# Known Limitations — QuantraVision Overlay v1.0-seed

1. Static Detection Only  
   - Phase 1 supports only preloaded chart image scanning.  
   - Live overlay scanning requires MediaProjection integration (Phase 2).

2. Template Library Coverage  
   - Only 3 sample pattern YAMLs included.  
   - Users can manually import more patterns via deterministic YAML definition.

3. Performance  
   - OpenCV `matchTemplate` is CPU-bound.  
   - Larger charts may cause ≈ 1 – 2 s processing delay per image.

4. Provenance Export  
   - Exports to YAML only. Greyline binder integration pending.

5. Device Compatibility  
   - Overlay permission handling differs on OEM firmwares; manual grant may be required.

6. Verification Scope  
   - No automated visual comparison of bounding boxes yet.  
   - Deterministic confidence only.

This version is intentionally minimal to demonstrate offline, deterministic pattern detection.
