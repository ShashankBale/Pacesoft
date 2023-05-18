package com.sample.pacesoft.app

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.pacesoft.sdk.app.PaceSoftCallback
import com.pacesoft.sdk.app.PaceSoftSdk

class MainApp : Application(), LifecycleObserver {

    private val mPaceSoftCallback = object : PaceSoftCallback {
        override fun onForceUserLogout() {
            /*
            Handling Pacesoft SDK logout

            This will get trigger in couple of cases
            1. If pDefend detected some Threat in Android devices.
            2. If ClientApp logs out from the Pacesoft SDK manually.

            */
        }
    }

    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        PaceSoftSdk.init(
            application = this,
            appId = "PACESOFT_SAMPLE_DEMO", //TODO : Sha2nk get it pacesoft team
            callback = mPaceSoftCallback,
        )
    }
}

