package uk.tomhomewood.jmriroster.screenshots

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import uk.tomhomewood.jmriroster.ActivityViewRosterEntry
import uk.tomhomewood.jmriroster.screenshots.base.BaseScreenshotTest
import uk.tomhomewood.jmriroster.utils.waitFor

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRosterEntry : BaseScreenshotTest() {

    @Rule @JvmField
    val activityRule = ActivityScenarioRule<ActivityViewRosterEntry>(ActivityViewRosterEntry.getLaunchIntent(ApplicationProvider.getApplicationContext(), ROSTER_ID_66957))

    @Test
    fun screenshotRosterEntry() {
        onView(ViewMatchers.isRoot())
                .perform(waitFor(API_RESPONSE_DELAY_MS))
        Screengrab.screenshot("2_roster_entry")
    }
}