# Provenance — QuantraVision Overlay

Each pattern detection is recorded with:
- SHA-256 hash of image input
- pattern type and template ID
- confidence score
- timestamp (UTC ISO-8601)
- local signature of engine version

A deterministic build hash is generated at compile time using 
`gradle tasks :hashSources`. This ensures identical source → identical hash → identical output.

Provenance logs are stored in `PatternMatch.db` and exportable as YAML bundles for Greyline OS integration.
