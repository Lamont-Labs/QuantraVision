# Handoff Package — QuantraVision Overlay v1.0-seed

## Overview
This repository represents a deterministic, privacy-first AI overlay system
that visually identifies known chart patterns on-device. It was designed for
handoff or acquisition by fintech or trading-education firms seeking
visual AI intelligence layers without network risk.

## Bundle Contents
- Full Android Studio source (Compose + OpenCV + Room)
- Pattern templates (YAML)
- Demo charts
- Provenance scripts and logs
- Verification Makefile
- Documentation (CONFIG, OPERATIONS, PROVENANCE)

## Verification Process
1. Run `make verify` to hash assets.  
2. Compare hash outputs with expected values in PROVENANCE.md.  
3. Rebuild project → identical hashes confirm determinism.  

## Legal and IP
All code © 2025 Jesse J. Lamont / Lamont Labs.  
Licensed Apache 2.0. Patent claims pending for visual overlay render method.  

## Next Steps
- Integrate MediaProjection for live overlay (Phase 2).  
- Enable Greyline OS binder export (Phase 3).  
- Submit APK to internal testing track for QA.  

This handoff package is demo-safe and ready for review by acquisition teams or investors.
