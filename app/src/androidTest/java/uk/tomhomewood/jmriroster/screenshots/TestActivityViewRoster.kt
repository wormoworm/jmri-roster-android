package uk.tomhomewood.jmriroster.screenshots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import uk.tomhomewood.jmriroster.ActivityViewRoster

const val API_DELAY_MS: Long = 5 * 1000

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRoster {

    @Rule @JvmField
    var activityRule: ActivityScenarioRule<ActivityViewRoster> = ActivityScenarioRule(ActivityViewRoster::class.java)

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun testTakeScreenshot() {
        onView(isRoot()).perform(waitFor(API_DELAY_MS))
        Screengrab.screenshot("1_roster_list")
        Screengrab.screenshot("2_screenshot_two")

    }
}

fun waitFor(millis: Long): ViewAction{
    return object: ViewAction{
        override fun getDescription(): String {
            return "Wait for "+millis+"ms"
        }

        override fun getConstraints(): Matcher<View> {
            return isRoot()
        }

        override fun perform(uiController: UiController?, view: View?) {
            uiController!!.loopMainThreadForAtLeast(millis)
        }
    }
}