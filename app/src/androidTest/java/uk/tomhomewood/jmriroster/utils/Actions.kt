package uk.tomhomewood.jmriroster.utils

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

fun waitFor(millis: Long): ViewAction {
    return object: ViewAction {
        override fun getDescription(): String {
            return "Wait for "+millis+"ms"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isRoot()
        }

        override fun perform(uiController: UiController?, view: View?) {
            uiController!!.loopMainThreadForAtLeast(millis)
        }
    }
}