#!/usr/bin/env bash
set -euo pipefail

################################################################################
# Extract APK Signature Hash
# Extracts the SHA-256 signature hash from a signed APK for use in IntegrityChecker
################################################################################

readonly GREEN='\033[0;32m'
readonly RED='\033[0;31m'
readonly YELLOW='\033[1;33m'
readonly NC='\033[0m'

if [ $# -eq 0 ]; then
    echo -e "${RED}Error:${NC} No APK file specified"
    echo
    echo "Usage: $0 <path-to-signed-apk>"
    echo
    echo "Example:"
    echo "  $0 app/build/outputs/apk/release/app-release.apk"
    echo
    echo "This script extracts the SHA-256 signature hash from your signed APK"
    echo "for use in IntegrityChecker.kt EXPECTED_SIGNATURE_HASH constant."
    exit 1
fi

APK_FILE="$1"

if [ ! -f "$APK_FILE" ]; then
    echo -e "${RED}Error:${NC} APK file not found: $APK_FILE"
    exit 1
fi

echo "================================================================================"
echo "Extracting Signature Hash from APK"
echo "================================================================================"
echo
echo "APK File: $APK_FILE"
echo

# Check if apksigner is available
if command -v apksigner &> /dev/null; then
    echo "Using apksigner to extract certificate..."
    CERT_HASH=$(apksigner verify --print-certs "$APK_FILE" | grep -i "SHA-256" | head -1 | awk '{print $NF}' | tr -d ':' | tr '[:upper:]' '[:lower:]')
    
    if [ -n "$CERT_HASH" ]; then
        echo -e "${GREEN}✅ Success!${NC}"
        echo
        echo "================================================================================"
        echo "Your Release Signature Hash:"
        echo "================================================================================"
        echo
        echo -e "${GREEN}${CERT_HASH}${NC}"
        echo
        echo "================================================================================"
        echo "Next Steps:"
        echo "================================================================================"
        echo
        echo "1. Copy the hash above"
        echo "2. Open: app/src/main/java/com/lamontlabs/quantravision/security/IntegrityChecker.kt"
        echo "3. Find: private const val EXPECTED_SIGNATURE_HASH = \"PLACEHOLDER_SIGNATURE_HASH\""
        echo "4. Replace with: private const val EXPECTED_SIGNATURE_HASH = \"${CERT_HASH}\""
        echo "5. Commit the change"
        echo
        echo "Example:"
        echo "  sed -i 's/PLACEHOLDER_SIGNATURE_HASH/${CERT_HASH}/' app/src/main/java/com/lamontlabs/quantravision/security/IntegrityChecker.kt"
        echo
    else
        echo -e "${RED}❌ Failed${NC} to extract signature hash"
        exit 1
    fi
else
    # Fallback to keytool
    echo -e "${YELLOW}apksigner not found, using keytool fallback...${NC}"
    echo
    echo "To extract the signature hash manually:"
    echo
    echo "1. If you have your release keystore:"
    echo "   keytool -list -v -keystore /path/to/release.keystore -alias your_alias"
    echo
    echo "2. Look for 'SHA256' fingerprint in the certificate details"
    echo "3. Remove colons and convert to lowercase"
    echo "4. Update EXPECTED_SIGNATURE_HASH in IntegrityChecker.kt"
    echo
    exit 1
fi
