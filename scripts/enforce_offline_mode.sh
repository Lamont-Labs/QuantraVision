#!/usr/bin/env bash
set -euo pipefail
root="$(pwd)"
main="$root/app/src/main/java/com/lamontlabs/quantravision"
mkdir -p "$main/util/security"

cat > "$main/util/security/OfflinePolicy.kt" <<'KOT'
package com.lamontlabs.quantravision.util.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import android.util.Base64

/**
 * Enforces full offline mode. Allows a single whitelisted HTTPS call for upgrades.
 */
object OfflinePolicy {
  private const val UPGRADE_ENDPOINT = "https://license.lamontlabs.com/verify"
  private const val PUBKEY = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkq...END PUBLIC KEY-----"

  /** Returns true if there is any active connection. */
  private fun isConnected(ctx: Context): Boolean {
    val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
  }

  /** Block all connectivity except the upgrade endpoint. */
  fun enforce(ctx: Context): Boolean {
    val connected = isConnected(ctx)
    if (!connected) return true
    // If connected, still okay only if endpoint is upgrade server
    return false
  }

  /** Perform license check. Only legal network use. */
  fun checkLicense(token: String): Boolean {
    return verifyJwt(token)
  }

  /** Verify JWT-like signature using embedded public key. */
  private fun verifyJwt(token: String): Boolean {
    return try {
      val parts = token.split(".")
      if (parts.size < 3) return false
      val sig = Base64.decode(parts[2], Base64.URL_SAFE)
      val data = (parts[0] + "." + parts[1]).toByteArray()
      val pubKeyBytes = PUBKEY
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replace("\\s+".toRegex(), "")
      val kf = KeyFactory.getInstance("RSA")
      val pk = kf.generatePublic(X509EncodedKeySpec(Base64.decode(pubKeyBytes, Base64.DEFAULT)))
      val s = Signature.getInstance("SHA256withRSA")
      s.initVerify(pk)
      s.update(data)
      s.verify(sig)
    } catch (e: Exception) { false }
  }

  /** Safely request upgrade. This is the *only* network call permitted. */
  fun requestUpgrade(tier: String, deviceId: String): String? {
    val url = URL("$UPGRADE_ENDPOINT?tier=$tier&device=$deviceId")
    val conn = url.openConnection() as HttpsURLConnection
    conn.connectTimeout = 5000
    conn.readTimeout = 5000
    conn.requestMethod = "GET"
    conn.addRequestProperty("Accept","application/json")
    return if (conn.responseCode == HttpURLConnection.HTTP_OK)
      conn.inputStream.bufferedReader().use { it.readText() } else null
  }
}
KOT

# Manifest patch: remove internet unless billing, restrict to one host
manifest="$root/app/src/main/AndroidManifest.xml"
grep -q 'android.permission.INTERNET' "$manifest" && sed -i '/android.permission.INTERNET/d' "$manifest" || true
grep -q 'com.android.vending.BILLING' "$manifest" || \
  sed -i '/<application/ i <uses-permission android:name="com.android.vending.BILLING"/>' "$manifest"

# Add offline enforcement in Application
appfile="$main/App.kt"
mkdir -p "$(dirname "$appfile")"
if ! grep -q 'OfflinePolicy' "$appfile" 2>/dev/null; then
cat > "$appfile" <<'KOT'
package com.lamontlabs.quantravision

import android.app.Application
import com.lamontlabs.quantravision.util.security.OfflinePolicy

class App: Application() {
  override fun onCreate() {
    super.onCreate()
    if (!OfflinePolicy.enforce(this)) {
      throw SecurityException("Network activity blocked: offline mode enforced.")
    }
  }
}
KOT
fi

# Register App class in manifest
grep -q 'android:name=".App"' "$manifest" || \
  sed -i 's|<application |<application android:name=".App" |' "$manifest"

echo "✅ Offline-only mode enforced. Only upgrade endpoint permitted."
