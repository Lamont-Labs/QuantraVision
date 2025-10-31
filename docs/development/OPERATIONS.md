# Operations Manual — QuantraVision Overlay

### Launch
1. Grant overlay + screen-capture permissions.  
2. Open app; select “Load Static Chart.”  
3. Engine runs OpenCV pattern match on image.  
4. Results render as highlight boxes with confidence %.  

### Provenance Check
`Verify → Recalculate Hash` confirms pattern integrity.  

### Log Review
Accessible via Settings → Detection Logs.  

### Safety
No external I/O. Network stack disabled in manifest.  

### Build Verification
Run `make verify` to ensure deterministic hashes match expected values.
