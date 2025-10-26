# Build Provenance

Each release includes:
- `dist/release/app-release.aab` — Play package
- `dist/release/sha256.txt` — SHA-256 of AAB
- `dist/release/provenance.json` — Ed25519 signature and public key
- `dist/sbom.json` — dependency manifest

Reproduce:
1. Check out the tagged commit.
2. Run `scripts/replit_master_build.sh`.
3. Verify hash matches `sha256.txt`.
