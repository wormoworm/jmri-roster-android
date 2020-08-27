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
            val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
                .penaltyLog()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                vmPolicyBuilder.detectNonSdkApiUsage()
            }
            StrictMode.setVmPolicy(vmPolicyBuilder.build())
        }
        super.onCreate()
    }
}