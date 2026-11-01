#!/usr/bin/env bash
set -euo pipefail
mkdir -p device-proof
collect() {
    local result=$?
    trap - EXIT
    timeout 20s adb logcat -d > device-proof/logcat.txt 2>&1 || result=1
    timeout 20s adb pull /sdcard/Android/data/com.lamontlabs.quantravision.next/files/proof/. device-proof/ > device-proof/pull.txt 2>&1 || result=1
    exit "$result"
}
trap collect EXIT
adb install -r device-bundle/app.apk
adb install -r -t device-bundle/test.apk
adb install -r device-bundle/fixture.apk
adb shell settings put system screen_off_timeout 1800000
adb shell svc wifi disable || true
adb shell svc data disable || true
test_class=com.lamontlabs.quantravision.next.CompatibilityFlowTest
if [[ "${ANDROID_API:-35}" -ge 29 ]]; then test_class=com.lamontlabs.quantravision.next.FullFlowTest; fi
timeout 360s adb shell am instrument -w -r -e class "$test_class" -e listener com.lamontlabs.quantravision.next.ProofListener com.lamontlabs.quantravision.next.test/androidx.test.runner.AndroidJUnitRunner | tee device-proof/instrumentation.txt
python3 - <<'PY'
import re,json
from pathlib import Path
log=Path('device-proof/instrumentation.txt').read_text()
match=re.search(r'OK \((\d+) tests?\)',log)
assert match and int(match[1])>=4 and 'FAILURES!!!' not in log and 'INSTRUMENTATION_FAILED' not in log and 'Process crashed' not in log, 'Android workflow tests did not all pass'
assert 'INSTRUMENTATION_CODE: -1' in log, 'Instrumentation did not finish cleanly'
Path('device-proof/result.json').write_text(json.dumps({'tests':int(match[1]),'failures':0,'errors':0,'skipped':0},indent=2))
PY
