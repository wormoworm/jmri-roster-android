package uk.tomhomewood.jmriroster.screenshots.base

import org.junit.After
import org.junit.Before
import org.junit.Rule
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType
import tools.fastlane.screengrab.locale.LocaleTestRule

open class BaseScreenshotTest {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Before
    fun setup() {
        enterDemoMode()
    }

    @After
    fun tearDown() {
//        exitDemoMode()
    }

    private fun enterDemoMode() {
        CleanStatusBar()
            .setClock("1337")
            .setShowNotifications(false)
            .setWifiLevel(4)
            .setMobileNetworkDataType(MobileDataType.LTE)
            .setMobileNetworkLevel(3)
            .setBatteryLevel(100)
            .setBatteryPlugged(false)
            .enable()
    }

    private fun exitDemoMode() {
        CleanStatusBar.disable()
    }
}