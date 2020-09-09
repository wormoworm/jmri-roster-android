#!/bin/bash

# Enable demo mode in system settings
adb shell settings put global sysui_demo_allowed 1
# Enter demo mode
adb shell am broadcast -a com.android.systemui.demo -e command enter
# Set the time
adb shell settings put system time_12_24 24 # Ensure 24 hour mode
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1337
# Set full Wi-Fi signal
adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
# Battery full and not charging
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false
# Hide notifications
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false