# ==================================================
# GAP SPEC — BACKUP & RETENTION POLICY
# ==================================================
**Goal:** Define data retention, deletion, and backup standards for QuantraVision.

## Retention Rules
| Data Type | Retention | Auto-Delete | User Deletion |
|------------|------------|--------------|----------------|
| Crash Logs | 7 days | ✅ | ✅ |
| Pattern Logs | 30 days | ✅ | ✅ |
| Feedback Data | 60 days or 10k events | ✅ | ✅ |
| Preferences / Watchlist | Indefinite (user controlled) | ❌ | ✅ |
| Sync Blobs | 90 days | ✅ | ✅ |

## Backups
- Local encrypted backup via Android Auto Backup excluded.
- Optional encrypted sync only if user opts in.
- No cloud copy by default.

## Compliance
- GDPR “right to be forgotten” implemented via Settings > Data Management.
- Automatic purging tasks run daily in background worker.

## Binder Reference
Append to `docs/LEGAL_INDEX.yaml → DataRetentionPolicy`
