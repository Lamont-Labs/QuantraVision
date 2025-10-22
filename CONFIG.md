# Configuration — QuantraVision Overlay (Multi-Timeframe v1.1)

## Detection Engine
- multi_scale.enabled: true
- multi_scale.levels: 9
- multi_scale.scale_factor: 0.85
- aspect_tolerance: 0.12           # allow ±12% aspect deformation
- nms.iou_threshold: 0.25           # prune overlapping matches
- min_confidence_global: 0.70
- grayscale_preprocessing: true
- histogram_equalization: true

## Template Schema (v1.1)
Each YAML in `assets/pattern_templates/` may include:
- name: string
- image: path-to-reference-png
- threshold: 0.70–0.95
- scale_range: [0.5, 2.0]           # relative to reference image
- aspect_tolerance: 0.10            # overrides global if present
- timeframe_hints: ["1m","5m","15m","1h","4h","D","W"]
- min_bars: 20                      # visual density guard
- notes: string

## Timeframe Agnosticism
Charts are images with varying DPI and bar width. Engine normalizes by:
- grayscale + CLAHE (contrast normalization)
- Gaussian blur for noise robustness
- multi-scale, slight aspect sweep
- NMS to deduplicate multi-scale hits

## Provenance
Store best match per scale with:
- scale_used
- aspect_used
- confidence
- template_hash
- input_hash

## Phase-2 (screen overlay) continuity
Exactly same pipeline per captured frame. Determinism preserved.
