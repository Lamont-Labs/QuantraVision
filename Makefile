# ==================================================
# QUANTRAVISION™ — BUILD & RELEASE AUTOMATION
# ==================================================

APP_NAME := QuantraVision
APP_ID := com.lamontlabs.quantravision
BUILD_DIR := app/build/outputs/apk/release
DIST_DIR := dist

.PHONY: all clean build sign verify bundle

all: clean build verify

clean:
	@echo "==> Cleaning previous builds..."
	./gradlew clean

build:
	@echo "==> Building release APK..."
	./gradlew assembleRelease

sign:
	@echo "==> Signing APK..."
	jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
	  -keystore ~/.android/release.keystore \
	  $(BUILD_DIR)/app-release-unsigned.apk lamontlabs_key
	@echo "==> Zipaligning..."
	zipalign -v 4 $(BUILD_DIR)/app-release-unsigned.apk $(BUILD_DIR)/$(APP_NAME)-signed.apk

verify:
	@echo "==> Verifying determinism..."
	bash verify.sh

bundle:
	@echo "==> Assembling Play Store AAB..."
	./gradlew bundleRelease
	mkdir -p $(DIST_DIR)
	cp app/build/outputs/bundle/release/app-release.aab $(DIST_DIR)/$(APP_NAME)-v1.1.aab
	@echo "==> Bundle ready in $(DIST_DIR)"
.PHONY: debug release sdk

sdk:
	bash ./scripts/setup-android-sdk.sh

debug:
	bash ./scripts/build-debug.sh

release:
	bash ./scripts/build-release.sh
.PHONY: sdk debug release binder full

sdk:
	bash ./scripts/setup-android-sdk.sh

debug:
	bash ./scripts/build-debug.sh

release:
	bash ./scripts/build-release.sh

binder:
	bash ./scripts/package-binder.sh

full:
	bash ./scripts/full-build.sh
