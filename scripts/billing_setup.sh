#!/usr/bin/env bash
# QuantraVision — Replit billing setup (ONE-TIME ONLY: Standard + Pro)
# Idempotent. Creates assets + Kotlin billing code. Adds billing dependency. Builds.
set -euo pipefail

APP_ID="com.lamontlabs.quantravision"
SRC="app/src/main/java/${APP_ID//./\/}"
ASSETS="app/src/main/assets"
RES="app/src/main/res"

mkdir -p "$ASSETS" "$SRC/billing" "$SRC/ui" "$RES/values"

# 1) SKUs (one-time only)
cat > "$ASSETS/billing_skus.json" <<'JSON'
{
  "skus": [
    { "sku": "qv_standard_one", "title": "Standard Unlock (One-Time)", "type": "inapp", "price": "4.99" },
    { "sku": "qv_pro_one",      "title": "Pro Unlock (One-Time)",      "type": "inapp", "price": "9.99" }
  ]
}
JSON

# 2) Strings (labels used by Upgrade screen)
cat > "$RES/values/strings.xml" <<'XML'
<resources>
    <string name="app_name">QuantraVision Overlay</string>
    <string name="qv_buy_once">Buy once. Own forever.</string>
    <string name="qv_no_subs">No subscriptions. No renewals.</string>
    <string name="qv_unlock_standard">Unlock Standard — $4.99</string>
    <string name="qv_unlock_pro">Unlock Pro — $9.99</string>
</resources>
XML

# 3) BillingManager (one-time INAPP only)
cat > "$SRC/billing/BillingManager.kt" <<'KOT'
package com.lamontlabs.quantravision.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*

/**
 * One-time purchases only. No subscriptions. Offline unlock flag persists locally.
 * SKUs: qv_standard_one, qv_pro_one (INAPP).
 */
class BillingManager(private val activity: Activity) : PurchasesUpdatedListener {

    private val prefs by lazy { activity.getSharedPreferences("qv_prefs", Context.MODE_PRIVATE) }
    private val unlockedKey = "qv_unlocked_tier" // "", "standard", "pro"

    private lateinit var client: BillingClient
    private var productMap: Map<String, ProductDetails> = emptyMap()

    fun start(onReady: () -> Unit = {}) {
        client = BillingClient.newBuilder(activity)
            .enablePendingPurchases()
            .setListener(this)
            .build()

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts(onReady)
                }
            }
            override fun onBillingServiceDisconnected() { /* retry handled by UI if needed */ }
        })
    }

    private fun queryProducts(onReady: () -> Unit) {
        val products = listOf("qv_standard_one", "qv_pro_one").map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()
        client.queryProductDetailsAsync(params) { res, list ->
            if (res.responseCode == BillingClient.BillingResponseCode.OK) {
                productMap = list.associateBy { it.productId }
                onReady()
            }
        }
    }

    fun purchaseStandard() = launchPurchase("qv_standard_one")
    fun purchasePro() = launchPurchase("qv_pro_one")

    private fun launchPurchase(sku: String) {
        val pd = productMap[sku] ?: return
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(pd)
                    .build()
            )).build()
        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (p in purchases) {
                acknowledgeIfNeeded(p)
                when (p.products.firstOrNull()) {
                    "qv_pro_one" -> setUnlocked("pro")
                    "qv_standard_one" -> if (getUnlockedTier() != "pro") setUnlocked("standard")
                }
            }
        }
    }

    private fun acknowledgeIfNeeded(p: Purchase) {
        if (!p.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()
            client.acknowledgePurchase(params) { /* no-op */ }
        }
    }

    private fun setUnlocked(tier: String) {
        prefs.edit().putString(unlockedKey, tier).apply()
    }

    fun getUnlockedTier(): String = prefs.getString(unlockedKey, "") ?: ""
    fun isStandard(): Boolean = getUnlockedTier() == "standard" || isPro()
    fun isPro(): Boolean = getUnlockedTier() == "pro"
}
KOT

# 4) Simple Upgrade screen (Compose) — two buttons, shows current tier
cat > "$SRC/ui/UpgradeScreen.kt" <<'KOT'
package com.lamontlabs.quantravision.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lamontlabs.quantravision.billing.BillingManager
import android.app.Activity

@Composable
fun UpgradeScreen(activity: Activity, bm: BillingManager) {
    val tier = remember { mutableStateOf(bm.getUnlockedTier()) }
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("QuantraVision Unlock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Buy once. Own forever.")
        Text("No subscriptions. No renewals.", color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))

        if (bm.isPro()) {
            Text("Current Tier: PRO", color = MaterialTheme.colorScheme.primary)
        } else if (bm.isStandard()) {
            Text("Current Tier: STANDARD", color = MaterialTheme.colorScheme.primary)
        } else {
            Text("Current Tier: FREE")
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = {
            bm.start { bm.purchaseStandard() }
        }, enabled = !bm.isStandard() && !bm.isPro(), modifier = Modifier.fillMaxWidth()) {
            Text("Unlock Standard — $4.99")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            bm.start { bm.purchasePro() }
        }, enabled = !bm.isPro(), modifier = Modifier.fillMaxWidth()) {
            Text("Unlock Pro — $9.99")
        }
        Spacer(Modifier.height(24.dp))
        Text("Pro includes all patterns, confidence overlays, export bundles.")
    }
}
KOT

# 5) Gradle dependency (billing-ktx) if missing
if ! grep -q 'com.android.billingclient:billing-ktx' app/build.gradle.kts; then
  awk '
    /dependencies\s*\{/ && !p { print; print "  implementation(\"com.android.billingclient:billing-ktx:6.1.0\")"; p=1; next }
    { print }
  ' app/build.gradle.kts > app/build.gradle.kts.tmp && mv app/build.gradle.kts.tmp app/build.gradle.kts
fi

# 6) Manifest sanity (no INTERNET in detection path; keep default)
# (No subscription service components needed.)

# 7) Build
./gradlew --no-daemon :app:assembleDebug -x test --build-cache --parallel
./gradlew --no-daemon :app:bundleRelease --build-cache --parallel

# 8) Artifacts
mkdir -p dist/release
cp -f app/build/outputs/apk/debug/*.apk dist/release/ 2>/dev/null || true
cp -f app/build/outputs/bundle/release/*.aab dist/release/ 2>/dev/null || true

echo "== One-time billing (Standard/Pro) integrated. Artifacts in dist/release =="
