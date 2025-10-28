#!/usr/bin/env bash
set -euo pipefail
mkdir -p app/src/main/assets/patterns
python3 scripts/tools/gen_patterns.py
echo "Pattern images generated in app/src/main/assets/patterns/"
