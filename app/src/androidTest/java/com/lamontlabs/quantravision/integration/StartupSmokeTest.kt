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
            for (tab in listOf("Market", "Scan", "Apex AI", "Settings", "Home")) {
                val target = device.findObject(By.text(tab))
                assertNotNull("Missing navigation tab: $tab", target)
                target.click()
                device.waitForIdle()
                scenario.onActivity { activity -> assertFalse("Navigation closed the activity: $tab", activity.isFinishing) }
                assertTrue("Navigation disappeared after opening $tab", device.wait(Until.hasObject(By.text("Home")), 5_000))
                assertTrue("Could not capture $tab", device.takeScreenshot(File(context.getExternalFilesDir(null), "screen-${tab.lowercase().replace(' ', '-')}.png")))
            }
        }
    }
}
