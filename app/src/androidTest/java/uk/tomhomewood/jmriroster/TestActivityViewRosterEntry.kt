package uk.tomhomewood.jmriroster

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRosterEntry {

    private val rosterId: String = "66957"

    lateinit var activityScenario: ActivityScenario<ActivityViewRosterEntry>

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun testTakeScreenshot() {
//        onView(isRoot()).perform(waitFor(API_DELAY_MS))

        activityScenario = ActivityScenario.launch(ActivityViewRosterEntry.getLaunchIntent(ApplicationProvider.getApplicationContext(), rosterId))
        activityScenario.onActivity {
            Screengrab.screenshot("3_roster_entry")
        }
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }
}