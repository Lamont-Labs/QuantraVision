# QuantraVision™ — Provenance & ProofChain Documentation

## Purpose
To ensure deterministic verification of every distributed QuantraVision build and dataset.  
This system allows any reviewer or acquirer to verify that each release matches Lamont Labs’ immutable source signatures.

---

## Verification Steps

### 1. Compute Current Build Hashes
Run:
```bash
bash verify.sh
```
Outputs:
```
src_hash.txt   → code fingerprint  
res_hash.txt   → resources fingerprint
```

### 2. Compare to Manifest
Ensure computed SHA256 digests match `manifest.json → hashes`.

### 3. Validate Ed25519 Signature
Run:
```bash
openssl dgst -sha256 -verify pubkey.pem \
  -signature signature.txt src_hash.txt
```
If verified → build is authentic and unmodified.

---

## Signature Policy
| Element | Algorithm | Key Length | Rotation |
|----------|------------|-------------|-----------|
| ProofGate Signing | Ed25519 | 256-bit | 180 days |
| Catalog Hash | SHA256 | n/a | per pattern update |
| Export Bundle | Ed25519 + SBOM digest | 256-bit | per release |

---

## Proof Files Overview
```
/app/src/main/assets/
 ├─ manifest.json          → primary manifest
 ├─ signature.txt          → Ed25519 signature of manifest
 ├─ pubkey.pem             → public verification key
 ├─ sbom.json              → generated software bill of materials
 └─ proof_log.txt          → timestamped verification log
```

---

## Trust Anchors
All QuantraVision cryptographic materials are created and stored offline.  
Private keys are never shipped in source or binaries.  
ProofGate maintains the immutable key lineage ledger with rotation audit files in `/proof/rotation_logs/`.

---

## Verifier Compatibility
Compatible verifiers:
- Lamont Labs Verify CLI (`verify.sh`)
- OpenSSL 3.0+
- Any Ed25519-compliant verifier library

---

## Deterministic Build Principle
If two independent builds (with identical inputs) yield identical `src_hash.txt`,  
QuantraVision passes reproducibility certification under Lamont Labs Standard 1.2.  

All discrepancies trigger fail-closed conditions and block public release.
