package uk.tomhomewood.jmriroster

import android.R
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("uk.tomhomewood.jmriroster", appContext.packageName)
    }

    @Rule
    @JvmField
    var activityRule: ActivityScenarioRule<ActivityViewRoster> = ActivityScenarioRule(ActivityViewRoster::class.java)

    @Test
    fun testTakeScreenshot() {
        Screengrab.screenshot("test_screenshot")

        // Your custom onView...
//        onView(withId(R.id.fab)).perform(click())
//        Screengrab.screenshot("after_button_click")
    }
}