package uk.tomhomewood.jmriroster.screenshots

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import uk.tomhomewood.jmriroster.ActivityViewRosterEntry

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRosterEntry {

    private val rosterId: String = "66957"

    private lateinit var activityScenario: ActivityScenario<ActivityViewRosterEntry>

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setup() {
        activityScenario = ActivityScenario.launch(ActivityViewRosterEntry.getLaunchIntent(ApplicationProvider.getApplicationContext(), rosterId))
    }

    @Test
    fun testTakeScreenshots() {
        activityScenario.onActivity {
            Espresso.onView(ViewMatchers.isRoot()).perform(waitFor(API_DELAY_MS))
            Screengrab.screenshot("roster_entry")
        }
    }

    @After
    fun tearDown() {
        activityScenario.close()
    }
}