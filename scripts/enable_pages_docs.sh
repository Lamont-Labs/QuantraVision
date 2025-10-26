#!/usr/bin/env bash
# QuantraVision — GitHub Pages enablement (docs folder, auto-deploy via Actions)
# Idempotent. Safe to run multiple times.

set -euo pipefail

# 1) Ensure docs exist and populate from legal sources
mkdir -p docs
touch docs/.nojekyll  # disable Jekyll processing so _ files work if any

# Create minimal index with links
cat > docs/index.md <<'MD'
# QuantraVision — Legal & Compliance

- **Privacy Policy:** [/privacy](privacy)
- **Terms of Use:** [/terms](terms)
- **License:** [/license](license)

Built by **Lamont Labs**. QuantraVision is an offline, observation-only overlay that never executes trades.
MD

# Mirror legal sources if present; otherwise create stubs
if [ -f legal/PRIVACY_POLICY.md ]; then cp legal/PRIVACY_POLICY.md docs/privacy.md; else
cat > docs/privacy.md <<'MD'
# Privacy Policy
QuantraVision operates fully offline. No user data leaves the device. This page will be replaced by the maintained policy in `legal/PRIVACY_POLICY.md`.
MD
fi

if [ -f legal/TERMS_OF_USE.md ]; then cp legal/TERMS_OF_USE.md docs/terms.md; else
cat > docs/terms.md <<'MD'
# Terms of Use
QuantraVision is an observation-only tool. It does not provide financial advice nor execute trades.
This page will be replaced by the maintained terms in `legal/TERMS_OF_USE.md`.
MD
fi

if [ -f legal/LICENSE.md ]; then cp legal/LICENSE.md docs/license.md; else
cat > docs/license.md <<'MD'
# License
Lamont Labs ©. License file not found in `/legal`. Add `legal/LICENSE.md` to override this page.
MD
fi

# 2) Add/Update Pages workflow
mkdir -p .github/workflows
cat > .github/workflows/deploy-pages.yml <<'YML'
name: deploy-pages
on:
  push:
    branches: [ "main" ]
  workflow_dispatch:
permissions:
  contents: read
  pages: write
  id-token: write
concurrency:
  group: "pages"
  cancel-in-progress: false
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Upload docs artifact
        uses: actions/upload-pages-artifact@v3
        with:
          path: ./docs
  deploy:
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - id: deployment
        uses: actions/deploy-pages@v4
YML

# 3) Commit
git add docs .github/workflows/deploy-pages.yml
git commit -m "[replit-add] Enable GitHub Pages from /docs with auto-deploy workflow" || true

echo "GitHub Pages ready. Push to main, then Pages will auto-deploy from /docs."
echo "Privacy URL: https://<github-username>.github.io/<repo>/privacy"
echo "Terms URL:   https://<github-username>.github.io/<repo>/terms"
