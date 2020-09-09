package uk.tomhomewood.jmriroster.screenshots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule
import uk.tomhomewood.jmriroster.ActivityViewRoster
import uk.tomhomewood.jmriroster.R
import uk.tomhomewood.jmriroster.RosterListViewHolder
import uk.tomhomewood.jmriroster.utils.waitFor

const val API_RESPONSE_DELAY_MS: Long = 5 * 1000
const val IMAGE_LOAD_DELAY_MS: Long = 5 * 1000
const val ROSTER_ID_66957: String = "66957"

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TestActivityViewRoster {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Rule @JvmField
    val activityRule = ActivityScenarioRule(ActivityViewRoster::class.java)

    @Test
    fun screenshotRosterList() {
        onView(isRoot())
            .perform(waitFor(API_RESPONSE_DELAY_MS))
        onView(withId(R.id.roster_list))
            .perform(RecyclerViewActions.scrollToHolder(RosterListViewHolderMatcher(ROSTER_ID_66957)))
        onView(isRoot())
            .perform(waitFor(IMAGE_LOAD_DELAY_MS))
        Screengrab.screenshot("1_roster_list")
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