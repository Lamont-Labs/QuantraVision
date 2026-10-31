#!/usr/bin/env python3
"""Reject unconditional assertions that previously masqueraded as workflow tests."""
from pathlib import Path
import re
import sys

literal_assertion = re.compile(r'assert(?:True|False)\(\s*(?:"(?:[^"\\]|\\.)*"\s*,\s*)?(?:true|false)\s*\)')
violations = []
for root in (Path('app/src/test'), Path('app/src/androidTest')):
    for path in root.rglob('*.kt'):
        for line, text in enumerate(path.read_text().splitlines(), 1):
            if literal_assertion.search(text):
                violations.append(f'{path}:{line}: unconditional assertion')
if violations:
    print('\n'.join(violations), file=sys.stderr)
    sys.exit(1)
print('Test integrity: no unconditional boolean assertions')
