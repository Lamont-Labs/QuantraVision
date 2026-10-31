package com.lamontlabs.quantravision.integration

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lamontlabs.quantravision.BuildConfig
import com.lamontlabs.quantravision.detection.VisualTemplateScanner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.yaml.snakeyaml.Yaml

/** Runs the real Android/OpenCV scanner against a bundled reference asset. */
@RunWith(AndroidJUnit4::class)
class DetectionFlowTest {
    @Test fun referenceTemplateIsLocatedInTheOriginalScreenshotCoordinates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals(BuildConfig.APPLICATION_ID, context.packageName)
        assertTrue("OpenCV must initialize", OpenCVLoader.initDebug())
        val yamlFile = context.assets.list("pattern_templates")!!.first { it.endsWith(".yaml") }
        val data = context.assets.open("pattern_templates/$yamlFile").use { Yaml().load<Map<String, Any>>(it) }
        val imageName = (data["image"] as String).substringAfterLast('/')
        val reference = context.assets.open("pattern_templates/$imageName").use { BitmapFactory.decodeStream(it) }!!
        val screenshot = Bitmap.createBitmap(reference.width + 80, reference.height + 80, Bitmap.Config.ARGB_8888)
        Canvas(screenshot).drawBitmap(reference, 40f, 40f, null)
        VisualTemplateScanner(context).use { scanner ->
            val match = scanner.scan(screenshot).firstOrNull { it.patternName == data["name"] }
            assertNotNull("Bundled reference must be recognized", match)
            assertEquals(40f, match!!.bbox!!.left, 3f)
            assertEquals(40f, match.bbox!!.top, 3f)
            assertTrue(match.confidence > 0.95)
        }
        reference.recycle()
        screenshot.recycle()
    }
}
