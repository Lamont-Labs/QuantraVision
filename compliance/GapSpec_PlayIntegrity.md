# ==================================================
# GAP SPEC — PLAY INTEGRITY + ANTI-TAMPER SYSTEM
# ==================================================
**Goal:** Prevent unauthorized modification, cloning, or debugging of QuantraVision APKs.

## Components
- **IntegrityValidator.kt** — interfaces with Google Play Integrity API.
- **SignatureVerifier.kt** — validates Lamont Labs release key fingerprint.
- **DebuggerDetection.kt** — monitors `Debug.isDebuggerConnected()`.
- **RootCheck.kt** — basic root indicators via system properties and common binaries.
- **Obfuscation:** R8 + ProGuard baseline with `minifyEnabled true`, custom rules.

## Enforcement
- Launch gate in `MainActivity` calls IntegrityValidator.
- If verification fails: display fail-closed banner, block overlay service.
- Verification proof written to `/app/internal/logs/integrity.log`.

## CI
- Include fingerprint check step in GitHub workflow.
- Verify release key hash matches manifest fingerprint.

## Compliance
- Required by Play Console “Integrity and App Signing” section.
- Aligns with Lamont Labs security posture (fail-closed principle).

## References
- Google Play Integrity API docs.
- OWASP MASVS L1–L2 mobile security checklist.
