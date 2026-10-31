package com.lamontlabs.quantravision.integration

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.lamontlabs.quantravision.MainActivity
import com.lamontlabs.quantravision.onboarding.OnboardingManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Launches the installed app and exercises every main navigation tab. */
@RunWith(AndroidJUnit4::class)
class StartupSmokeTest {
    @Test fun mainNavigationSurvivesFreshStartupWithoutARequiredLanguageModel() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        OnboardingManager.getInstance(context).completeOnboarding()
        val device = UiDevice.getInstance(instrumentation)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue("Home navigation must render", device.wait(Until.hasObject(By.text("Home")), 15_000))
            // Android's first immersive-mode hint can cover page headings after startup.
            device.wait(Until.findObject(By.res("android:id/ok").text("Got it")), 5_000)?.click()
            device.waitForIdle()
            val pages = linkedMapOf(
                // A fresh Free install must reach the Market feature gate, not paid content.
                "Market" to By.text("Real-Time Market Data"), "Scan" to By.text("Pattern Scanner"),
                "Apex AI" to By.text("QuantraBot"), "Settings" to By.text("Current Plan"),
                "Home" to By.textStartsWith("Welcome,")
            )
            for ((tab, content) in pages) {
                val target = device.findObject(By.text(tab))
                assertNotNull("Missing navigation tab: $tab", target)
                target.click()
                device.waitForIdle()
                scenario.onActivity { activity -> assertFalse("Navigation closed the activity: $tab", activity.isFinishing) }
                val rendered = device.wait(Until.hasObject(content), 15_000)
                val stem = "screen-${tab.lowercase().replace(' ', '-')}"
                assertTrue("Could not capture $tab", device.takeScreenshot(File(context.getExternalFilesDir(null), "$stem.png")))
                device.dumpWindowHierarchy(File(context.getExternalFilesDir(null), "$stem.xml"))
                assertTrue("Expected page content did not render for $tab", rendered)
            }
        }
    }
}
