# QuantraVision Overlay Makefile

.PHONY: build clean install run verify

build:
	@./gradlew assembleDebug

clean:
	@./gradlew clean

install:
	@./gradlew installDebug

run:
	@adb shell am start -n com.lamontlabs.quantravision/.MainActivity

verify:
	@echo "Verifying provenance hashes..."
	@sh scripts/verify_hashes.sh
