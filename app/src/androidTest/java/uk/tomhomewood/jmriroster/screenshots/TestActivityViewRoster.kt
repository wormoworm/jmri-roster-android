package uk.tomhomewood.jmriroster.screenshots

import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import uk.tomhomewood.jmriroster.ActivityViewRoster
import uk.tomhomewood.jmriroster.R
import uk.tomhomewood.jmriroster.RosterListViewHolder

const val API_DELAY_MS: Long = 10 * 1000
const val ROSTER_ID_66957: String = "66957"

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRoster {

    @Rule @JvmField
    val activityRule = ActivityScenarioRule(ActivityViewRoster::class.java)

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun testTakeScreenshots() {
        onView(isRoot())
            .perform(waitFor(API_DELAY_MS))
        onView(withId(R.id.roster_list))
            .perform(RecyclerViewActions.scrollToHolder(RosterListViewHolderMatcher(ROSTER_ID_66957)))
        Screengrab.screenshot("roster_list")
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

class RosterListViewHolderMatcher(private val rosterId: String) : BaseMatcher<RosterListViewHolder>() {
    override fun describeTo(description: Description?) {
        description?.appendText("Searching for roster item with number: $rosterId")
    }

    override fun matches(item: Any?): Boolean {
        return (item as RosterListViewHolder).rosterId == rosterId
    }
}