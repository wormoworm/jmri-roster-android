package uk.tomhomewood.jmriroster

import android.app.Application
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi

class JmriRosterApplication: Application() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        if (BuildConfig.DEBUG && BuildConfig.STRICT_MODE) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                .detectNonSdkApiUsage()
                .penaltyLog()
                .build())
        }
        super.onCreate()
    }
}