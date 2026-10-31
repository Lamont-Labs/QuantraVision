#!/usr/bin/env bash
set -euo pipefail

# The runner shuts the emulator down when this script exits. Collect evidence
# inside the session, including when a test fails, and preserve a failed status.
collect_proof() {
  local scan_status=$?
  trap - EXIT
  mkdir -p device-proof
  if ! timeout 20s adb logcat -d > device-proof/logcat.txt 2>&1; then
    scan_status=1
  fi
  if ! timeout 20s adb pull /sdcard/Android/data/com.lamontlabs.quantravision.debug/files/. device-proof/ > device-proof/pull-log.txt 2>&1; then
    scan_status=1
  fi
  exit "$scan_status"
}
trap collect_proof EXIT

bash ./gradlew :app:installDebug :app:installDebugAndroidTest -PqvTestAbi=x86_64 --no-daemon
adb shell appops set com.lamontlabs.quantravision.debug SYSTEM_ALERT_WINDOW allow
adb shell pm grant com.lamontlabs.quantravision.debug android.permission.POST_NOTIFICATIONS
bash ./gradlew :app:connectedDebugAndroidTest -PqvTestAbi=x86_64 -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true -Pandroid.testInstrumentationRunnerArguments.class=com.lamontlabs.quantravision.integration.DetectionFlowTest,com.lamontlabs.quantravision.integration.StartupSmokeTest --no-daemon --stacktrace
