# Configuration — QuantraVision Overlay

## Permissions
- SYSTEM_ALERT_WINDOW  
- MediaProjection API  
- Foreground Service for overlay persistence  

## Modes
- **Static Detection (Phase 1):** Analyze chart images from assets folder.  
- **Live Overlay (Phase 2):** Real-time screen capture + pattern highlight.  

## Provenance Storage
- Room database `PatternMatch.db`  
- Each match entry: timestamp, pattern_id, confidence, hash  

## Template Library
Stored in `assets/pattern_templates/` as YAML files.  
Integrity checked on load via SHA-256.

## Logging
All detections → `/data/data/com.lamontlabs.quantravision/files/logs/`
